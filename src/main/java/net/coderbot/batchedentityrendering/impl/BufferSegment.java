package net.coderbot.batchedentityrendering.impl;

import java.nio.ByteBuffer;

import lombok.Getter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;

public class BufferSegment {
    @Getter
    private final ByteBuffer slice;
    @Getter
    private final BufferBuilder.State drawState;
    private final BlockRenderLayer layer;

    public BufferSegment(ByteBuffer slice, BufferBuilder.State drawState, BlockRenderLayer layer) {
        this.slice = slice;
        this.drawState = drawState;
        this.layer = layer;
    }

    public BlockRenderLayer getRenderLayer() {
        return layer;
    }
}
