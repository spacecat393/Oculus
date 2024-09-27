package net.coderbot.iris.mixin.state_tracking;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gbuffer_overrides.state.StateTracker;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.samplers.IrisSamplers;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	@Shadow
	private static int activeTextureUnit;

	@Inject(method = "enableTexture2D", at = @At("HEAD"))
	private static void iris$onEnableTexture(CallbackInfo ci) {
		if (activeTextureUnit == IrisSamplers.ALBEDO_TEXTURE_UNIT) {
			StateTracker.INSTANCE.albedoSampler = true;
		} else if (activeTextureUnit == IrisSamplers.LIGHTMAP_TEXTURE_UNIT) {
			StateTracker.INSTANCE.lightmapSampler = true;
		} else if (activeTextureUnit == IrisSamplers.OVERLAY_TEXTURE_UNIT) {
			StateTracker.INSTANCE.overlaySampler = true;
		} else {
			return;
		}

		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setInputs(StateTracker.INSTANCE.getInputs()));
	}

	@Inject(method = "disableTexture2D", at = @At("HEAD"))
	private static void iris$onDisableTexture(CallbackInfo ci) {
		if (activeTextureUnit == IrisSamplers.ALBEDO_TEXTURE_UNIT) {
			StateTracker.INSTANCE.albedoSampler = false;
		} else if (activeTextureUnit == IrisSamplers.LIGHTMAP_TEXTURE_UNIT) {
			StateTracker.INSTANCE.lightmapSampler = false;
		} else if (activeTextureUnit == IrisSamplers.OVERLAY_TEXTURE_UNIT) {
			StateTracker.INSTANCE.overlaySampler = false;
		} else {
			return;
		}

		Iris.getPipelineManager().getPipeline().ifPresent(p -> p.setInputs(StateTracker.INSTANCE.getInputs()));
	}

	@Inject(method = "glDrawArrays", at = @At("HEAD"))
	private static void iris$beforeDrawArrays(int mode, int first, int count, CallbackInfo ci)
	{
		Iris.getPipelineManager().getPipeline().ifPresent(WorldRenderingPipeline::syncProgram);
//		if (Minecraft.getMinecraft().world != null)
//		{
//			GL30.glGetFramebufferAttachmentParameter(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, OPENGL_INTBUFFER);
//			int gl_color_attachment0 = OPENGL_INTBUFFER.get(0);
//			GL30.glGetFramebufferAttachmentParameter(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_DEPTH_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, OPENGL_INTBUFFER);
//			int gl_depth_attachment = OPENGL_INTBUFFER.get(0);
//
//			Iris.logger.info("gl_color_attachment0 " + gl_color_attachment0);
//			checkTextureInfo(gl_color_attachment0);
//
//			Iris.logger.info("gl_depth_attachment " + gl_depth_attachment);
//			checkTextureInfo(gl_depth_attachment);
//		}
	}
}
