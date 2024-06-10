package net.coderbot.iris.texture.mipmap;

import nanolive.compat.NativeImage;

import javax.annotation.Nullable;

public interface CustomMipmapGenerator {
	NativeImage[] generateMipLevels(NativeImage image, int mipLevel);

	public interface Provider {
		@Nullable
		CustomMipmapGenerator getMipmapGenerator(int width);
	}
}
