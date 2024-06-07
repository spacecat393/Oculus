package net.coderbot.iris.mixin;

import net.coderbot.iris.gl.shader.ShaderWorkarounds;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Works around a bug in AMD drivers that causes crashes with glShaderSource.
 *
 * See @link ShaderWorkarounds#safeShaderSource(int, ByteBuffer) for more details.
 */
@Mixin(GlStateManager.class)
public class MixinGlStateManager_AmdCrashFix {
	@Redirect(method = "glShaderSource(ILjava/lang/CharSequence;)V",
			at = @At(value = "INVOKE",
					target = "org/lwjgl/opengl/GL20.glShaderSource (ILjava/lang/CharSequence;)V"))
	private static void iris$safeShaderSource(int glId, CharSequence source) {
		ShaderWorkarounds.safeShaderSource(glId, source);
	}
}
