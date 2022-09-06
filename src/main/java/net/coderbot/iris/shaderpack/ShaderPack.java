package net.coderbot.iris.shaderpack;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.features.FeatureFlags;
import net.coderbot.iris.gui.FeatureMissingErrorScreen;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import net.coderbot.iris.shaderpack.include.IncludeGraph;
import net.coderbot.iris.shaderpack.include.IncludeProcessor;
import net.coderbot.iris.shaderpack.include.ShaderPackSourceNames;
import net.coderbot.iris.shaderpack.option.ProfileSet;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuContainer;
import net.coderbot.iris.shaderpack.option.values.MutableOptionValues;
import net.coderbot.iris.shaderpack.option.values.OptionValues;
import net.coderbot.iris.shaderpack.preprocessor.JcppProcessor;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.coderbot.iris.shaderpack.texture.TextureFilteringData;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.shaderpack.transform.line.LineTransform;
import net.coderbot.iris.shaderpack.transform.line.VersionDirectiveNormalizer;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShaderPack {
	private static final Gson GSON = new Gson();

	private final ProgramSet base;
	@Nullable
	private final ProgramSet overworld;
	private final ProgramSet nether;
	private final ProgramSet end;

	private final IdMap idMap;
	private final LanguageMap languageMap;
	private final EnumMap<TextureStage, Object2ObjectMap<String, CustomTextureData>> customTextureDataMap = new EnumMap<>(TextureStage.class);
	private final CustomTextureData customNoiseTexture;
	private final ShaderPackOptions shaderPackOptions;
	private final OptionMenuContainer menuContainer;

	private final ProfileSet.ProfileResult profile;
	private final String profileInfo;

	public ShaderPack(Path root, Iterable<StringPair> environmentDefines) throws IOException, IllegalStateException {
		this(root, Collections.emptyMap(), environmentDefines);
	}

	/**
	 * Reads a shader pack from the disk.
	 *
	 * @param root The path to the "shaders" directory within the shader pack. The created ShaderPack will not retain
	 *             this path in any form; once the constructor exits, all disk I/O needed to load this shader pack will
	 *             have completed, and there is no need to hold on to the path for that reason.
	 * @throws IOException if there are any IO errors during shader pack loading.
	 */
	public ShaderPack(Path root, Map<String, String> changedConfigs, Iterable<StringPair> environmentDefines) throws IOException, IllegalStateException {
		// A null path is not allowed.
		Objects.requireNonNull(root);


		ImmutableList.Builder<AbsolutePackPath> starts = ImmutableList.builder();
		ImmutableList<String> potentialFileNames = ShaderPackSourceNames.POTENTIAL_STARTS;

		ShaderPackSourceNames.findPresentSources(starts, root, AbsolutePackPath.fromAbsolutePath("/"),
				potentialFileNames);

		boolean hasWorld0 = ShaderPackSourceNames.findPresentSources(starts, root,
				AbsolutePackPath.fromAbsolutePath("/world0"), potentialFileNames);

		boolean hasNether = ShaderPackSourceNames.findPresentSources(starts, root,
				AbsolutePackPath.fromAbsolutePath("/world-1"), potentialFileNames);

		boolean hasEnd = ShaderPackSourceNames.findPresentSources(starts, root,
				AbsolutePackPath.fromAbsolutePath("/world1"), potentialFileNames);

		// Read all files and included files recursively
		IncludeGraph graph = new IncludeGraph(root, starts.build());

		if (!graph.getFailures().isEmpty()) {
			graph.getFailures().forEach((path, error) -> {
				Iris.logger.error("{}", error.toString());
			});

			throw new IOException("Failed to resolve some #include directives, see previous messages for details");
		}

		this.languageMap = new LanguageMap(root.resolve("lang"));

		// Discover, merge, and apply shader pack options
		this.shaderPackOptions = new ShaderPackOptions(graph, changedConfigs);
		graph = this.shaderPackOptions.getIncludes();

		Iterable<StringPair> finalEnvironmentDefines = environmentDefines;
		ShaderProperties shaderProperties = loadProperties(root, "shaders.properties")
				.map(source -> new ShaderProperties(source, shaderPackOptions, finalEnvironmentDefines))
				.orElseGet(ShaderProperties::empty);

		List<String> invalidFeatureFlags = shaderProperties.getRequiredFeatureFlags().stream().filter(FeatureFlags::isInvalid).map(FeatureFlags::getNameOfValue).collect(Collectors.toList());

		if (!invalidFeatureFlags.isEmpty()) {
			if (Minecraft.getInstance().screen instanceof ShaderPackScreen) {
				Minecraft.getInstance().setScreen(new FeatureMissingErrorScreen(Minecraft.getInstance().screen, new TextComponent("Shaderpack incompatible!"), new TextComponent("The shaderpack you are trying to load contains features unsupported by Iris or your PC. Please try another pack. List" + invalidFeatureFlags.stream()
					.collect(Collectors.joining(", ", ": ", ".")))));
			}
			IrisApi.getInstance().getConfig().setShadersEnabledAndApply(false);
		}

		List<String> optionalFeatureFlags = shaderProperties.getOptionalFeatureFlags().stream().filter(flag -> !FeatureFlags.isInvalid(flag)).collect(Collectors.toList());

		if (!optionalFeatureFlags.isEmpty()) {
			List<StringPair> newEnvDefines = new ArrayList<>();
			environmentDefines.forEach(newEnvDefines::add);
			optionalFeatureFlags.forEach(flag -> newEnvDefines.add(new StringPair("IRIS_FEATURE_" + flag, "")));
			environmentDefines = ImmutableList.copyOf(newEnvDefines);
		}

		ProfileSet profiles = ProfileSet.fromTree(shaderProperties.getProfiles(), this.shaderPackOptions.getOptionSet());
		this.profile = profiles.scan(this.shaderPackOptions.getOptionSet(), this.shaderPackOptions.getOptionValues());

		// Get programs that should be disabled from the detected profile
		List<String> disabledPrograms = new ArrayList<>();
		this.profile.current.ifPresent(profile -> disabledPrograms.addAll(profile.disabledPrograms));
		// Add programs that are disabled by shader options
		shaderProperties.getConditionallyEnabledPrograms().forEach((program, shaderOption) -> {
			if ("true".equals(shaderOption)) return;

			if ("false".equals(shaderOption) || !this.shaderPackOptions.getOptionValues().getBooleanValueOrDefault(shaderOption)) {
				disabledPrograms.add(program);
			}
		});

		this.menuContainer = new OptionMenuContainer(shaderProperties, this.shaderPackOptions, profiles);

		{
			String profileName = getCurrentProfileName();
			OptionValues profileOptions = new MutableOptionValues(
					this.shaderPackOptions.getOptionSet(), this.profile.current.map(p -> p.optionValues).orElse(new HashMap<>()));

			int userOptionsChanged = this.shaderPackOptions.getOptionValues().getOptionsChanged() - profileOptions.getOptionsChanged();

			this.profileInfo = "Profile: " + profileName + " (+" + userOptionsChanged + " option" + (userOptionsChanged == 1 ? "" : "s") + " changed by user)";
		}

		Iris.logger.info(this.profileInfo);

		// Prepare our include processor
		IncludeProcessor includeProcessor = new IncludeProcessor(graph);

		// Set up our source provider for creating ProgramSets
		Iterable<StringPair> finalEnvironmentDefines1 = environmentDefines;
		Function<AbsolutePackPath, String> sourceProvider = (path) -> {
			String pathString = path.getPathString();
			// Removes the first "/" in the path if present, and the file
			// extension in order to represent the path as its program name
			String programString = pathString.substring(pathString.indexOf("/") == 0 ? 1 : 0, pathString.lastIndexOf("."));

			// Return an empty program source if the program is disabled by the current profile
			if (disabledPrograms.contains(programString)) {
				return null;
			}

			ImmutableList<String> lines = includeProcessor.getIncludedFile(path);

			if (lines == null) {
				return null;
			}

			// Normalize version directives.
			lines = LineTransform.apply(lines, VersionDirectiveNormalizer.INSTANCE);

			StringBuilder builder = new StringBuilder();

			for (String line : lines) {
				builder.append(line);
				builder.append('\n');
			}

			// Apply GLSL preprocessor to source, while making environment defines available.
			//
			// This uses similar techniques to the *.properties preprocessor to avoid actually putting
			// #define statements in the actual source - instead, we tell the preprocessor about them
			// directly. This removes one obstacle to accurate reporting of line numbers for errors,
			// though there exist many more (such as relocating all #extension directives and similar things)
			String source = builder.toString();
			source = JcppProcessor.glslPreprocessSource(source, finalEnvironmentDefines1);

			return source;
		};

		this.base = new ProgramSet(AbsolutePackPath.fromAbsolutePath("/"), sourceProvider, shaderProperties, this);

		this.overworld = loadOverrides(hasWorld0, AbsolutePackPath.fromAbsolutePath("/world0"), sourceProvider,
				shaderProperties, this);
		this.nether = loadOverrides(hasNether, AbsolutePackPath.fromAbsolutePath("/world-1"), sourceProvider,
				shaderProperties, this);
		this.end = loadOverrides(hasEnd, AbsolutePackPath.fromAbsolutePath("/world1"), sourceProvider,
				shaderProperties, this);

		this.idMap = new IdMap(root, shaderPackOptions, environmentDefines);

		customNoiseTexture = shaderProperties.getNoiseTexturePath().map(path -> {
			try {
				return readTexture(root, path);
			} catch (IOException e) {
				Iris.logger.error("Unable to read the custom noise texture at " + path, e);

				return null;
			}
		}).orElse(null);

		shaderProperties.getCustomTextures().forEach((textureStage, customTexturePropertiesMap) -> {
			Object2ObjectMap<String, CustomTextureData> innerCustomTextureDataMap = new Object2ObjectOpenHashMap<>();
			customTexturePropertiesMap.forEach((samplerName, path) -> {
				try {
					innerCustomTextureDataMap.put(samplerName, readTexture(root, path));
				} catch (IOException e) {
					Iris.logger.error("Unable to read the custom texture at " + path, e);
				}
			});

			customTextureDataMap.put(textureStage, innerCustomTextureDataMap);
		});
	}

	private String getCurrentProfileName() {
		return profile.current.map(p -> p.name).orElse("Custom");
	}

	public String getProfileInfo() {
		return profileInfo;
	}

	@Nullable
	private static ProgramSet loadOverrides(boolean has, AbsolutePackPath path, Function<AbsolutePackPath, String> sourceProvider,
											ShaderProperties shaderProperties, ShaderPack pack) {
		if (has) {
			return new ProgramSet(path, sourceProvider, shaderProperties, pack);
		}

		return null;
	}

	// TODO: Copy-paste from IdMap, find a way to deduplicate this
	private static Optional<String> loadProperties(Path shaderPath, String name) {
		String fileContents = readProperties(shaderPath, name);
		if (fileContents == null) {
			return Optional.empty();
		}

		return Optional.of(fileContents);
	}

	// TODO: Implement raw texture data types
	public CustomTextureData readTexture(Path root, String path) throws IOException {
		CustomTextureData customTextureData;
		if (path.contains(":")) {
			String[] parts = path.split(":");

			if (parts.length > 2) {
				Iris.logger.warn("Resource location " + path + " contained more than two parts?");
			}

			if (parts[0].equals("minecraft") && (parts[1].equals("dynamic/lightmap_1") || parts[1].equals("dynamic/light_map_1"))) {
				customTextureData = new CustomTextureData.LightmapMarker();
			} else {
				customTextureData = new CustomTextureData.ResourceData(parts[0], parts[1]);
			}
		} else {
			// TODO: Make sure the resulting path is within the shaderpack?
			if (path.startsWith("/")) {
				// NB: This does not guarantee the resulting path is in the shaderpack as a double slash could be used,
				// this just fixes shaderpacks like Continuum 2.0.4 that use a leading slash in texture paths
				path = path.substring(1);
			}

			boolean blur = false;
			boolean clamp = false;

			String mcMetaPath = path + ".mcmeta";
			Path mcMetaResolvedPath = root.resolve(mcMetaPath);

			if (Files.exists(mcMetaResolvedPath)) {
				try {
					JsonObject meta = loadMcMeta(mcMetaResolvedPath);
					if (meta.get("texture") != null) {
						if (meta.get("texture").getAsJsonObject().get("blur") != null) {
							blur = meta.get("texture").getAsJsonObject().get("blur").getAsBoolean();
						}
						if (meta.get("texture").getAsJsonObject().get("clamp") != null) {
							clamp = meta.get("texture").getAsJsonObject().get("clamp").getAsBoolean();
						}
					}
				} catch (IOException e) {
					Iris.logger.error("Unable to read the custom texture mcmeta at " + mcMetaPath + ", ignoring: " + e);
				}
			}

			byte[] content = Files.readAllBytes(root.resolve(path));

			customTextureData = new CustomTextureData.PngData(new TextureFilteringData(blur, clamp), content);
		}
		return customTextureData;
	}

	private JsonObject loadMcMeta(Path mcMetaPath) throws IOException, JsonParseException {
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(Files.newInputStream(mcMetaPath), StandardCharsets.UTF_8));

		JsonReader jsonReader = new JsonReader(reader);
		return GSON.getAdapter(JsonObject.class).read(jsonReader);
	}

	private static String readProperties(Path shaderPath, String name) {
		try {
			// Property files should be encoded in ISO_8859_1.
			return new String(Files.readAllBytes(shaderPath.resolve(name)), StandardCharsets.ISO_8859_1);
		} catch (NoSuchFileException e) {
			Iris.logger.debug("An " + name + " file was not found in the current shaderpack");

			return null;
		} catch (IOException e) {
			Iris.logger.error("An IOException occurred reading " + name + " from the current shaderpack", e);

			return null;
		}
	}

	public ProgramSet getProgramSet(DimensionId dimension) {
		ProgramSet overrides;

		switch (dimension) {
			case OVERWORLD:
				overrides = overworld;
				break;
			case NETHER:
				overrides = nether;
				break;
			case END:
				overrides = end;
				break;
			default:
				throw new IllegalArgumentException("Unknown dimension " + dimension);
		}

		// NB: If a dimension overrides directory is present, none of the files from the parent directory are "merged"
		//     into the override. Rather, we act as if the overrides directory contains a completely different set of
		//     shader programs unrelated to that of the base shader pack.
		//
		//     This makes sense because if base defined a composite pass and the override didn't, it would make it
		//     impossible to "un-define" the composite pass. It also removes a lot of complexity related to "merging"
		//     program sets. At the same time, this might be desired behavior by shader pack authors. It could make
		//     sense to bring it back as a configurable option, and have a more maintainable set of code backing it.
		if (overrides != null) {
			return overrides;
		} else {
			return base;
		}
	}

	public IdMap getIdMap() {
		return idMap;
	}

	public EnumMap<TextureStage, Object2ObjectMap<String, CustomTextureData>> getCustomTextureDataMap() {
		return customTextureDataMap;
	}

	public Optional<CustomTextureData> getCustomNoiseTexture() {
		return Optional.ofNullable(customNoiseTexture);
	}

	public LanguageMap getLanguageMap() {
		return languageMap;
	}

	public ShaderPackOptions getShaderPackOptions() {
		return shaderPackOptions;
	}

	public OptionMenuContainer getMenuContainer() {
		return menuContainer;
	}
}
