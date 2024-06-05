package net.coderbot.batchedentityrendering.impl.ordering;

import net.minecraft.util.BlockRenderLayer;

import java.util.LinkedHashSet;

public class SimpleRenderOrderManager implements RenderOrderManager {
    private final LinkedHashSet<BlockRenderLayer> renderTypes;

    public SimpleRenderOrderManager() {
        renderTypes = new LinkedHashSet<>();
    }

    public void begin(BlockRenderLayer type) {
        renderTypes.add(type);
    }

    public void startGroup() {
        // no-op
    }

    public boolean maybeStartGroup() {
        // no-op
        return false;
    }

    public void endGroup() {
        // no-op
    }

    @Override
    public void reset() {
        renderTypes.clear();
    }

    public Iterable<BlockRenderLayer> getRenderOrder() {
        return renderTypes;
    }
}
