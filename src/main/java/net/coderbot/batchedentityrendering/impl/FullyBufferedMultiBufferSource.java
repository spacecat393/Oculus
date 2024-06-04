package net.coderbot.batchedentityrendering.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.Getter;
import net.minecraft.client.renderer.BufferBuilder;

import net.coderbot.batchedentityrendering.impl.ordering.GraphTranslucencyRenderOrderManager;
import net.coderbot.batchedentityrendering.impl.ordering.RenderOrderManager;
import net.coderbot.iris.fantastic.WrappingMultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.profiler.Profiler;

public class FullyBufferedMultiBufferSource implements MemoryTrackingBuffer, Groupable, WrappingMultiBufferSource {
	private static final int NUM_BUFFERS = 32;

	private final RenderOrderManager renderOrderManager;
	private final SegmentedBufferBuilder[] builders;
	/**
	 * An LRU cache mapping RenderType objects to a relevant buffer.
	 */
	private final LinkedHashMap<BlockRenderLayer, Integer> affinities;
	@Getter
    private int drawCalls;
	@Getter
    private int renderTypes;

	private final BufferSegmentRenderer segmentRenderer;
	@Getter
    private final UnflushableWrapper unflushableWrapper;
	private final List<Function<BlockRenderLayer, BlockRenderLayer>> wrappingFunctionStack;
	private Function<BlockRenderLayer, BlockRenderLayer> wrappingFunction = null;

	public FullyBufferedMultiBufferSource() {
		this.renderOrderManager = new GraphTranslucencyRenderOrderManager();
		this.builders = new SegmentedBufferBuilder[NUM_BUFFERS];

		for (int i = 0; i < this.builders.length; i++) {
			this.builders[i] = new SegmentedBufferBuilder();
		}

		// use accessOrder=true so our LinkedHashMap works as an LRU cache.
		this.affinities = new LinkedHashMap<>(32, 0.75F, true);

		this.drawCalls = 0;
		this.segmentRenderer = new BufferSegmentRenderer();
		this.unflushableWrapper = new UnflushableWrapper(this);
		this.wrappingFunctionStack = new ArrayList<>();
	}

	public BufferBuilder getBuffer(BlockRenderLayer renderType) {
		if (wrappingFunction != null) {
			renderType = wrappingFunction.apply(renderType);
		}

		renderOrderManager.begin(renderType);
		Integer affinity = affinities.get(renderType);

		if (affinity == null) {
			if (affinities.size() < builders.length) {
				affinity = affinities.size();
			} else {
				// We remove the element from the map that is used least-frequently.
				// With how we've configured our LinkedHashMap, that is the first element.
				Iterator<Map.Entry<BlockRenderLayer, Integer>> iterator = affinities.entrySet().iterator();
				Map.Entry<BlockRenderLayer, Integer> evicted = iterator.next();
				iterator.remove();

				// The previous type is no longer associated with this buffer ...
				affinities.remove(evicted.getKey());

				// ... since our new type is now associated with it.
				affinity = evicted.getValue();
			}

			affinities.put(renderType, affinity);
		}

		return builders[affinity].getBuffer(renderType);
	}

	public void endBatch() {
		Profiler profiler = Minecraft.getMinecraft().profiler;

		profiler.startSection("collect");

		Map<BlockRenderLayer, List<BufferSegment>> layerToSegment = new HashMap<>();

		for (SegmentedBufferBuilder builder : builders) {
			List<BufferSegment> segments = builder.getSegments();
			for (BufferSegment segment : segments) {
				layerToSegment.computeIfAbsent(segment.getRenderLayer(), (type) -> new ArrayList<>()).add(segment);
			}
		}

		profiler.endStartSection("resolve ordering");

		Iterable<BlockRenderLayer> renderOrder = renderOrderManager.getRenderOrder();

		profiler.endStartSection("draw buffers");

		for (BlockRenderLayer layer : renderOrder) {
			renderTypes += 1;

			for (BufferSegment segment : layerToSegment.getOrDefault(layer, Collections.emptyList())) {
				segmentRenderer.drawInner(segment);
				drawCalls += 1;
			}
		}

		profiler.endStartSection("reset");

		renderOrderManager.reset();
		affinities.clear();

		profiler.endSection();
	}

    public void resetDrawCalls() {
		drawCalls = 0;
		renderTypes = 0;
	}

	public void endBatch(BlockRenderLayer type) {
		renderOrderManager.endGroup();
	}

    @Override
	public int getAllocatedSize() {
		int size = 0;

		for (SegmentedBufferBuilder builder : builders) {
			size += builder.getAllocatedSize();
		}

		return size;
	}

	@Override
	public int getUsedSize() {
		int size = 0;

		for (SegmentedBufferBuilder builder : builders) {
			size += builder.getUsedSize();
		}

		return size;
	}

	@Override
	public void startGroup() {
		renderOrderManager.startGroup();
	}

	@Override
	public boolean maybeStartGroup() {
		return renderOrderManager.maybeStartGroup();
	}

	@Override
	public void endGroup() {
		renderOrderManager.endGroup();
	}

	public void pushWrappingFunction(Function<BlockRenderLayer, BlockRenderLayer> wrappingFunction) {
		if (this.wrappingFunction != null) {
			this.wrappingFunctionStack.add(this.wrappingFunction);
		}

		this.wrappingFunction = wrappingFunction;
	}

	public void popWrappingFunction() {
		if (this.wrappingFunctionStack.isEmpty()) {
			this.wrappingFunction = null;
		} else {
			this.wrappingFunction = this.wrappingFunctionStack.remove(this.wrappingFunctionStack.size() - 1);
		}
	}

	public void assertWrapStackEmpty() {
		if (!this.wrappingFunctionStack.isEmpty() || this.wrappingFunction != null) {
			throw new IllegalStateException("Wrapping function stack not empty!");
		}
	}

	/**
	 * A wrapper that prevents callers from explicitly flushing anything.
	 */
	private static class UnflushableWrapper extends BufferBuilder implements Groupable {
		private final FullyBufferedMultiBufferSource wrapped;

		UnflushableWrapper(FullyBufferedMultiBufferSource wrapped) {
			super(0);
			this.wrapped = wrapped;
		}

		public BufferBuilder getBuffer(BlockRenderLayer renderLayer) {
			return wrapped.getBuffer(renderLayer);
		}

		public void endBatch() {
			// Disable explicit flushing
		}

		public void endBatch(BlockRenderLayer type) {
			// Disable explicit flushing
		}

		@Override
		public void startGroup() {
			wrapped.startGroup();
		}

		@Override
		public boolean maybeStartGroup() {
			return wrapped.maybeStartGroup();
		}

		@Override
		public void endGroup() {
			wrapped.endGroup();
		}
	}
}
