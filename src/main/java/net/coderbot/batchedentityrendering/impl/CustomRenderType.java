package net.coderbot.batchedentityrendering.impl;

import lombok.Getter;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.util.Optional;

public abstract class CustomRenderType {
    @Getter
    private final String name;
    @Getter
    private final VertexFormat format;
    @Getter
    private final int mode;
    @Getter
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;

    public CustomRenderType(String name, VertexFormat format, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, int mode) {
        this.name = name;
        this.format = format;
        this.bufferSize = bufferSize;
        this.affectsCrumbling = affectsCrumbling;
        this.sortOnUpload = sortOnUpload;
        this.mode = mode;
    }

    public abstract void setupRenderState();

    public abstract void clearRenderState();

    public Optional<CustomRenderType> outline() {
        return Optional.empty();
    }

    public boolean isOutline() {
        return false;
    }

    public boolean affectsCrumbling() {
        return affectsCrumbling;
    }

    public boolean sortOnUpload() {
        return sortOnUpload;
    }
}
