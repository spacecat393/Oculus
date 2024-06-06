package net.coderbot.iris.fantastic;

import net.coderbot.batchedentityrendering.impl.CustomRenderType;
import java.util.function.Function;

public interface WrappingMultiBufferSource {
	void pushWrappingFunction(Function<CustomRenderType, CustomRenderType> wrappingFunction);
	void popWrappingFunction();
	void assertWrapStackEmpty();
}
