package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.Matrix4f;

import me.jellysquid.mods.sodium.client.render.GameRendererContext;
import net.coderbot.iris.shadows.ShadowRenderingState;

/**
 * Allows the Iris shadow map projection matrix to be used during shadow rendering instead of the player view's
 * projection matrix.
 */
@Mixin(GameRendererContext.class)
public class MixinGameRendererContext {
	// todo: unable to locate obfuscation mapping
//	@Redirect(method = "getModelViewProjectionMatrix",
//			at = @At(value = "INVOKE",
//					target = "com/mojang/math/Matrix4f.copy ()Lcom/mojang/math/Matrix4f;"))
//	private static Matrix4f iris$useShadowProjectionMatrix(Matrix4f matrix) {
//		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
//			return ShadowRenderingState.getShadowOrthoMatrix();
//		} else {
//			return matrix.copy();
//		}
//	}
}
