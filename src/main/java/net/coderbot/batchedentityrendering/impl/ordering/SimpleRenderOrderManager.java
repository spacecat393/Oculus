package net.coderbot.batchedentityrendering.impl.ordering;

import net.coderbot.batchedentityrendering.impl.CustomRenderType;
import net.minecraft.util.BlockRenderLayer;

import java.util.LinkedHashSet;

public class SimpleRenderOrderManager implements RenderOrderManager {
    private final LinkedHashSet<CustomRenderType> renderTypes;

    public SimpleRenderOrderManager() {
        renderTypes = new LinkedHashSet<>();
    }

    public void begin(CustomRenderType type) {
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

    public Iterable<CustomRenderType> getRenderOrder() {
        return renderTypes;
    }
}
