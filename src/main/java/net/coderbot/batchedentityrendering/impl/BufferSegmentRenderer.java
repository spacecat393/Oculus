package net.coderbot.batchedentityrendering.impl;

//import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
//import com.mojang.blaze3d.vertex.BufferUploader;


public class BufferSegmentRenderer {
    private final BufferBuilder fakeBufferBuilder;
    private final BufferBuilderExt fakeBufferBuilderExt;

    public BufferSegmentRenderer() {
        this.fakeBufferBuilder = new BufferBuilder(0);
        this.fakeBufferBuilderExt = (BufferBuilderExt) this.fakeBufferBuilder;
    }

    /**
     * Sets up the render type, draws the buffer, and then tears down the render type.
     */
    public void draw(BufferSegment segment) {
        //segment.getRenderLayer().setupRenderState();
        drawInner(segment);
        //segment.getRenderLayer().clearRenderState();
    }

    /**
     * Like draw(), but it doesn't setup / tear down the render type.
     */
    public void drawInner(BufferSegment segment) {
        fakeBufferBuilderExt.setupBufferSlice(segment.getSlice(), segment.getDrawState());
        Tessellator.getInstance().draw();
        fakeBufferBuilderExt.teardownBufferSlice();
    }
}
