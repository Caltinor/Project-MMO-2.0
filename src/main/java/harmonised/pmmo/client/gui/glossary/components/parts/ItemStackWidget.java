package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
            guiGraphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

}
