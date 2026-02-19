package harmonised.pmmo.client.gui.glossary.components;

import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.api.client.wrappers.BoxDimensions;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class ReactiveWidget extends AbstractWidget implements GlossaryFilter, ResponsiveLayout{
    protected static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();
    private final List<Positioner<?>> children = new ArrayList<>();
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
        this.setPadding(left, top, right, bottom);
//        Double scaledLeft = (double)left / scale;
//        Double scaledTop = (double)top / scale;
//        Double scaledBottom = (double)bottom / scale;
//        Double scaledRight = (double)right / scale;
//        this.setPadding(scaledLeft.intValue(), scaledTop.intValue(), scaledRight.intValue(), scaledBottom.intValue());
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

    /// Function to allow custom resizing of the widget dimensions after
    /// the children have been arranged.  The default behavior of this
    /// method is to summate the height of all children.  width is not
    /// affected, nor is consideration for grids or inline.
    ///
    ///*Note: if the sizing of this widget should be entirely defined by
    /// sizing constraints, override with a NOOP implementation*
    public void resize() {
        setHeight(this.visible ? getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0) : 0);
    }

    @Override
    public void addChild(Positioner<?> child) {children.add(child);}

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.arrangeElements();
        resize();
        widgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    protected List<AbstractWidget> widgets() {
        return getChildren().stream().filter(poser -> poser.get() instanceof  AbstractWidget).map(poser -> (AbstractWidget)poser.get()).toList();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mbe, boolean isRelease) {
        for (AbstractWidget widget : widgets().stream().filter(a -> a.visible).toList()) {
            if (widget.isMouseOver(mbe.x(), mbe.y()) && widget.mouseClicked(mbe, isRelease)) {
                widget.setFocused(true);
                return true;
            }
        }
        return super.mouseClicked(mbe, isRelease);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mbe) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isMouseOver(mbe.x(), mbe.y()) && widget.mouseReleased(mbe)) return true;
        }
        return super.mouseReleased(mbe);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mbe, double dragX, double dragY) {
        for (AbstractWidget widget : widgets()) {
            if (widget.mouseDragged(mbe, dragX, dragY)) return true;
        }
        return super.mouseDragged(mbe, dragX, dragY);
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
    public boolean keyPressed(KeyEvent ke) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isHoveredOrFocused() && widget.keyPressed(ke)) return true;
        }
        return super.keyPressed(ke);
    }

    @Override
    public boolean keyReleased(KeyEvent ke) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isHoveredOrFocused() && widget.keyReleased(ke)) return true;
        }
        return super.keyReleased(ke);
    }

    @Override
    public boolean charTyped(CharacterEvent ce) {
        for (AbstractWidget widget : widgets()) {
            if (widget.isHoveredOrFocused() && widget.charTyped(ce)) return true;
        }
        return super.charTyped(ce);
    }

    @Override
    public void onClick(MouseButtonEvent mbe, boolean isDoubleClick) {
        widgets().stream()
                .filter(AbstractWidget::isHoveredOrFocused)
                .forEach(widget -> widget.onClick(mbe, isDoubleClick));
        super.onClick(mbe, isDoubleClick);
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
