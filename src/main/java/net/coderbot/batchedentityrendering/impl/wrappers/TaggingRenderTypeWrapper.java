package net.coderbot.batchedentityrendering.impl.wrappers;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.coderbot.batchedentityrendering.impl.CustomRenderType;
import net.coderbot.batchedentityrendering.impl.WrappableRenderType;

public class TaggingRenderTypeWrapper extends CustomRenderType implements WrappableRenderType {
    private final int tag;
    private final CustomRenderType wrapped;

    public TaggingRenderTypeWrapper(String name, CustomRenderType wrapped, int tag) {
        super(name, wrapped.getFormat(), wrapped.getBufferSize(), wrapped.affectsCrumbling(), wrapped.sortOnUpload());
        this.tag = tag;
        this.wrapped = wrapped;
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
        TaggingRenderTypeWrapper other = (TaggingRenderTypeWrapper) object;
        return this.tag == other.tag && Objects.equals(this.wrapped, other.wrapped);
    }

    @Override
    public int hashCode() {
        return this.wrapped.hashCode() + 1;
    }

    @Override
    public String toString() {
        return "tagged(" + tag + "):" + this.wrapped.toString();
    }

    @Override
    public void setupRenderState() {
        wrapped.setupRenderState();
    }

    @Override
    public void clearRenderState() {
        wrapped.clearRenderState();
    }
}
