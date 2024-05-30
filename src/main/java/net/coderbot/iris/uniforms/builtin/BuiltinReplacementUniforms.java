package net.coderbot.iris.uniforms.builtin;

//import com.mojang.math.Matrix4f;
import net.coderbot.iris.vendored.joml.Matrix4f;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;

public class BuiltinReplacementUniforms {
	private static final Matrix4f lightmapTextureMatrix;

	static {
		// This mimics the transformations done in LightmapTextureManager to the GL_TEXTURE matrix.
		lightmapTextureMatrix = new Matrix4f();
//		lightmapTextureMatrix.setIdentity();
		lightmapTextureMatrix.identity();
		float value = 0.00390625f;
		Matrix4f tempMatrix = new Matrix4f(
				value, value, value, value,
				value, value, value, value,
				value, value, value, value,
				value, value, value, value
		);
//		lightmapTextureMatrix.multiply(0.00390625f);
		lightmapTextureMatrix.mul(tempMatrix);
//		lightmapTextureMatrix.multiply(Matrix4f.createTranslateMatrix(8.0f, 8.0f, 8.0f));
		lightmapTextureMatrix.translate(8.0f, 8.0f, 8.0f);
	}

	public static void addBuiltinReplacementUniforms(UniformHolder uniforms) {
		uniforms.uniformMatrix(UniformUpdateFrequency.ONCE, "iris_LightmapTextureMatrix", () -> {
			Iris.logger.warn("A shader appears to require the lightmap texture matrix even after transformations have occurred");
			Iris.logger.warn("Iris handles this correctly but it indicates that the shader is doing weird things with lightmap coordinates");

			return lightmapTextureMatrix;
		});
	}
}
