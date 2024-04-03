package nanolive.compat;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class VertexUtils {

    public static void setupBufferState(VertexFormat format, long offset) {
        for(int i = 0; i < format.getElements().size(); i++) {
            VertexFormatElement element = format.getElements().get(i);
            // TODO Not sure
            element.getUsage().preDraw(format, (int) (offset + i), format.getSize(), Tessellator.getInstance().getBuffer().getByteBuffer());
        }
    }

    public static void clearBufferState(VertexFormat format) {
        for(int i = 0; i < format.getElements().size(); i++) {
            VertexFormatElement element = format.getElements().get(i);
            element.getUsage().postDraw(format, i, 0, null);
        }
    }

}
