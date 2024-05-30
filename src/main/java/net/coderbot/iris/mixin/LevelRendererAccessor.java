package net.coderbot.iris.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//import com.mojang.blaze3d.vertex.PoseStack;

/*import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType; */
import net.minecraft.client.renderer.culling.Frustum;
/*import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;*/

@Mixin(RenderGlobal.class)
public interface LevelRendererAccessor {
	@Accessor("renderManager")
	RenderManager getEntityRenderDispatcher();

	@Invoker("renderBlockLayer")
	void invokeRenderChunkLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn);

	// replaced Camera with EntityRenderer
	@Invoker("setupRender")
	void invokeSetupRender(EntityRenderer camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator);

	// removed PoseStack and MultiBufferSource
	@Invoker("renderEntity")
	void invokeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta);

	@Accessor("world")
	WorldClient getLevel();

	@Accessor("frameId")
	int getFrameId();

	@Accessor("frameId")
	void setFrameId(int frame);

	//@Accessor("renderBuffers")
	//RenderBuffers getRenderBuffers();

	//@Accessor("renderBuffers")
	//void setRenderBuffers(RenderBuffers buffers);

	@Accessor("generateClouds")
	boolean shouldRegenerateClouds();

	@Accessor("generateClouds")
	void setShouldRegenerateClouds(boolean shouldRegenerate);
}
