package net.coderbot.iris.compat.indigo.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Non-critical mixin config plugin, just disables mixins if Indigo isn't present,
 * since otherwise the log gets spammed with warnings.
 */
public class IrisIndigoCompatMixinPlugin implements IMixinConfigPlugin {
	public static boolean isReforgiumLoaded;

	@Override
	public void onLoad(String mixinPackage) {
		isReforgiumLoaded = FMLLoader.getLoadingModList().getModFileById("reforgium") != null;
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return isReforgiumLoaded;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
