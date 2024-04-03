package net.coderbot.iris.texture.pbr;

import javax.annotation.Nullable;

public interface TextureAtlasExtension {
	@Nullable
	PBRAtlasHolder getPBRHolder();

	PBRAtlasHolder getOrCreatePBRHolder();
}
