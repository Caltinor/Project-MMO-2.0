package harmonised.pmmo.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.submit.RenderPhaseKey;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;

import java.awt.*;
import java.util.Collection;
import java.util.Set;

public class VeinRenderer implements CustomBlockOutlineRenderer {
    private final Set<BlockPos> vein;
    private final BlockPos blockPos;
    private final int red, green, blue, alpha;

    public VeinRenderer(Set<BlockPos> vein, BlockPos viewPos) {
        this.vein = vein;
        this.blockPos = viewPos;
        Color color = new Color(0xFFFF00FF);
        try {
            color = new Color(Integer.parseUnsignedInt(Config.VEIN_COLOR.get(), 16));
        } catch (Exception ignored) {
            MsLoggy.ERROR.log(MsLoggy.LOG_CODE.GUI, "Vein color in client config invalid.  defaulting to PMMO purple");
        }
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        this.alpha = color.getAlpha();

    }

    @Override
    public boolean render(BlockOutlineRenderState state, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        poseStack.pushPose();
        Vec3 viewPosition = levelRenderState.cameraRenderState.pos;
        poseStack.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);
        float lineWidth = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        RenderPhaseKey<SubmitNode> phase = state.isTranslucent() ? RenderPhaseKeys.AFTER_TERRAIN : RenderPhaseKeys.SHAPE_OUTLINES;
        submitNodeCollector.submitSpecial(phase, new CustomFeatureRenderer.Submit(poseStack.last().copy(), RenderTypes.lines(), new LineDrawer(vein, lineWidth, red, green, blue, alpha)));
        poseStack.popPose();
        return true;
    }

    private record LineDrawer(Collection<BlockPos> toDraw, float lineWidth, int red, int green, int blue, int alpha) implements SubmitNodeCollector.CustomGeometryRenderer {

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer buffer) {
            for (BlockPos pos : toDraw) {
                //TOP
                buffer.addVertex(pose, pos.getX(), pos.getY(), pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX(), pos.getY(), pos.getZ()+ 1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX(), pos.getY(), pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX() + 1, pos.getY(), pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX()+1, pos.getY(), pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX()+1, pos.getY(), pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX(), pos.getY(), pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX()+1, pos.getY(), pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                //SIDES
                buffer.addVertex(pose, pos.getX(), pos.getY(), pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX(), pos.getY()+ 1, pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX() + 1, pos.getY(), pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX() + 1, pos.getY()+ 1, pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX()+1, pos.getY(), pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX()+1, pos.getY()+ 1, pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX(), pos.getY(), pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX(), pos.getY()+ 1, pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                //BOTTOM
                buffer.addVertex(pose, pos.getX(), pos.getY()+1, pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX(), pos.getY()+1, pos.getZ()+ 1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX(), pos.getY()+1, pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX() + 1, pos.getY()+1, pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX()+1, pos.getY()+1, pos.getZ()).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX()+1, pos.getY()+1, pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);

                buffer.addVertex(pose, pos.getX(), pos.getY()+1, pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
                buffer.addVertex(pose, pos.getX()+1, pos.getY()+1, pos.getZ()+1).setColor(red, green, blue, alpha).setNormal(0,0,0).setLineWidth(lineWidth);
            }
        }
    }
}
