package harmonised.pmmo.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Set;

public class VeinRenderer {

	public static void drawBoxHighlights(PoseStack stack, Set<BlockPos> vein)
    {
		Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.position();
        stack.pushPose();
        stack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(RenderTypes.lines());

        for(BlockPos pos : vein) {
            drawBoxHighlight(stack, builder, pos);
        }
        
        stack.popPose();
//        RenderSystem.disableDepthTest();
        buffer.endBatch();
    }

    public static void drawBoxHighlight(PoseStack stack, VertexConsumer builder, BlockPos pos)
    {
        stack.pushPose();
        Matrix4f matrix4f = stack.last().pose();
        int red = 255;
        int green = 0;
        int blue = 255;
        int alpha = 255;

        for(int i = 0; i < 12; i++)
        {
            int mode = i/4, j = i%4;
            float modulus = j%2, divide = j/2;
            switch(mode)
            {
                case 0:
                    builder.addVertex(matrix4f, pos.getX() + modulus, pos.getY() + divide, pos.getZ()).setColor(red, green, blue, alpha).setNormal(0, 0, 0);
                    builder.addVertex(matrix4f, pos.getX() + modulus, pos.getY() + divide, pos.getZ() + 1).setColor(red, green, blue, alpha).setNormal(0, 0, 0);
                    break;

                case 1:
                    builder.addVertex(matrix4f, pos.getX(), pos.getY() + modulus, pos.getZ() + divide).setColor(red, green, blue, alpha).setNormal(0, 0, 0);
                    builder.addVertex(matrix4f, pos.getX() + 1, pos.getY() + modulus, pos.getZ() + divide).setColor(red, green, blue, alpha).setNormal(0, 0, 0);
                    break;

                case 2:
                    builder.addVertex(matrix4f, pos.getX() + divide, pos.getY(), pos.getZ() + modulus).setColor(red, green, blue, alpha).setNormal(0, 0, 0);
                    builder.addVertex(matrix4f, pos.getX() + divide, pos.getY() + 1, pos.getZ() + modulus).setColor(red, green, blue, alpha).setNormal(0, 0, 0);
                    break;
            }
        }
        stack.popPose();
    }
}
