package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.BlendingStateHolder;
import net.coderbot.batchedentityrendering.impl.CustomRenderType;
import net.coderbot.batchedentityrendering.impl.TransparencyType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomRenderType.class)
public class MixinRenderType implements BlendingStateHolder {
    // Fallback
    @Override
    public TransparencyType getTransparencyType() {
        return TransparencyType.GENERAL_TRANSPARENT;
    }
}