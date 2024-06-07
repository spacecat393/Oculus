package net.coderbot.batchedentityrendering.impl;

import org.lwjgl.opengl.GL11;

public class RenderTypeUtil {
    public static boolean isTriangleStripDrawMode(CustomRenderType renderType) {
        return renderType.getMode() == GL11.GL_TRIANGLE_STRIP;
    }
}