package net.coderbot.batchedentityrendering.impl;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.nio.ByteBuffer;

public class BufferSegmentRenderer {
    private final BufferBuilder fakeBufferBuilder;
    private final VertexBufferUploader uploader;

    public BufferSegmentRenderer() {
        this.fakeBufferBuilder = new BufferBuilder(0);
        this.uploader = new VertexBufferUploader();
    }

    public void draw(BufferSegment segment) {
        segment.getRenderType().setupRenderState();
        drawInner(segment);
        segment.getRenderType().clearRenderState();
    }

    public void drawInner(BufferSegment segment) {
        setupBufferSlice(segment.getSlice(), segment.getDrawState());
        uploader.draw(fakeBufferBuilder);
        teardownBufferSlice();
    }

    private void setupBufferSlice(ByteBuffer buffer, BufferBuilder.State drawState) {
        fakeBufferBuilder.setVertexState(drawState);
        fakeBufferBuilder.getByteBuffer().put(buffer);
    }

    private void teardownBufferSlice() {
        fakeBufferBuilder.reset();
    }
}