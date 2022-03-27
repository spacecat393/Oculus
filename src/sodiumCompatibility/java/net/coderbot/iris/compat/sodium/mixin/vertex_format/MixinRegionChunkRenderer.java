package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.DefaultChunkRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultChunkRenderer.class)
public class MixinRegionChunkRenderer {
	/*@Shadow(remap = false)
	@Final
	@Mutable
	private GlVertexAttributeBinding[] vertexAttributeBindings;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$onInit(RenderDevice device, ChunkVertexType vertexType, CallbackInfo ci) {
		if (!Iris.isPackActive()) {
			return;
		}

		GlVertexFormat<ChunkMeshAttribute> vertexFormat = vertexType.getCustomVertexFormat();

		vertexAttributeBindings = ArrayUtils.addAll(vertexAttributeBindings,
				new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
						vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
				new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
						vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
				new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
						vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
				new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
						vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL))
		);
	}*/
}