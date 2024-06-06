package net.coderbot.batchedentityrendering.impl;

import java.nio.ByteBuffer;

import lombok.Getter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexBuffer;

public class BufferSegment {
    @Getter
    private final ByteBuffer slice;
    @Getter
    private final BufferBuilder.State drawState;
    private final CustomRenderType type;

    public BufferSegment(ByteBuffer slice, BufferBuilder.State drawState, CustomRenderType type) {
        this.slice = slice;
        this.drawState = drawState;
        this.type = type;
    }

    public CustomRenderType getRenderType() {
        return type;
    }
}
