package net.coderbot.batchedentityrendering.impl.ordering;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.MapDigraph;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSet;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetPolicy;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetProvider;
import de.odysseus.ithaka.digraph.util.fas.SimpleFeedbackArcSetProvider;
import net.coderbot.batchedentityrendering.impl.BlendingStateHolder;
import net.coderbot.batchedentityrendering.impl.CustomRenderType;
import net.coderbot.batchedentityrendering.impl.TransparencyType;
import net.coderbot.batchedentityrendering.impl.WrappableRenderType;
import net.minecraft.util.BlockRenderLayer;

public class GraphTranslucencyRenderOrderManager implements RenderOrderManager {
    private final FeedbackArcSetProvider feedbackArcSetProvider;
    private final EnumMap<TransparencyType, Digraph<CustomRenderType>> types;
    private boolean inGroup = false;
    private final EnumMap<TransparencyType, CustomRenderType> currentTypes;

    public GraphTranslucencyRenderOrderManager() {
        feedbackArcSetProvider = new SimpleFeedbackArcSetProvider();
        types = new EnumMap<>(TransparencyType.class);
        currentTypes = new EnumMap<>(TransparencyType.class);

        for (TransparencyType type : TransparencyType.values()) {
            types.put(type, new MapDigraph<>());
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

    public void begin(CustomRenderType renderType) {
        TransparencyType transparencyType = getTransparencyType(renderType);
        Digraph<CustomRenderType> graph = types.get(transparencyType);
        graph.add(renderType);

        if (inGroup) {
            CustomRenderType previous = currentTypes.put(transparencyType, renderType);
            if (previous == null) {
                return;
            }
            int weight = graph.get(previous, renderType).orElse(0);
            weight += 1;
            graph.put(previous, renderType, weight);
        }
    }

    public void startGroup() {
        if (inGroup) {
            throw new IllegalStateException("Already in a group");
        }
        currentTypes.clear();
        inGroup = true;
    }

    public boolean maybeStartGroup() {
        if (inGroup) {
            return false;
        }
        currentTypes.clear();
        inGroup = true;
        return true;
    }

    public void endGroup() {
        if (!inGroup) {
            throw new IllegalStateException("Not in a group");
        }
        currentTypes.clear();
        inGroup = false;
    }

    @Override
    public void reset() {
        types.clear();
        for (TransparencyType type : TransparencyType.values()) {
            types.put(type, new MapDigraph<>());
        }
    }

    public Iterable<CustomRenderType> getRenderOrder() {
        int layerCount = 0;
        for (Digraph<CustomRenderType> graph : types.values()) {
            layerCount += graph.getVertexCount();
        }
        List<CustomRenderType> allLayers = new ArrayList<>(layerCount);
        for (Digraph<CustomRenderType> graph : types.values()) {
            FeedbackArcSet<CustomRenderType> arcSet = feedbackArcSetProvider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_WEIGHT);
            if (arcSet.getEdgeCount() > 0) {
                for (CustomRenderType source : arcSet.vertices()) {
                    for (CustomRenderType target : arcSet.targets(source)) {
                        graph.remove(source, target);
                    }
                }
            }
            allLayers.addAll(Digraphs.toposort(graph, false));
        }
        return allLayers;
    }
}