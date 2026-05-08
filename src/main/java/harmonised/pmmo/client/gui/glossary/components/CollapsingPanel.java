package harmonised.pmmo.client.gui.glossary.components;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class CollapsingPanel extends ReactiveWidget {
    protected static final ResourceLocation TEXTURE_LOCATION = Reference.rl("textures/gui/player_stats.png");
    protected static final ResourceLocation RIGHT = Reference.rl("textures/gui/arrow_right.png");
    protected static final ResourceLocation LEFT = Reference.rl("textures/gui/arrow_left.png");
    public final int expandedWidth;
    private final double scale;
    private Consumer<CollapsingPanel> callback;

    public CollapsingPanel(int x, int y, int width, int height) {
        this(x, y, width, height, true);
    }
    public CollapsingPanel(int x, int y, int width, int height, boolean open) {
        super(x, y, width, height);
        this.expandedWidth = width;
        this.scale = Minecraft.getInstance().getWindow().getGuiScale();
        this.setPadding(5, 5, 7, 7);
        this.setWidth(open ? this.expandedWidth : collapsedWidth());
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    private boolean collapsed() {return this.getWidth() < this.expandedWidth;}

    public CollapsingPanel addCallback(Consumer<CollapsingPanel> callback) {
        this.callback = callback;
        return this;
    }

    @Override public void resize() {}

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        guiGraphics.blit(TEXTURE_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                collapsed() ? 140 : 0, 0, collapsed() ? 7 : 147, 165, 256, 256);
        int offset = (int) (28d / scale);
        int hScaled = (int) (80d / scale);
        guiGraphics.blit(collapsed() ? RIGHT : LEFT, this.getX()+this.width-offset,
                this.getY()+ (this.height/2) - (hScaled/2) - 1, offset, hScaled,
                0, 0, 7, 20, 7, 20);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clickedToggleArrow(mouseX, mouseY)) {
            this.setWidth(collapsed() ? this.expandedWidth : collapsedWidth());
            if (this.callback != null) callback.accept(this);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickedToggleArrow(double mouseX, double mouseY) {
        return mouseY > this.getY() && mouseY < this.getY() + this.getHeight()
                && mouseX > this.getRight() - (20 / scale) && mouseX < this.getX() + this.getRight();
    }

    private int collapsedWidth() {
        return (int) (20d / scale);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        return false;
    }
}
