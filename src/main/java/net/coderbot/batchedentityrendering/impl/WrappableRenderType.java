package net.coderbot.batchedentityrendering.impl;

import net.minecraft.util.BlockRenderLayer;

public interface WrappableRenderType {
	/**
	 * Returns the underlying wrapped BlockRenderLayer. Might return itself if this BlockRenderLayer doesn't wrap anything.
	 */
	BlockRenderLayer unwrap();
}