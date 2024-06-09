package net.coderbot.iris.mixin.fantastic;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import org.spongepowered.asm.mixin.Mixin;

//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.particle.ParticleRenderType;
//import net.minecraft.client.particle.SimpleAnimatedParticle;
//import net.minecraft.client.particle.SpriteSet;

@Mixin(targets = "net.minecraft.client.particle.ParticleFirework$Spark")
public class MixinFireworkSparkParticle extends ParticleSimpleAnimated {
	private MixinFireworkSparkParticle(WorldClient world, double x, double y, double z, int texture, int frames, float upwardsAcceleration) {
		// ParticleSimpleAnimated(World worldIn, double x, double y, double z, int textureIdxIn, int numFrames, float yAccelIn)
		super(world, x, y, z, texture, frames, upwardsAcceleration);
	}

//	@Override
//	public ParticleRenderType getRenderType() {
//		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
//	}
}
