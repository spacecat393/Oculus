package net.coderbot.iris.compat.sodium.mixin.block_id;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.jellysquid.mods.sodium.client.gl.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.render.pipeline.FluidRenderer;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.coderbot.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
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
	private void iris$setLocalPos(ChunkBuildContext context,
								  CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> cir,
								  ChunkRenderData.Builder renderData, VisGraph occluder, ChunkRenderBounds.Builder bounds, ChunkBuildBuffers buffers,
								  ChunkRenderCacheLocal cacheLocal,
								  WorldSlice slice, int baseX, int baseY, int baseZ, int maxX, int maxY, int maxZ,
								  Map<BlockPos, IModelData> modelDataMap,
								  BlockPos.MutableBlockPos pos, BlockPos.MutableBlockPos renderOffset,
								  int relY, int relZ, int relX) {
		if (context.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) context.buffers).iris$setLocalPos(relX, relY, relZ);
		}
	}

	@Redirect(method = "performBuild", at = @At(value = "INVOKE",
			target = "net/minecraft/world/level/block/state/BlockState.getSeed(Lnet/minecraft/core/BlockPos;)J"))
	private long iris$wrapGetBlockLayer(BlockState blockState, BlockPos pos, ChunkBuildContext context) {
		if (context.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) context.buffers).iris$setMaterialId(blockState, ExtendedDataHelper.BLOCK_RENDER_TYPE);
		}

		return blockState.getSeed(pos);
	}

	@Redirect(method = "performBuild", at = @At(value = "INVOKE",
			target = "me/jellysquid/mods/sodium/client/render/pipeline/FluidRenderer.render(" +
						"Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lme/jellysquid/mods/sodium/client/render/chunk/compile/buffers/ChunkModelBuilder;" + 
					")Z"), remap = false)
	private boolean iris$wrapGetFluidLayer(FluidRenderer renderer, BlockAndTintGetter world, FluidState fluidState, BlockPos pos, BlockPos offset, ChunkModelBuilder buffers, ChunkBuildContext context) {
		if (context.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) context.buffers).iris$setMaterialId(fluidState.createLegacyBlock(), ExtendedDataHelper.FLUID_RENDER_TYPE);
		}

		return renderer.render(world, fluidState, pos, offset, buffers);
	}

	@Inject(method = "performBuild",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
	private void iris$resetContext(ChunkBuildContext buildContext, CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> cir) {
		if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
			((ChunkBuildBuffersExt) buildContext.buffers).iris$resetBlockContext();
		}
	}
}