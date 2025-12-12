package harmonised.pmmo.client.gui;

import harmonised.pmmo.api.client.types.GlossaryFilter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class ComponentContainer extends AbstractWidget implements GlossaryFilter, Layout {
    private final List<Renderable> renderables = new ArrayList<>();
    private final List<GuiEventListener> listeners = new ArrayList<>();
    private final List<GlossaryFilter> filterables = new ArrayList<>();
    private final List<LayoutElement> arrangeables = new ArrayList<>();

    public ComponentContainer(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public <T> T addWidget(T widget) {
        if (widget instanceof Renderable renderable)
            renderables.add(renderable);
        if (widget instanceof GuiEventListener listener)
            listeners.add(listener);
        if (widget instanceof LayoutElement element)
            arrangeables.add(element);
        if (widget instanceof GlossaryFilter filters)
            filterables.add(filters);
        return widget;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderables.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener widget : listeners) {
            if (widget.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiEventListener widget : listeners) {
            if (widget.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (GuiEventListener widget : listeners) {
            if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (GuiEventListener widget : listeners) {
            if (widget.isMouseOver(mouseX, mouseY)) return true;
        }
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (GuiEventListener widget : listeners) {
            if (widget.isMouseOver(mouseX, mouseY)) widget.mouseMoved(mouseX, mouseY);;
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (GuiEventListener widget : listeners) {
            if (widget.mouseScrolled(mouseX, mouseY, scrollX,scrollY)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener widget : listeners) {
            if (widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (GuiEventListener widget : listeners) {
            if (widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (GuiEventListener widget : listeners) {
            if (widget.charTyped(codePoint, modifiers)) return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
