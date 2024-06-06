package net.coderbot.batchedentityrendering.impl.ordering;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import net.coderbot.batchedentityrendering.impl.BlendingStateHolder;
import net.coderbot.batchedentityrendering.impl.CustomRenderType;
import net.coderbot.batchedentityrendering.impl.TransparencyType;
import net.coderbot.batchedentityrendering.impl.WrappableRenderType;

public class TranslucencyRenderOrderManager implements RenderOrderManager {
    private final EnumMap<TransparencyType, LinkedHashSet<CustomRenderType>> renderTypes;

    public TranslucencyRenderOrderManager() {
        renderTypes = new EnumMap<>(TransparencyType.class);
        for (TransparencyType type : TransparencyType.values()) {
            renderTypes.put(type, new LinkedHashSet<>());
        }
    }

    private static TransparencyType getTransparencyType(CustomRenderType type) {
        while (type instanceof WrappableRenderType) {
            type = ((WrappableRenderType) type).unwrap();
        }
        if (type instanceof BlendingStateHolder) {
            return ((BlendingStateHolder) type).getTransparencyType();
        }
        return TransparencyType.GENERAL_TRANSPARENT;
    }

    public void begin(CustomRenderType type) {
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

    public Iterable<CustomRenderType> getRenderOrder() {
        int layerCount = 0;
        for (LinkedHashSet<CustomRenderType> set : renderTypes.values()) {
            layerCount += set.size();
        }
        List<CustomRenderType> allRenderTypes = new ArrayList<>(layerCount);
        for (LinkedHashSet<CustomRenderType> set : renderTypes.values()) {
            allRenderTypes.addAll(set);
        }
        return allRenderTypes;
    }
}