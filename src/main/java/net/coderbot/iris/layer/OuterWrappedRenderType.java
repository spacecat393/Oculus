package net.coderbot.iris.layer;

import net.coderbot.batchedentityrendering.impl.CustomRenderType;
import net.coderbot.batchedentityrendering.impl.WrappableRenderType;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class OuterWrappedRenderType extends CustomRenderType implements WrappableRenderType {
	private final CustomRenderType wrapped;
	private final Runnable extraSetup;
	private final Runnable extraClear;

	public OuterWrappedRenderType(String name, CustomRenderType wrapped, Runnable extraSetup, Runnable extraClear) {
		super(name, wrapped.getFormat(), wrapped.getBufferSize(), wrapped.affectsCrumbling(), wrapped.sortOnUpload(), wrapped.getMode());
		this.wrapped = wrapped;
		this.extraSetup = extraSetup;
		this.extraClear = extraClear;
	}

	public static OuterWrappedRenderType wrapExactlyOnce(String name, CustomRenderType wrapped, Runnable extraSetup, Runnable extraClear) {
		if (wrapped instanceof OuterWrappedRenderType) {
			wrapped = ((OuterWrappedRenderType) wrapped).unwrap();
		}
		return new OuterWrappedRenderType(name, wrapped, extraSetup, extraClear);
	}

	@Override
	public void setupRenderState() {
		extraSetup.run();
		wrapped.setupRenderState();
	}

	@Override
	public void clearRenderState() {
		wrapped.clearRenderState();
		extraClear.run();
	}

	@Override
	public CustomRenderType unwrap() {
		return this.wrapped;
	}

	@Override
	public Optional<CustomRenderType> outline() {
		return this.wrapped.outline();
	}

	@Override
	public boolean isOutline() {
		return this.wrapped.isOutline();
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (object == null) {
			return false;
		}
		if (object.getClass() != this.getClass()) {
			return false;
		}
		OuterWrappedRenderType other = (OuterWrappedRenderType) object;
		return Objects.equals(this.wrapped, other.wrapped) && Objects.equals(this.extraSetup, other.extraSetup) && Objects.equals(this.extraClear, other.extraClear);
	}

	@Override
	public int hashCode() {
		return this.wrapped.hashCode() + 1;
	}

	@Override
	public String toString() {
		return "iris_wrapped:" + this.wrapped.toString();
	}
}
