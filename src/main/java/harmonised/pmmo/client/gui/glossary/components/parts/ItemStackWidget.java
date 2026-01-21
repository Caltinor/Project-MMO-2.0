package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemStackWidget extends AbstractWidget{
    private final ItemStack stack;
    public ItemStackWidget(Item item) {this(item.getDefaultInstance());}
    public ItemStackWidget(ItemStack stack) {this(0, 0, 18, 18, stack.getDisplayName(), stack);}
    public ItemStackWidget(int x, int y, int width, int height, Component message, ItemStack stack) {
        super(x, y, width, height, message);
        this.stack = stack;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.renderItem(stack, this.getX() + 1, this.getY() +1);
        if (mouseX > this.getX()+1 && mouseX <= this.getX()+17 && mouseY > this.getY()+1 && mouseY <= this.getY()+17) {
            //for the record i absolutely hate this implementation of renderTooltip.  tf Mojang?!
            var lines = stack.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, TooltipFlag.NORMAL).stream()
                    .map(c -> ClientTooltipComponent.create(c.getVisualOrderText())).toList();
            guiGraphics.renderTooltip(Minecraft.getInstance().font, lines, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null, stack);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

}
