package net.coderbot.batchedentityrendering.impl.ordering;

import net.coderbot.batchedentityrendering.impl.CustomRenderType;

public interface RenderOrderManager {
    void begin(CustomRenderType type);
    void startGroup();
    boolean maybeStartGroup();
    void endGroup();
    void reset();
    Iterable<CustomRenderType> getRenderOrder();
}
