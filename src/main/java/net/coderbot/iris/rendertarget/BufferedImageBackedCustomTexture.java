package net.coderbot.iris.rendertarget;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;

@Getter
public class BufferedImageBackedCustomTexture extends DynamicTexture {
	private final int width;
	private final int height;
	public BufferedImageBackedCustomTexture(CustomTextureData.PngData textureData) throws IOException {
		super(create(textureData.getContent()));
		BufferedImage image = create(textureData.getContent());
		width = image.getWidth();
		height = image.getHeight();

		// By default, images are unblurred and not clamped.

		if (textureData.getFilteringData().shouldBlur()) {
			IrisRenderSystem.texParameteri(getGlTextureId(), GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			IrisRenderSystem.texParameteri(getGlTextureId(), GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		}

		if (textureData.getFilteringData().shouldClamp()) {
			IrisRenderSystem.texParameteri(getGlTextureId(), GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			IrisRenderSystem.texParameteri(getGlTextureId(), GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		}
	}

	private static BufferedImage create(byte[] content) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        return ImageIO.read(inputStream);
	}

	@Override
	public void updateDynamicTexture() {
		// create the image
		int[] textureData = this.getTextureData();
		int width = this.getWidth();
		int height = this.getHeight();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, width, height, textureData, 0, width);

		// bind
		GlStateManager.bindTexture(this.getGlTextureId());

		int[] pixels = new int[width * height];
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * width * height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = pixels[y * width + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
				buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
				buffer.put((byte) (pixel & 0xFF));         // Blue
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
			}
		}
		buffer.flip();

		// upload
		// image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false, false);
		// private void _upload(int 0, int 0, int 0, int 0, int 0, int p_85096_, int p_85097_, boolean p_85098_, boolean p_85099_, boolean p_85100_, boolean p_85101_) {
		//        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		//        this.checkAllocated();
		//        setFilter(p_85098_, p_85100_);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		//        if (p_85096_ == this.getWidth()) {
		//            GlStateManager._pixelStore(3314, 0);
		GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
		//        } else {
		//            GlStateManager._pixelStore(3314, this.getWidth());
		//        }
		//
		//        GlStateManager._pixelStore(3316, p_85094_);
		//        GlStateManager._pixelStore(3315, p_85095_);
		GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
		GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
		//        this.format.setUnpackPixelStoreState();
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
		//        GlStateManager._texSubImage2D(3553, p_85091_, p_85092_, p_85093_, p_85096_, p_85097_, this.format.glFormat(), 5121, this.pixels);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		//        if (p_85099_) {
		//            GlStateManager._texParameter(3553, 10242, 33071);
		//            GlStateManager._texParameter(3553, 10243, 33071);
		//        }
		//
		//        if (p_85101_) {
		//            this.close();
		//        }
		//
		//    }
	}
}
