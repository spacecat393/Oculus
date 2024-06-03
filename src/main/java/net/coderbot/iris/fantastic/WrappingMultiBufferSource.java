package net.coderbot.iris.fantastic;

import net.minecraft.util.BlockRenderLayer;
import java.util.function.Function;

public interface WrappingMultiBufferSource {
	void pushWrappingFunction(Function<BlockRenderLayer, BlockRenderLayer> wrappingFunction);
	void popWrappingFunction();
	void assertWrapStackEmpty();
}
