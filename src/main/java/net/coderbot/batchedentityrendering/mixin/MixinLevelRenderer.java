package net.coderbot.batchedentityrendering.mixin;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.coderbot.batchedentityrendering.impl.DrawCallTrackingRenderBuffers;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.batchedentityrendering.impl.RenderBuffersExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;

/**
 * Tracks whether the world is being rendered, and manages grouping
 * with different entities.
 */
// Uses a priority of 999 to apply before the main Iris mixins to draw entities before deferred runs.
@Mixin(value = RenderGlobal.class, priority = 999)
public class MixinLevelRenderer {
	private static final String RENDER_ENTITY =
			"renderEntity(Lnet/minecraft/entity/Entity;DDDFFZ)V";

	@Shadow
	private EntityRenderer entityRenderer;

	@Unique
	private Groupable groupable;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void batchedentityrendering$beginLevelRender(Entity entity, double d0, double d1, double d2, float partialTicks, CallbackInfo ci) {
		if (entityRenderer instanceof DrawCallTrackingRenderBuffers) {
			((DrawCallTrackingRenderBuffers) entityRenderer).resetDrawCounts();
		}
		((RenderBuffersExt) entityRenderer).beginLevelRendering();
		if (entityRenderer instanceof Groupable) {
			groupable = (Groupable) entityRenderer;
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_ENTITY))
	private void batchedentityrendering$preRenderEntity(Entity entity, double d0, double d1, double d2, float partialTicks, CallbackInfo ci) {
		if (groupable != null) {
			groupable.startGroup();
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_ENTITY, shift = At.Shift.AFTER))
	private void batchedentityrendering$postRenderEntity(Entity entity, double d0, double d1, double d2, float partialTicks, CallbackInfo ci) {
		if (groupable != null) {
			groupable.endGroup();
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void batchedentityrendering$beginTranslucents(Entity entity, double d0, double d1, double d2, float partialTicks, CallbackInfo ci) {
		Minecraft.getMinecraft().profiler.endStartSection("entity_draws");
		// todo: double-check
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(new ResourceLocation("textures/entity/default.png"));
	}

	@Inject(method = "renderLevel", at = @At("RETURN"))
	private void batchedentityrendering$endLevelRender(Entity entity, double d0, double d1, double d2, float partialTicks, CallbackInfo ci) {
		((RenderBuffersExt) entityRenderer).endLevelRendering();
		groupable = null;
	}
}
