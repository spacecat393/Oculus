package net.coderbot.iris.mixin.compat.pixelmon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pixelmonmod.pixelmon.client.models.smd.DeformVertex;
import com.pixelmonmod.pixelmon.client.models.smd.NormalizedFace;
import com.pixelmonmod.pixelmon.client.models.smd.TextureCoordinate;
import com.pixelmonmod.pixelmon.client.models.smd.Vertex;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = NormalizedFace.class, remap = false)
public class MixinNormalizedFace {

    @Shadow
    public DeformVertex[] vertices;

    @Shadow
    public TextureCoordinate[] textureCoordinates;

    @Shadow
    public Vertex faceNormal;

    @Shadow
    public Vertex calculateFaceNormal() {
        return null;
    }

    /**
     * @author Asek3
     * @reason We should deal with this...
     */
    @Overwrite
    public void addFaceForRender(PoseStack matrixStack, VertexConsumer bufferBuilder, int packedLight, int packedOverlay, boolean smoothShading, float partialTick, float r, float g, float b, float a) {
        if (!smoothShading && this.faceNormal == null)
            this.faceNormal = calculateFaceNormal();
        for (int i = 0; i < 3; i++) {
            Matrix4f pose = matrixStack.last().pose();
            Matrix3f normal = matrixStack.last().normal();
            DeformVertex vertex = this.vertices[i];
            Vector4f transformedPosition = pose.transform(new Vector4f(vertex.getX(partialTick), vertex.getY(partialTick), vertex.getZ(partialTick), 1.0F));
            Vector3f transformedNormal = normal.transform(new Vector3f(vertex.getXN(partialTick), vertex.getYN(partialTick), vertex.getZN(partialTick)));
            bufferBuilder.vertex(transformedPosition.x(), transformedPosition.y(), transformedPosition.z(), r, g, b, a, this.textureCoordinates[i].u, this.textureCoordinates[i].v, packedOverlay, packedLight, transformedNormal.x(), transformedNormal.y(), transformedNormal.z());
        }
    }

}