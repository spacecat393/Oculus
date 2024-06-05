package net.coderbot.batchedentityrendering.impl.ordering;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
//import java.util.List;

import net.coderbot.batchedentityrendering.impl.BlendingStateHolder;
import net.coderbot.batchedentityrendering.impl.TransparencyType;
import net.coderbot.batchedentityrendering.impl.WrappableRenderType;
import net.minecraft.util.BlockRenderLayer;
//import net.minecraft.client.renderer.RenderType;

public class TranslucencyRenderOrderManager implements RenderOrderManager {
    private final EnumMap<TransparencyType, LinkedHashSet<BlockRenderLayer>> renderTypes;

    public TranslucencyRenderOrderManager() {
        renderTypes = new EnumMap<>(TransparencyType.class);

        for (TransparencyType type : TransparencyType.values()) {
            renderTypes.put(type, new LinkedHashSet<>());
        }
    }

    private static TransparencyType getTransparencyType(BlockRenderLayer type) {
        // todo: double-check
        // Default to "generally transparent" if we can't figure it out.
        return TransparencyType.GENERAL_TRANSPARENT;
    }

    public void begin(BlockRenderLayer type) {
        renderTypes.get(getTransparencyType(type)).add(type);
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
        renderTypes.forEach((type, set) -> {
            set.clear();
        });
    }

    public Iterable<BlockRenderLayer> getRenderOrder() {
        int layerCount = 0;

        for (LinkedHashSet<BlockRenderLayer> set : renderTypes.values()) {
            layerCount += set.size();
        }

        List<BlockRenderLayer> allRenderTypes = new ArrayList<>(layerCount);

        for (LinkedHashSet<BlockRenderLayer> set : renderTypes.values()) {
            allRenderTypes.addAll(set);
        }

        return allRenderTypes;
    }
}
