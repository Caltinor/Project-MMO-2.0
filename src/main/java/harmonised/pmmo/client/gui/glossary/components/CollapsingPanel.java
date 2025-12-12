package harmonised.pmmo.client.gui.glossary.components;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class CollapsingPanel extends ReactiveWidget {
    protected static final ResourceLocation TEXTURE_LOCATION = Reference.rl("textures/gui/player_stats.png");
    public final int expandedWidth;
    private Consumer<CollapsingPanel> callback;

    public CollapsingPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.expandedWidth = width;
        this.setPadding(5, 5, 7, 7);
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    private boolean collapsed() {return this.getWidth() == 5;}

    public CollapsingPanel addCallback(Consumer<CollapsingPanel> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        guiGraphics.blit(TEXTURE_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                collapsed() ? 140 : 0, 0, collapsed() ? 7 : 147, 165, 256, 256);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY > this.getY() && mouseY < this.getY() + this.getHeight()
           && mouseX > this.getRight() - 7 && mouseX < this.getX() + this.getRight()) {
            this.setWidth(collapsed() ? this.expandedWidth : 5);
            if (this.callback != null)
                callback.accept(this);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        return false;
    }
}
