package net.coderbot.iris;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Nali
{
	public static IntBuffer OPENGL_INTBUFFER = ByteBuffer.allocateDirect(16 << 2).order(ByteOrder.nativeOrder()).asIntBuffer();

	public static void checkTextureInfo(int target)
	{
		GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D, OPENGL_INTBUFFER);
		int gl_texture_binding_2d = OPENGL_INTBUFFER.get(0);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, target);
		target = GL11.GL_TEXTURE_2D;
		int width = GL11.glGetTexLevelParameteri(target, 0, GL11.GL_TEXTURE_WIDTH);
		Iris.logger.info("Texture Width: " + width);

		int height = GL11.glGetTexLevelParameteri(target, 0, GL11.GL_TEXTURE_HEIGHT);
		Iris.logger.info("Texture Height: " + height);

		int internalFormat = GL11.glGetTexLevelParameteri(target, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);
		Iris.logger.info("Texture Internal Format: " + getFormatString(internalFormat));

		int minFilter = GL11.glGetTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER);
		Iris.logger.info("Texture Min Filter: " + getFilterString(minFilter));

		int magFilter = GL11.glGetTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER);
		Iris.logger.info("Texture Mag Filter: " + getFilterString(magFilter));

		int wrapS = GL11.glGetTexParameteri(target, GL11.GL_TEXTURE_WRAP_S);
		Iris.logger.info("Texture Wrap S: " + getWrapString(wrapS));

		int wrapT = GL11.glGetTexParameteri(target, GL11.GL_TEXTURE_WRAP_T);
		Iris.logger.info("Texture Wrap T: " + getWrapString(wrapT));

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, gl_texture_binding_2d);
	}

	public static String getFormatString(int format)
	{
		switch (format)
		{
			case GL11.GL_RGB:
				return "GL_RGB";
			case GL11.GL_RGBA:
				return "GL_RGBA";
			case GL11.GL_RGBA8:
				return "GL_RGBA8";
			case GL11.GL_RGB8:
				return "GL_RGB8";
			case GL11.GL_DEPTH_COMPONENT:
				return "GL_DEPTH_COMPONENT";
			case GL14.GL_DEPTH_COMPONENT24:
				return "GL_DEPTH_COMPONENT24";
			default:
//				Iris.logger.info("Texture Internal Format: Unknown Format: " + format);
//				FMLCommonHandler.instance().exitJava(-1, true);
				return "Unknown Format: " + format;
		}
	}

	public static String getFilterString(int filter)
	{
		switch (filter)
		{
			case GL11.GL_NEAREST:
				return "GL_NEAREST";
			case GL11.GL_LINEAR:
				return "GL_LINEAR";
			case GL11.GL_NEAREST_MIPMAP_NEAREST:
				return "GL_NEAREST_MIPMAP_NEAREST";
			case GL11.GL_LINEAR_MIPMAP_NEAREST:
				return "GL_LINEAR_MIPMAP_NEAREST";
			case GL11.GL_NEAREST_MIPMAP_LINEAR:
				return "GL_NEAREST_MIPMAP_LINEAR";
			case GL11.GL_LINEAR_MIPMAP_LINEAR:
				return "GL_LINEAR_MIPMAP_LINEAR";
			default:
				return "Unknown Filter: " + filter;
		}
	}

	public static String getWrapString(int wrap)
	{
		switch (wrap)
		{
			case GL11.GL_REPEAT:
				return "GL_REPEAT";
			case GL12.GL_CLAMP_TO_EDGE:
				return "GL_CLAMP_TO_EDGE";
			case GL14.GL_MIRRORED_REPEAT:
				return "GL_MIRRORED_REPEAT";
			default:
				return "Unknown Wrap Mode: " + wrap;
		}
	}
}
