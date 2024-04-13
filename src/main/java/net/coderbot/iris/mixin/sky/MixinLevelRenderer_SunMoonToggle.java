package net.coderbot.iris.mixin.sky;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows pipelines to disable the sun, moon, or both.
 */
@Mixin(RenderGlobal.class)
public class MixinLevelRenderer_SunMoonToggle {
	/**
	 * This is a convenient way to disable rendering the sun / moon, since this clears the sun's vertices from
	 * the buffer, then when BufferRenderer is passed the empty buffer it will notice that it's empty and
	 * won't dispatch an unnecessary draw call. Nice!
	 */
	@Unique
	private void iris$emptyBuilder() {
		BufferBuilder builder = Tessellator.getInstance().getBuffer();

		builder.discard();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		builder.end();
	}

	@Inject(method = "renderSky(FI)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;draw()V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;SUN_TEXTURES:Lnet/minecraft/util/ResourceLocation;"),
			to = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;MOON_PHASES_TEXTURES:Lnet/minecraft/util/ResourceLocation;")),
		allow = 1)
	private void iris$beforeDrawSun(float partialTicks, int pass, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderSun).orElse(true)) {
			iris$emptyBuilder();
		}
	}

	@Inject(method = "renderSky(FI)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;draw()V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;MOON_PHASES_TEXTURES:Lnet/minecraft/util/ResourceLocation;"),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getStarBrightness(F)F")),
		allow = 1)
	private void iris$beforeDrawMoon(float partialTicks, int pass, CallbackInfo ci) {
		if (!Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderMoon).orElse(true)) {
			iris$emptyBuilder();
		}
	}
}
