package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityWidget extends AbstractWidget{
    private final Entity entity;
    public EntityWidget(Entity entity) {this(0, 0, 18, 18, entity.getDisplayName(), entity);}
    public EntityWidget(int x, int y, int width, int height, Component message, Entity entity) {
        super(x, y, width, height, message);
        this.entity = entity;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        if (entity instanceof LivingEntity living) {
            int scale = Math.max(1, 10 / Math.max(1, (int) entity.getBoundingBox().getSize()));
            InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphicsExtractor, this.getX(), this.getY(), this.getRight(), this.getBottom(),
                    scale, 0, 0.5f, -0.5f, living);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

//    public static void renderEntityInInventoryFollowsAngle(GuiGraphicsExtractor p_282802_, int p_275688_, int p_275245_, int p_275535_, int p_294406_, int p_294663_, float p_275604_, float angleXComponent, float angleYComponent, LivingEntity p_275689_) {
//        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
//        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(angleYComponent * 20.0F * ((float)Math.PI / 180F));
//        quaternionf.mul(quaternionf1);
//        EntityRenderState entityrenderstate = extractRenderState(p_275689_);
//        if (entityrenderstate instanceof LivingEntityRenderState livingentityrenderstate) {
//            livingentityrenderstate.bodyRot = 180.0F + angleXComponent * 20.0F;
//            livingentityrenderstate.yRot = angleXComponent * 20.0F;
//            if (livingentityrenderstate.pose != Pose.FALL_FLYING) {
//                livingentityrenderstate.xRot = -angleYComponent * 20.0F;
//            } else {
//                livingentityrenderstate.xRot = 0.0F;
//            }
//
//            livingentityrenderstate.boundingBoxWidth /= livingentityrenderstate.scale;
//            livingentityrenderstate.boundingBoxHeight /= livingentityrenderstate.scale;
//            livingentityrenderstate.scale = 1.0F;
//        }
//
//        Vector3f vector3f = new Vector3f(0.0F, p_275604_, 0.0F);
//        p_282802_.submitEntityRenderState(entityrenderstate, (float)p_294663_, vector3f, quaternionf, quaternionf1, p_275688_, p_275245_, p_275535_, p_294406_);
//    }
//
//    private static EntityRenderState extractRenderState(LivingEntity entity) {
//        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
//        EntityRenderer<? super LivingEntity, ?> entityrenderer = entityrenderdispatcher.getRenderer(entity);
//        EntityRenderState entityrenderstate = entityrenderer.createRenderState(entity, 1.0F);
//        entityrenderstate.lightCoords = 15728880;
//        entityrenderstate.shadowPieces.clear();
//        entityrenderstate.outlineColor = 0;
//        return entityrenderstate;
//    }

}
