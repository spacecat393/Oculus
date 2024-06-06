package net.coderbot.iris.mixin.rendertype;

import net.coderbot.batchedentityrendering.impl.CustomRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomRenderType.class)
public interface RenderTypeAccessor {
    @Accessor("sortOnUpload")
    boolean shouldSortOnUpload();
}
