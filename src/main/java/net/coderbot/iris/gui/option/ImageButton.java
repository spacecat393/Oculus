package net.coderbot.iris.gui.option;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ImageButton extends GuiButton {
    private final ResourceLocation image;
    private final int textureX;
    private final int textureY;
    private final int hoverX;
    private final int hoverY;

    public ImageButton(int buttonId, int x, int y, int widthIn, int heightIn, int textureX, int textureY, int hoverX, int hoverY, ResourceLocation image) {
        super(buttonId, x, y, widthIn, heightIn, "");
        this.image = image;
        this.textureX = textureX;
        this.textureY = textureY;
        this.hoverX = hoverX;
        this.hoverY = hoverY;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(image);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int drawX = hovered ? hoverX : textureX;
            this.drawTexturedModalRect(this.x, this.y, drawX, textureY, this.width, this.height);
        }
    }
}
