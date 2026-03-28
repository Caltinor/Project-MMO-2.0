package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.item(stack, this.getX() + 1, this.getY() +1);
        if (this.isMouseOver(mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(Minecraft.getInstance().font, stack, this.getX() + 20, 100);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

}
