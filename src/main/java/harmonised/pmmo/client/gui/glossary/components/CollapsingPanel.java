package harmonised.pmmo.client.gui.glossary.components;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
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

    @Override public void resize() {} //Panel does not adjust height beyond parent size constraints

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
        // The right-edge arrow tab toggles between collapsed and expanded width.
        if (clickedToggleArrow(mouseX, mouseY)) {
            this.setWidth(collapsed() ? this.expandedWidth : collapsedWidth());
            if (this.callback != null) callback.accept(this);
            return true;
        }
        // Otherwise let ReactiveWidget route the click to the appropriate child,
        // then drop focus on any child that wasn't the one clicked. This makes
        // clicks elsewhere inside the panel (e.g. on a skill row) defocus a
        // previously-focused search bar.
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        defocusUnclickedChildren(mouseX, mouseY, handled);
        return handled;
    }

    /**
     * When the screen moves focus away from this panel (e.g. user clicks an inventory slot),
     * vanilla calls {@code setFocused(false)}. Cascade that to defocus any focused child
     * widget so a still-focused search box doesn't keep eating keystrokes.
     */
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) defocusUnclickedChildren(0, 0, false);
    }

    /** True when the cursor is on the thin arrow strip on the panel's right edge that toggles open/closed. */
    private boolean clickedToggleArrow(double mouseX, double mouseY) {
        return mouseY > this.getY() && mouseY < this.getY() + this.getHeight()
                && mouseX > this.getRight() - (20 / scale) && mouseX < this.getX() + this.getRight();
    }

    /** Width of the panel when collapsed — only enough for the arrow tab to show. */
    private int collapsedWidth() {
        return (int) (20d / scale);
    }

    /**
     * Defocuses every focused child except the one the click landed on. With
     * {@code clickHandled=false} (call from setFocused) all focused children are
     * cleared regardless of cursor position.
     */
    private void defocusUnclickedChildren(double mouseX, double mouseY, boolean clickHandled) {
        for (AbstractWidget child : widgets()) {
            if (!child.isFocused()) continue;
            if (clickHandled && child.isMouseOver(mouseX, mouseY)) continue;
            child.setFocused(false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        return false;
    }
}
