package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class PanelWidget extends ReactiveWidget {
    private final int color;
    public PanelWidget(int color, int width) {
        super(0,0, width, 0);
        this.color = color;
    }

    @Override public DisplayType getDisplayType() {return DisplayType.INLINE;}

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.arrangeElements();
        guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), color);
        widgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        return false;
    }
}
