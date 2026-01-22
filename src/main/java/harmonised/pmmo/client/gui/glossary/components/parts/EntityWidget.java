package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityWidget extends AbstractWidget{
    private final Entity entity;
    public EntityWidget(Entity entity) {this(0, 0, 18, 18, entity.getDisplayName(), entity);}
    public EntityWidget(int x, int y, int width, int height, Component message, Entity entity) {
        super(x, y, width, height, message);
        this.entity = entity;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = 180;
            living.yHeadRot = 195;
            int scale = Math.max(1, 10 / Math.max(1, (int) entity.getBoundingBox().getSize()));Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf quaternionf1 = new Quaternionf().rotateX(90 * 20.0F * (float) (Math.PI / 180.0));
            quaternionf.mul(quaternionf1);
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.getX(), this.getY(), this.getRight(), this.getBottom(), scale, 0, mouseX, mouseY, living);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

}
