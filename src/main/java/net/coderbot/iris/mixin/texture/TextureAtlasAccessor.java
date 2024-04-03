package net.coderbot.iris.mixin.texture;

import java.util.Map;

import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Mixin(TextureMap.class)
public interface TextureAtlasAccessor {
	@Accessor("mapUploadedSprites")
	Map<String, TextureAtlasSprite> getTexturesByName();

	@Invoker("getResourceLocation")
	ResourceLocation callGetResourceLocation(ResourceLocation location);
}
