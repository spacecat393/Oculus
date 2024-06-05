package net.coderbot.batchedentityrendering.impl.ordering;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
//import java.util.List;

//import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.MapDigraph;
//import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSet;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSet;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetPolicy;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetProvider;
import de.odysseus.ithaka.digraph.util.fas.SimpleFeedbackArcSetProvider;
import net.coderbot.batchedentityrendering.impl.BlendingStateHolder;
import net.coderbot.batchedentityrendering.impl.TransparencyType;
import net.coderbot.batchedentityrendering.impl.WrappableRenderType;
import net.minecraft.util.BlockRenderLayer;
//import net.minecraft.client.renderer.RenderType;

public class GraphTranslucencyRenderOrderManager implements RenderOrderManager {
    private final FeedbackArcSetProvider feedbackArcSetProvider;
    private final EnumMap<TransparencyType, MapDigraph<BlockRenderLayer>> types;

    private boolean inGroup = false;
    private final EnumMap<TransparencyType, BlockRenderLayer> currentTypes;

    public GraphTranslucencyRenderOrderManager() {
        feedbackArcSetProvider = new SimpleFeedbackArcSetProvider();
        types = new EnumMap<>(TransparencyType.class);
        currentTypes = new EnumMap<>(TransparencyType.class);

        for (TransparencyType type : TransparencyType.values()) {
            types.put(type, new MapDigraph<>());
        }
    }

    private static TransparencyType getTransparencyType(BlockRenderLayer type) {
        // todo: double-check
        // Default to "generally transparent" if we can't figure it out.
        return TransparencyType.GENERAL_TRANSPARENT;
    }

    @Override
    public void begin(BlockRenderLayer renderType) {
        TransparencyType transparencyType = getTransparencyType(renderType);
        MapDigraph<BlockRenderLayer> graph = types.get(transparencyType);
        graph.add(renderType);

        if (inGroup) {
            BlockRenderLayer previous = currentTypes.put(transparencyType, renderType);

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
        // TODO: Is reallocation efficient?
        types.clear();

        for (TransparencyType type : TransparencyType.values()) {
            types.put(type, new MapDigraph<>());
        }
    }

    public Iterable<BlockRenderLayer> getRenderOrder() {
        int layerCount = 0;
        for (MapDigraph<BlockRenderLayer> graph : types.values()) {
            layerCount += graph.getVertexCount();
        }
        List<BlockRenderLayer> allLayers = new ArrayList<>(layerCount);
        for (MapDigraph<BlockRenderLayer> graph : types.values()) {
            FeedbackArcSet<BlockRenderLayer> arcSet =
                    feedbackArcSetProvider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_WEIGHT);
            if (arcSet.getEdgeCount() > 0) {
                for (BlockRenderLayer source : arcSet.vertices()) {
                    for (BlockRenderLayer target : arcSet.targets(source)) {
                        graph.remove(source, target);
                    }
                }
            }
            allLayers.addAll(Digraphs.toposort(graph, false));
        }
        return allLayers;
    }
}
