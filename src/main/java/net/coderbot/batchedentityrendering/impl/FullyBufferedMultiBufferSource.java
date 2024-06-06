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
import net.coderbot.batchedentityrendering.impl.ordering.GraphTranslucencyRenderOrderManager;
import net.coderbot.batchedentityrendering.impl.ordering.RenderOrderManager;
import net.coderbot.iris.fantastic.WrappingMultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.profiler.Profiler;

public class FullyBufferedMultiBufferSource implements MemoryTrackingBuffer, Groupable, WrappingMultiBufferSource {
	private static final int NUM_BUFFERS = 32;

	private final RenderOrderManager renderOrderManager;
	private final SegmentedBufferBuilder[] builders;
	/**
	 * An LRU cache mapping CustomRenderType objects to a relevant buffer.
	 */
	private final LinkedHashMap<CustomRenderType, Integer> affinities;
	@Getter
    private int drawCalls;
	@Getter
    private int renderTypes;

	private final BufferSegmentRenderer segmentRenderer;
	private final UnflushableWrapper unflushableWrapper;
	private final List<Function<CustomRenderType, CustomRenderType>> wrappingFunctionStack;
	private Function<CustomRenderType, CustomRenderType> wrappingFunction = null;

	public FullyBufferedMultiBufferSource() {
		super();

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

	public VertexBuffer getBuffer(CustomRenderType renderType) {
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
				Iterator<Map.Entry<CustomRenderType, Integer>> iterator = affinities.entrySet().iterator();
				Map.Entry<CustomRenderType, Integer> evicted = iterator.next();
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

		Map<CustomRenderType, List<BufferSegment>> typeToSegment = new HashMap<>();

		for (SegmentedBufferBuilder builder : builders) {
			List<BufferSegment> segments = builder.getSegments();

			for (BufferSegment segment : segments) {
				typeToSegment.computeIfAbsent(segment.getRenderType(), (type) -> new ArrayList<>()).add(segment);
			}
		}

		profiler.endStartSection("resolve ordering");

		Iterable<CustomRenderType> renderOrder = renderOrderManager.getRenderOrder();

		profiler.endStartSection("draw buffers");

		for (CustomRenderType type : renderOrder) {
			type.setupRenderState();

			renderTypes += 1;

			for (BufferSegment segment : typeToSegment.getOrDefault(type, Collections.emptyList())) {
				segmentRenderer.drawInner(segment);
				drawCalls += 1;
			}

			type.clearRenderState();
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

	public void endBatch(CustomRenderType type) {
		// Disable explicit flushing
	}

	public FullyBufferedMultiBufferSource getUnflushableWrapper() {
		return unflushableWrapper;
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

	@Override
	public void pushWrappingFunction(Function<CustomRenderType, CustomRenderType> wrappingFunction) {
		if (this.wrappingFunction != null) {
			this.wrappingFunctionStack.add(this.wrappingFunction);
		}

		this.wrappingFunction = wrappingFunction;
	}

	@Override
	public void popWrappingFunction() {
		if (this.wrappingFunctionStack.isEmpty()) {
			this.wrappingFunction = null;
		} else {
			this.wrappingFunction = this.wrappingFunctionStack.remove(this.wrappingFunctionStack.size() - 1);
		}
	}

	@Override
	public void assertWrapStackEmpty() {
		if (!this.wrappingFunctionStack.isEmpty() || this.wrappingFunction != null) {
			throw new IllegalStateException("Wrapping function stack not empty!");
		}
	}

	/**
	 * A wrapper that prevents callers from explicitly flushing anything.
	 */
	private static class UnflushableWrapper extends FullyBufferedMultiBufferSource implements Groupable {
		private final FullyBufferedMultiBufferSource wrapped;

		UnflushableWrapper(FullyBufferedMultiBufferSource wrapped) {
			super();

			this.wrapped = wrapped;
		}

		@Override
		public VertexBuffer getBuffer(CustomRenderType renderType) {
			return wrapped.getBuffer(renderType);
		}

		@Override
		public void endBatch() {
			// Disable explicit flushing
		}

		@Override
		public void endBatch(CustomRenderType type) {
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
