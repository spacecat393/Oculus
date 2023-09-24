package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.screen.HudHideable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ForgeGui.class)
public class MixinForgeGui {

    @Shadow
    public Minecraft getMinecraft() {
        return null;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void iris$handleHudHidingScreens(GuiGraphics pGui0, float pFloat1, CallbackInfo ci) {
        Screen screen = this.getMinecraft().screen;

        if (screen instanceof HudHideable) {
            ci.cancel();
        }
    }

    // TODO: Move this to a more appropriate mixin
    @Inject(method = "render", at = @At("RETURN"))
    public void iris$displayBigSodiumWarning(GuiGraphics guiGraphics, float pFloat1, CallbackInfo ci) {
        if (Iris.isSodiumInstalled()
                || Minecraft.getInstance().options.renderDebug
                || !Iris.getCurrentPack().isPresent()) {
            return;
        }

        Font font = Minecraft.getInstance().font;

        List<String> warningLines = new ArrayList<>();
        warningLines.add("[" + Iris.MODNAME + "] Rubidium isn't installed; you will have poor performance.");
        warningLines.add("[" + Iris.MODNAME + "] Install Rubidium if you want to run benchmarks or get higher FPS!");

        for (int i = 0; i < warningLines.size(); ++i) {
            String string = warningLines.get(i);

            final int lineHeight = 9;
            final int lineWidth = font.width(string);
            final int y = 2 + lineHeight * i;

            guiGraphics.fill(1, y - 1, 2 + lineWidth + 1, y + lineHeight - 1, 0x9050504E);
            guiGraphics.drawString(font, string, 2, y, 0xFFFF55);
        }
    }

}
