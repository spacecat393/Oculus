package net.coderbot.batchedentityrendering.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.renderer.BufferBuilder;
import net.coderbot.batchedentityrendering.mixin.RenderTypeAccessor;

public class SegmentedBufferBuilder implements MemoryTrackingBuffer {
    private final BufferBuilder buffer;
    private final List<CustomRenderType> usedTypes;
    private CustomRenderType currentType;

    public SegmentedBufferBuilder() {
        // 2 MB initial allocation
        this.buffer = new BufferBuilder(512 * 1024);
        this.usedTypes = new ArrayList<>(256);
        this.currentType = null;
    }

    public BufferBuilder getBuffer(CustomRenderType renderType) {
        if (!Objects.equals(currentType, renderType)) {
            if (currentType != null) {
                if (shouldSortOnUpload(currentType)) {
                    buffer.sortVertexData(0, 0, 0);
                }

                buffer.finishDrawing();
                usedTypes.add(currentType);
            }

            buffer.begin(renderType.getMode(), renderType.getFormat());
            currentType = renderType;
        }

        return buffer;
    }

    public List<BufferSegment> getSegments() {
        if (currentType == null) {
            return Collections.emptyList();
        }

        usedTypes.add(currentType);

        if (shouldSortOnUpload(currentType)) {
            buffer.sortVertexData(0, 0, 0);
        }

        buffer.finishDrawing();
        currentType = null;

        List<BufferSegment> segments = new ArrayList<>(usedTypes.size());

        for (CustomRenderType type : usedTypes) {
            BufferBuilder.State drawState = buffer.getVertexState();
            ByteBuffer slice = buffer.getByteBuffer();

            segments.add(new BufferSegment(slice, drawState, type));
        }

        usedTypes.clear();

        return segments;
    }

    private static boolean shouldSortOnUpload(CustomRenderType type) {
        // todo: fix
        // return ((RenderTypeAccessor) type).shouldSortOnUpload();
        return false;
    }

    @Override
    public int getAllocatedSize() {
        return buffer.getByteBuffer().capacity();
    }

    @Override
    public int getUsedSize() {
        return buffer.getByteBuffer().position();
    }
}