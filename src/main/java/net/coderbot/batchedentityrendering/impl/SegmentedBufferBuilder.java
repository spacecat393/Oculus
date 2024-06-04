package net.coderbot.batchedentityrendering.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;

import static net.coderbot.batchedentityrendering.impl.RenderLayerUtil.*;

public class SegmentedBufferBuilder implements MemoryTrackingBuffer {
    private final BufferBuilder buffer;
    private final List<BlockRenderLayer> usedLayers;
    private BlockRenderLayer currentLayer;

    public SegmentedBufferBuilder() {
        // 2 MB initial allocation
        this.buffer = new BufferBuilder(512 * 1024);
        this.usedLayers = new ArrayList<>(256);

        this.currentLayer = null;
    }

    public BufferBuilder getBuffer(BlockRenderLayer renderLayer) {
        if (!Objects.equals(currentLayer, renderLayer)) {
            if (currentLayer != null) {
                buffer.finishDrawing();
                usedLayers.add(currentLayer);
            }

            // Begin drawing with the specified render layer
            buffer.begin(getGLMode(renderLayer), DefaultVertexFormats.BLOCK);

            currentLayer = renderLayer;
        }

        // Use duplicate vertices to break up triangle strips
        // https://developer.apple.com/library/archive/documentation/3DDrawing/Conceptual/OpenGLES_ProgrammingGuide/Art/degenerate_triangle_strip_2x.png
        // This works by generating zero-area triangles that don't end up getting rendered.
        // TODO: How do we handle DEBUG_LINE_STRIP?
        if (isTriangleStripDrawMode(currentLayer)) {
            buffer.putBulkData(buffer.getByteBuffer().duplicate());
        }

        return buffer;
    }

    public List<BufferSegment> getSegments() {
        if (currentLayer == null) {
            return Collections.emptyList();
        }

        usedLayers.add(currentLayer);

        buffer.finishDrawing();
        currentLayer = null;

        List<BufferSegment> segments = new ArrayList<>(usedLayers.size());

        for (BlockRenderLayer layer : usedLayers) {
            ByteBuffer slice = buffer.getByteBuffer().slice();
            BufferBuilder.State drawState = buffer.getVertexState();
            segments.add(new BufferSegment(slice, drawState, layer));
        }

        usedLayers.clear();
        return segments;
    }

    @Override
    public int getAllocatedSize() {
        return buffer.getByteBuffer().capacity();
    }

    @Override
    public int getUsedSize() {
        return buffer.getByteBuffer().limit();
    }
}
