package net.coderbot.iris.gl.framebuffer;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.texture.TextureInfoCache;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

import static net.coderbot.iris.Nali.OPENGL_INTBUFFER;

public class GlFramebuffer extends GlResource {
	private final Int2IntMap attachments;
	private final int maxDrawBuffers;
	private final int maxColorAttachments;
	private boolean hasDepthAttachment;

	public GlFramebuffer() {
		super(IrisRenderSystem.createFramebuffer());
//		super(OpenGlHelper.glGenFramebuffers());

		this.attachments = new Int2IntArrayMap();
		this.maxDrawBuffers = GlStateManager.glGetInteger(GL20.GL_MAX_DRAW_BUFFERS);
		this.maxColorAttachments = GlStateManager.glGetInteger(GL30.GL_MAX_COLOR_ATTACHMENTS);
		this.hasDepthAttachment = false;
	}

	public void addDepthAttachment(int texture) {
//		Iris.logger.info("t " + texture);
//		Nali.checkTextureInfo(texture);
		int internalFormat = TextureInfoCache.INSTANCE.getInfo(texture).getInternalFormat();
		DepthBufferFormat depthBufferFormat = DepthBufferFormat.fromGlEnumOrDefault(internalFormat);

		int fb = getGlId();

		if (depthBufferFormat.isCombinedStencil()) {
			IrisRenderSystem.framebufferTexture2D(fb, OpenGlHelper.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, texture, 0);
		} else {
			if (internalFormat == GL11.GL_RGBA)
			{
				GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D, OPENGL_INTBUFFER);
				int gl_texture_binding_2d = OPENGL_INTBUFFER.get(0);

				GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
				int target = GL11.GL_TEXTURE_2D;
				int width = GL11.glGetTexLevelParameteri(target, 0, GL11.GL_TEXTURE_WIDTH);
				int height = GL11.glGetTexLevelParameteri(target, 0, GL11.GL_TEXTURE_HEIGHT);

				texture = GL11.glGenTextures();
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);

				GL11.glBindTexture(GL11.GL_TEXTURE_2D, gl_texture_binding_2d);
			}
			IrisRenderSystem.framebufferTexture2D(fb, OpenGlHelper.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, texture, 0);
		}

		this.hasDepthAttachment = true;
	}

	public void addColorAttachment(int index, int texture) {
		int fb = getGlId();

//		Iris.logger.info("t " + texture);
//		Nali.checkTextureInfo(texture);

		IrisRenderSystem.framebufferTexture2D(fb, OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0 + index, GL11.GL_TEXTURE_2D, texture, 0);
		attachments.put(index, texture);
	}

	public void noDrawBuffers() {
		IrisRenderSystem.drawBuffers(getGlId(), new int[] { GL11.GL_NONE });
	}

	public void drawBuffers(int[] buffers) {
		int[] glBuffers = new int[buffers.length];
		int index = 0;

		if (buffers.length > maxDrawBuffers) {
			throw new IllegalArgumentException("Cannot write to more than " + maxDrawBuffers + " draw buffers on this GPU");
		}

		for (int buffer : buffers) {
			if (buffer >= maxColorAttachments) {
				throw new IllegalArgumentException("Only " + maxColorAttachments + " color attachments are supported on this GPU, but an attempt was made to write to a color attachment with index " + buffer);
			}

			glBuffers[index++] = OpenGlHelper.GL_COLOR_ATTACHMENT0 + buffer;
		}

		IrisRenderSystem.drawBuffers(getGlId(), glBuffers);
	}

	public void readBuffer(int buffer) {
		IrisRenderSystem.readBuffer(getGlId(), OpenGlHelper.GL_COLOR_ATTACHMENT0 + buffer);
	}

	public int getColorAttachment(int index) {
		return attachments.get(index);
	}

	public boolean hasDepthAttachment() {
		return hasDepthAttachment;
	}

	public void bind() {
		OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, getGlId());
	}

	public void bindAsReadBuffer() {
		OpenGlHelper.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, getGlId());
	}

	public void bindAsDrawBuffer() {
		OpenGlHelper.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, getGlId());
	}

	protected void destroyInternal() {
		OpenGlHelper.glDeleteFramebuffers(getGlId());
	}

	public boolean isComplete() {
		bind();
		int status = OpenGlHelper.glCheckFramebufferStatus(OpenGlHelper.GL_FRAMEBUFFER);

		return status == OpenGlHelper.GL_FRAMEBUFFER_COMPLETE;
	}

	public int getId() {
		return getGlId();
	}
}
