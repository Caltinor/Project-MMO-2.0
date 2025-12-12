package harmonised.pmmo.client.gui.glossary.components;

import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.BoxDimensions;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

import javax.swing.text.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class ReactiveWidget extends AbstractWidget implements GlossaryFilter, ResponsiveLayout{
    private final List<Positioner<?>> children = new ArrayList<>();
    private int cachedChildCount = 0;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    BoxDimensions margin, padding;

    protected ReactiveWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("container"));
        setMargin(0, 0, 0, 0);
        setPadding(0, 0, 0, 0);
    }

    @Override public BoxDimensions getMargin() {return margin;}
    @Override public BoxDimensions getPadding() {return padding;}
    @Override public ResponsiveLayout setMargin(int left, int top, int right, int bottom) {
        margin = new BoxDimensions(left, top, right, bottom);
        return this;
    }
    @Override
    public ResponsiveLayout setPadding(int left, int top, int right, int bottom) {
        padding = new BoxDimensions(left, top, right, bottom);
        return this;
    }

    public ResponsiveLayout setPadding(int left, int top, int right, int bottom, double scale) {
        Double scaledLeft = (double)left / scale;
        Double scaledTop = (double)top / scale;
        Double scaledBottom = (double)bottom / scale;
        Double scaledRight = (double)right / scale;
        this.setPadding(scaledLeft.intValue(), scaledTop.intValue(), scaledBottom.intValue(), scaledBottom.intValue());
        return this;
    }

    @Override
    public List<Positioner<?>> getChildren() {
        return children;
    }

    @Override
    public List<Positioner<?>> visibleChildren() {
        return children.stream().filter(poser -> (poser.get() instanceof AbstractWidget widget && widget.visible)
                || poser.get() instanceof ResponsiveLayout).toList();
    }

    @Override
    public void addChild(Positioner<?> child) {children.add(child);}

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.arrangeElements();
        widgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    protected List<AbstractWidget> widgets() {
        if (children.size() == cachedChildCount) return widgets;
        cachedChildCount = children.size();
        widgets.clear();
        this.visitWidgets(widgets::add);
        return widgets;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : widgets()) {
            if (widget.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : widgets()) {
            if (widget.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (AbstractWidget widget : widgets()) {
            if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isMouseOver(mouseX, mouseY)) return true;
        }
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isMouseOver(mouseX, mouseY)) widget.mouseMoved(mouseX, mouseY);;
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (AbstractWidget widget : widgets()) {
            if (widget.mouseScrolled(mouseX, mouseY, scrollX,scrollY)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isHoveredOrFocused() && widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isHoveredOrFocused() && widget.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isHoveredOrFocused() && widget.charTyped(codePoint, modifiers)) return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        for (AbstractWidget widget : widgets()) {
            widget.setFocused(focused);
        }
        super.setFocused(focused);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        widgets().stream()
                .filter(AbstractWidget::isHoveredOrFocused)
                .forEach(widget -> widget.onClick(mouseX, mouseY));
        super.onClick(mouseX, mouseY);
    }

    @Override
    public boolean applyFilter(Filter filter) {
        AtomicBoolean filtered = new AtomicBoolean(true);
        this.visitWidgets(widget -> {if (widget instanceof GlossaryFilter filterable && !filterable.applyFilter(filter)) filtered.getAndSet(false);});
        return filtered.get();
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        ResponsiveLayout.super.visitChildren(visitor);
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        ResponsiveLayout.super.visitWidgets(consumer);
    }
}
