package net.coderbot.iris.rendertarget;

import net.minecraft.client.renderer.texture.DynamicTexture;

import java.awt.image.BufferedImage;

public class BufferedImageBackedSingleColorTexture extends DynamicTexture {

	public BufferedImageBackedSingleColorTexture(int red, int green, int blue, int alpha) {
		super(create(BufferedImage.combine(alpha, blue, green, red)));
	}

	public BufferedImageBackedSingleColorTexture(int rgba) {
		this(rgba >> 24 & 0xFF, rgba >> 16 & 0xFF, rgba >> 8 & 0xFF, rgba & 0xFF);
	}

	private static BufferedImage create(int color) {
		BufferedImage image = new BufferedImage(BufferedImage.Format.RGBA, 1, 1, false);
		image.setPixelRGBA(0, 0, color);
		return image;
	}
}
