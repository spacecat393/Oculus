package net.coderbot.iris.compat.sodium.mixin.fast_render;

import me.jellysquid.mods.sodium.client.model.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ModelPart.Cube.class)
public class MixinCube implements ModelCuboidAccessor {
    private ModelCuboid sodium$cuboid;
	
	@Inject(method = "<init>", at = @At(value = "TAIL"))
    private void onInit(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight, Set<Direction> renderDirections, CallbackInfo ci) {
        if(!mirror)
        	x += extraX;
        else {
        	x -= extraX;
        	x -= sizeX;
        }
    	y += extraY;
        z += extraZ;
    	this.sodium$cuboid = new ModelCuboid(u, v, x, y, z, sizeX, sizeY, sizeZ, extraX, extraY, extraZ, mirror, textureWidth, textureHeight, renderDirections);
    }

    @Override
    public ModelCuboid copy() {
        return this.sodium$cuboid;
    }
}
