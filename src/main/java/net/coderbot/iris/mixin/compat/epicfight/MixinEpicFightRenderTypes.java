package net.coderbot.iris.mixin.compat.epicfight;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;

@Mixin(EpicFightRenderTypes.class)
public class MixinEpicFightRenderTypes {

    @Shadow
    private static RenderType enchantedAnimatedArmor() {
        return null;
    }

    @Inject(method = "getArmorVertexBuilder", at = @At("HEAD"), cancellable = true)
    private static void getArmorVertexBuilder(MultiBufferSource buffer, RenderType renderType, boolean withGlint, CallbackInfoReturnable<VertexConsumer> cir) {
        if(withGlint)
            cir.setReturnValue(buffer.getBuffer(enchantedAnimatedArmor()));
    }

}
