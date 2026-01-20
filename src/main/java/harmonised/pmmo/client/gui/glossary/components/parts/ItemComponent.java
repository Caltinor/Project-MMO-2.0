package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class ItemComponent implements Renderable, LayoutElement {
    private int x, y;
    private final ItemStack stack;

    public ItemComponent(Item item) {this(item.getDefaultInstance());}
    public ItemComponent(ItemStack stack) {this.stack = stack;}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.renderItem(stack, x, y);
    }

    @Override public void setX(int x) {this.x = x;}
    @Override public void setY(int y) {this.y = y;}
    @Override public int getX() {return x;}
    @Override public int getY() {return y;}
    @Override public int getWidth() {return 32;}
    @Override public int getHeight() {return 32;}

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {

    }
}
