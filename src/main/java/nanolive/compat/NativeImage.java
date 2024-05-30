package nanolive.compat;

import com.google.common.base.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NativeImage implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<StandardOpenOption> OPEN_OPTIONS;
    private final Format format;
    private final int width;
    private final int height;
    private final boolean useStbFree;
    public long pixels;
    private final long size;

    public NativeImage(int i, int j, boolean bl) {
        this(NativeImage.Format.RGBA, i, j, bl);
    }

    public NativeImage(Format arg, int i, int j, boolean bl) {
        this.format = arg;
        this.width = i;
        this.height = j;
        this.size = (long)i * (long)j * (long)arg.components();
        this.useStbFree = false;
        if (bl) {
            this.pixels = MemoryUtil.nmemCalloc(1L, this.size);
        } else {
            this.pixels = MemoryUtil.nmemAlloc(this.size);
        }

    }

    private NativeImage(Format arg, int i, int j, boolean bl, long l) {
        this.format = arg;
        this.width = i;
        this.height = j;
        this.useStbFree = bl;
        this.pixels = l;
        this.size = (long) i * j * arg.components();
    }

    @Override
    public String toString() {
        return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
    }

    public static NativeImage read(InputStream inputStream) throws IOException {
        return read(NativeImage.Format.RGBA, inputStream);
    }

    public static NativeImage read(@Nullable Format arg, InputStream inputStream) throws IOException {
        ByteBuffer bytebuffer = null;

        NativeImage nativeimage;
        try {
            bytebuffer = TextureUtil.readResource(inputStream);
            bytebuffer.rewind();
            nativeimage = read(arg, bytebuffer);
        } finally {
            MemoryUtil.memFree(bytebuffer);
            IOUtils.closeQuietly(inputStream);
        }

        return nativeimage;
    }

    public static NativeImage read(ByteBuffer byteBuffer) throws IOException {
        return read(NativeImage.Format.RGBA, byteBuffer);
    }

    public static NativeImage read(@Nullable Format arg, ByteBuffer byteBuffer) throws IOException {
        if (arg != null && !arg.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to read format " + arg);
        } else if (MemoryUtil.memAddress(byteBuffer) == 0L) {
            throw new IllegalArgumentException("Invalid buffer");
        } else {
            MemoryStack memorystack = MemoryStack.stackPush();
            Throwable var4 = null;

            NativeImage nativeimage;
            try {
                IntBuffer intbuffer = memorystack.mallocInt(1);
                IntBuffer intbuffer1 = memorystack.mallocInt(1);
                IntBuffer intbuffer2 = memorystack.mallocInt(1);
                ByteBuffer bytebuffer = STBImage.stbi_load_from_memory(byteBuffer, intbuffer, intbuffer1, intbuffer2, arg == null ? 0 : arg.components);
                if (bytebuffer == null) {
                    throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
                }

                nativeimage = new NativeImage(arg == null ? NativeImage.Format.getStbFormat(intbuffer2.get(0)) : arg, intbuffer.get(0), intbuffer1.get(0), true, MemoryUtil.memAddress(bytebuffer));
            } catch (Throwable var16) {
                var4 = var16;
                throw var16;
            } finally {
                if (memorystack != null) {
                    if (var4 != null) {
                        try {
                            memorystack.close();
                        } catch (Throwable var15) {
                            var4.addSuppressed(var15);
                        }
                    } else {
                        memorystack.close();
                    }
                }

            }

            return nativeimage;
        }
    }

    private static void setClamp(boolean bl) {
        if (bl) {
            GlStateManager.glTexParameteri(3553, 10242, 10496);
            GlStateManager.glTexParameteri(3553, 10243, 10496);
        } else {
            GlStateManager.glTexParameteri(3553, 10242, 10497);
            GlStateManager.glTexParameteri(3553, 10243, 10497);
        }

    }

    private static void setFilter(boolean bl, boolean bl2) {
        if (bl) {
            GlStateManager.glTexParameteri(3553, 10241, bl2 ? 9987 : 9729);
            GlStateManager.glTexParameteri(3553, 10240, 9729);
        } else {
            GlStateManager.glTexParameteri(3553, 10241, bl2 ? 9986 : 9728);
            GlStateManager.glTexParameteri(3553, 10240, 9728);
        }

    }

    private void checkAllocated() {
        if (this.pixels == 0L) {
            throw new IllegalStateException("Image is not allocated.");
        }
    }

    public void close() {
        if (this.pixels != 0L) {
            if (this.useStbFree) {
                STBImage.nstbi_image_free(this.pixels);
            } else {
                MemoryUtil.nmemFree(this.pixels);
            }
        }

        this.pixels = 0L;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Format format() {
        return this.format;
    }

    public int getPixelRGBA(int j, int k) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", this.format));
        } else if (j >= 0 && k >= 0 && j < this.width && k < this.height) {
            this.checkAllocated();
            long i = (j + (long) k * this.width) * 4;
            return CompatMemoryUtil.memGetInt(this.pixels + i);
        } else {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", j, k, this.width, this.height));
        }
    }

    public void setPixelRGBA(int j, int k, int l) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", this.format));
        } else if (j >= 0 && k >= 0 && j < this.width && k < this.height) {
            this.checkAllocated();
            long i = (j + (long) k * this.width) * 4;
            CompatMemoryUtil.memPutInt(this.pixels + i, l);
        } else {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", j, k, this.width, this.height));
        }
    }

    @Deprecated
    public int[] makePixelArray() {
        if (this.format != NativeImage.Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        } else {
            this.checkAllocated();
            int[] aint = new int[this.getWidth() * this.getHeight()];

            for(int i = 0; i < this.getHeight(); ++i) {
                for(int j = 0; j < this.getWidth(); ++j) {
                    int k = this.getPixelRGBA(j, i);
                    int l = getA(k);
                    int i1 = getB(k);
                    int j1 = getG(k);
                    int k1 = getR(k);
                    int l1 = l << 24 | k1 << 16 | j1 << 8 | i1;
                    aint[j + i * this.getWidth()] = l1;
                }
            }

            return aint;
        }
    }

    public void upload(int i, int j, int k, boolean bl) {
        this.upload(i, j, k, 0, 0, this.width, this.height, false, bl);
    }

    public void upload(int i, int j, int k, int l, int m, int n, int o, boolean bl, boolean bl2) {
        this.upload(i, j, k, l, m, n, o, false, false, bl, bl2);
    }

    public void upload(int i, int j, int k, int l, int m, int n, int o, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        this._upload(i, j, k, l, m, n, o, bl, bl2, bl3, bl4);
    }

    private void _upload(int i, int j, int k, int l, int m, int n, int o, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        this.checkAllocated();
        setFilter(bl, bl3);
        setClamp(bl2);
        if (n == this.getWidth()) {
            GlStateManager.glPixelStorei(3314, 0);
        } else {
            GlStateManager.glPixelStorei(3314, this.getWidth());
        }

        GlStateManager.glPixelStorei(3316, l);
        GlStateManager.glPixelStorei(3315, m);
        this.format.setUnpackPixelStoreState();
        GlStateManager.glTexSubImage2D(3553, i, j, k, n, o, this.format.glFormat(), 5121, this.pixels);
        if (bl4) {
            this.close();
        }

    }

    public void downloadTexture(int k, boolean bl) {
        this.checkAllocated();
        this.format.setPackPixelStoreState();
        GlStateManager._getTexImage(3553, k, this.format.glFormat(), 5121, this.pixels);
        if (bl && this.format.hasAlpha()) {
            for(int i = 0; i < this.getHeight(); ++i) {
                for(int j = 0; j < this.getWidth(); ++j) {
                    this.setPixelRGBA(j, i, this.getPixelRGBA(j, i) | 255 << this.format.alphaOffset());
                }
            }
        }

    }

    public void fillRect(int k, int l, int m, int n, int o) {
        for(int i = l; i < l + n; ++i) {
            for(int j = k; j < k + m; ++j) {
                this.setPixelRGBA(j, i, o);
            }
        }

    }

    public void copyRect(int m, int n, int o, int p, int q, int r, boolean bl, boolean bl2) {
        for(int i = 0; i < r; ++i) {
            for(int j = 0; j < q; ++j) {
                int k = bl ? q - 1 - j : j;
                int l = bl2 ? r - 1 - i : i;
                int i1 = this.getPixelRGBA(m + j, n + i);
                this.setPixelRGBA(m + o + k, n + p + l, i1);
            }
        }

    }

    public static int getA(int i) {
        return i >> 24 & 255;
    }

    public static int getR(int i) {
        return i & 255;
    }

    public static int getG(int i) {
        return i >> 8 & 255;
    }

    public static int getB(int i) {
        return i >> 16 & 255;
    }

    public static int combine(int i, int j, int k, int l) {
        return (i & 255) << 24 | (j & 255) << 16 | (k & 255) << 8 | (l & 255);
    }

    static {
        OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // @OnlyIn(Dist.CLIENT)
    public static enum InternalGlFormat {
        RGBA(6408),
        RGB(6407),
        LUMINANCE_ALPHA(6410),
        LUMINANCE(6409),
        INTENSITY(32841);

        private final int glFormat;

        private InternalGlFormat(int j) {
            this.glFormat = j;
        }

        int glFormat() {
            return this.glFormat;
        }
    }

    // @OnlyIn(Dist.CLIENT)
    public static enum Format {
        RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
        LUMINANCE_ALPHA(2, 6410, false, false, false, true, true, 255, 255, 255, 0, 8, true),
        LUMINANCE(1, 6409, false, false, false, true, false, 0, 0, 0, 0, 255, true);

        private final int components;
        private final int glFormat;
        private final boolean hasRed;
        private final boolean hasGreen;
        private final boolean hasBlue;
        private final boolean hasLuminance;
        private final boolean hasAlpha;
        private final int redOffset;
        private final int greenOffset;
        private final int blueOffset;
        private final int luminanceOffset;
        private final int alphaOffset;
        private final boolean supportedByStb;

        private Format(int j, int k, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, int l, int m, int n, int o, int p, boolean bl6) {
            this.components = j;
            this.glFormat = k;
            this.hasRed = bl;
            this.hasGreen = bl2;
            this.hasBlue = bl3;
            this.hasLuminance = bl4;
            this.hasAlpha = bl5;
            this.redOffset = l;
            this.greenOffset = m;
            this.blueOffset = n;
            this.luminanceOffset = o;
            this.alphaOffset = p;
            this.supportedByStb = bl6;
        }

        public int components() {
            return this.components;
        }

        public void setPackPixelStoreState() {
            GlStateManager.glPixelStorei(3333, this.components());
        }

        public void setUnpackPixelStoreState() {
            GlStateManager.glPixelStorei(3317, this.components());
        }

        public int glFormat() {
            return this.glFormat;
        }

        public boolean hasAlpha() {
            return this.hasAlpha;
        }

        public int alphaOffset() {
            return this.alphaOffset;
        }

        public boolean hasLuminanceOrAlpha() {
            return this.hasLuminance || this.hasAlpha;
        }

        public int luminanceOrAlphaOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
        }

        public boolean supportedByStb() {
            return this.supportedByStb;
        }

        private static Format getStbFormat(int i) {
            switch (i) {
                case 1:
                    return LUMINANCE;
                case 2:
                    return LUMINANCE_ALPHA;
                case 3:
                    return RGB;
                case 4:
                default:
                    return RGBA;
            }
        }
    }
}
