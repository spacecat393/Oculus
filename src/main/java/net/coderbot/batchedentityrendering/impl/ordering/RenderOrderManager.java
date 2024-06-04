package net.coderbot.batchedentityrendering.impl.ordering;

import net.minecraft.util.BlockRenderLayer;

public interface RenderOrderManager {
    void begin(BlockRenderLayer type);
    void startGroup();
    boolean maybeStartGroup();
    void endGroup();
    void reset();
    Iterable<BlockRenderLayer> getRenderOrder();
}
