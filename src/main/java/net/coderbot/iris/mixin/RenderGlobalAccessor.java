package net.coderbot.iris.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderGlobal.class)
public interface RenderGlobalAccessor {
	@Accessor("renderManager")
	RenderManager getEntityRenderDispatcher();

//	@Invoker("renderBlockLayer")
//	void invokeRenderChunkLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn);
//
//	@Invoker("setupRender")
//	void invokeSetupRender(EntityRenderer camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator);
//
//	@Invoker("renderEntity")
//	void invokeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta);

	@Accessor("world")
	WorldClient getWorld();

//	@Accessor("frameId")
//	int getFrameId();
//
//	@Accessor("frameId")
//	void setFrameId(int frame);
//
//	@Accessor("generateClouds")
//	boolean shouldRegenerateClouds();
//
//	@Accessor("generateClouds")
//	void setShouldRegenerateClouds(boolean shouldRegenerate);
}
