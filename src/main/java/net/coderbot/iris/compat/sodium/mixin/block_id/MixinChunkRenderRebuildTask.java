package net.coderbot.iris.compat.sodium.mixin.block_id;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.coderbot.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.data.IModelData;

/**
 * Passes additional information indirectly to the vertex writer to support the mc_Entity and at_midBlock parts of the vertex format.
 */
@Mixin(ChunkRenderRebuildTask.class)
public class MixinChunkRenderRebuildTask {
	@Inject(method = "performBuild", at = @At(value = "INVOKE",
			target = "net/minecraft/world/level/block/state/BlockState.getRenderShape()" +
					"Lnet/minecraft/world/level/block/RenderShape;"),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$setLocalPos(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers,
								  CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult<?>> cir,
								  ChunkRenderData.Builder renderData, VisGraph occluder, ChunkRenderBounds.Builder bounds,
								  WorldSlice slice, int baseX, int baseY, int baseZ,
								  Map<BlockPos, IModelData> modelDataMap, BlockPos.MutableBlockPos pos, BlockPos renderOffset,
								  int relY, int relZ, int relX) {
		if (buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buffers).iris$setLocalPos(relX, relY, relZ);
		}
	}

	/*@Redirect(method = "performBuild", at = @At(value = "INVOKE",
			target = "net/minecraft/client/renderer/ItemBlockRenderTypes.getChunkRenderType(" +
						"Lnet/minecraft/world/level/block/state/BlockState;" +
					")Lnet/minecraft/client/renderer/RenderType;"))
	private RenderType iris$wrapGetBlockLayer(BlockState blockState, ChunkRenderCacheLocal cache,
											  ChunkBuildBuffers buffers, CancellationSource cancellationSource) {
		if (buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buffers).iris$setMaterialId(blockState, ExtendedDataHelper.BLOCK_RENDER_TYPE);
		}

		return ItemBlockRenderTypes.getChunkRenderType(blockState);
	}

	@Redirect(method = "performBuild", at = @At(value = "INVOKE",
			target = "net/minecraft/client/renderer/ItemBlockRenderTypes.getRenderLayer(" +
						"Lnet/minecraft/world/level/material/FluidState;" +
					")Lnet/minecraft/client/renderer/RenderType;"))
	private RenderType iris$wrapGetFluidLayer(FluidState fluidState, ChunkRenderCacheLocal cache,
											  ChunkBuildBuffers buffers, CancellationSource cancellationSource) {
		if (buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buffers).iris$setMaterialId(fluidState.createLegacyBlock(), ExtendedDataHelper.FLUID_RENDER_TYPE);
		}

		return ItemBlockRenderTypes.getRenderLayer(fluidState);
	}*/

	@Inject(method = "performBuild",
			at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/state/BlockState.hasTileEntity()Z"))
	private void iris$resetContext(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers,
							  CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult<?>> cir) {
		if (buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buffers).iris$resetBlockContext();
		}
	}
}
