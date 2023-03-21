package net.coderbot.iris.mixin.texture.pbr;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(DirectoryLister.class)
public class MixinDirectoryLister {
	@Shadow
	@Final
	private String sourcePath;
	
	@Shadow
	@Final
	private String idPrefix;
	
	/*@ModifyArgs(method = "run(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/atlas/SpriteSource$Output;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V", remap = false, ordinal = 0))
	private void iris$modifyForEachAction(Args args, ResourceManager resourceManager, SpriteSource.Output output) {
		BiConsumer<? super ResourceLocation, ? super Resource> action = args.get(0);
		BiConsumer<? super ResourceLocation, ? super Resource> wrappedAction = (location, resource) -> {
			ResourceLocation baseLocation = PBRType.removeSuffix(location);
			if (baseLocation != null && resourceManager.getResource(baseLocation).isPresent()) {
				return;
			}
			action.accept(location, resource);
		};
		args.set(0, wrappedAction);
	}*/
	

	@Overwrite
	public void run(ResourceManager resourceManager, SpriteSource.Output output) {
	    FileToIdConverter fileToIdConverter = new FileToIdConverter("textures/" + sourcePath, ".png");
	    fileToIdConverter.listMatchingResources(resourceManager).forEach((location, resource) -> {
	        ResourceLocation baseLocation = PBRType.removeSuffix(location);
	        if (baseLocation != null && resourceManager.getResource(baseLocation).isPresent()) {
	            return;
	        }
	        ResourceLocation resourceLocation = fileToIdConverter.fileToId(location).withPrefix(idPrefix);
	        output.add(resourceLocation, resource);
	    });
	}
	
}
