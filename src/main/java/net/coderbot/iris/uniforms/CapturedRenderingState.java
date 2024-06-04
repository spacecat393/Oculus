package net.coderbot.iris.uniforms;

import lombok.Getter;
import lombok.Setter;
import net.coderbot.iris.vendored.joml.Matrix4f;

import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.minecraft.client.Minecraft;

public class CapturedRenderingState {
	public static final CapturedRenderingState INSTANCE = new CapturedRenderingState();

	private static final Vector3d ZERO_VECTOR_3d = new Vector3d();

	@Getter
    private Matrix4f gbufferModelView;
	@Getter
    private Matrix4f gbufferProjection;
	private Vector3d fogColor;
	@Getter
    @Setter
    private float tickDelta;
	@Getter
    private int currentRenderedBlockEntity;
	private Runnable blockEntityIdListener = null;

	@Getter
    private int currentRenderedEntity = -1;
	private Runnable entityIdListener = null;

	private CapturedRenderingState() {

	}

    public void setGbufferModelView(Matrix4f gbufferModelView) {
		this.gbufferModelView = new Matrix4f(gbufferModelView);
	}

    public void setGbufferProjection(Matrix4f gbufferProjection) {
		this.gbufferProjection = new Matrix4f(gbufferProjection);
	}

	public Vector3d getFogColor() {
		if (Minecraft.getMinecraft().world == null || fogColor == null) {
			return ZERO_VECTOR_3d;
		}

		return fogColor;
	}

	public void setFogColor(float red, float green, float blue) {
		fogColor = new Vector3d(red, green, blue);
	}

    public void setCurrentBlockEntity(int entity) {
		this.currentRenderedBlockEntity = entity;

		if (this.blockEntityIdListener != null) {
			this.blockEntityIdListener.run();
		}
	}

    public void setCurrentEntity(int entity) {
		this.currentRenderedEntity = entity;

		if (this.entityIdListener != null) {
			this.entityIdListener.run();
		}
	}

	public ValueUpdateNotifier getEntityIdNotifier() {
		return listener -> this.entityIdListener = listener;
	}

	public ValueUpdateNotifier getBlockEntityIdNotifier() {
		return listener -> this.blockEntityIdListener = listener;
	}
}
