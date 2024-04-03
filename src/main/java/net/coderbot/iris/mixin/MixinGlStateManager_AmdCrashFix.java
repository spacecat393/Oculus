package net.coderbot.iris.mixin;

import net.minecraft.client.renderer.OpenGlHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.coderbot.iris.gl.shader.ShaderWorkarounds;

import java.nio.ByteBuffer;

/**
 * Works around a bug in AMD drivers that causes crashes with glShaderSource.
 *
 * See {@link ShaderWorkarounds#safeShaderSource(int, ByteBuffer)} for more details.
 */
@Mixin(OpenGlHelper.class)
public class MixinGlStateManager_AmdCrashFix {
	@Redirect(method = "glShaderSource",
			at = @At(value = "INVOKE",
					target = "Lorg/lwjgl/opengl/GL20;glShaderSource(ILjava/nio/ByteBuffer;)V"))
	private static void iris$safeShaderSource(int glId, ByteBuffer source) {
		ShaderWorkarounds.safeShaderSource(glId, source);
	}

	@Redirect(method = "glShaderSource",
			at = @At(value = "INVOKE",
					target = "Lorg/lwjgl/opengl/ARBShaderObjects;glShaderSourceARB(ILjava/nio/ByteBuffer;)V"))
	private static void iris$arbSafeShaderSource(int glId, ByteBuffer source) {
		ShaderWorkarounds.safeShaderSource(glId, source);
	}
}
