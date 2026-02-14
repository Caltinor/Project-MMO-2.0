package harmonised.pmmo.client.gui.glossary.components;

import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.api.client.wrappers.BoxDimensions;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.PanelWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class DetailScroll extends AbstractTextAreaWidget implements GlossaryFilter, ResponsiveLayout {
	private final List<Positioner<?>> children = new ArrayList<>();
	private BoxDimensions margin, padding;

	public DetailScroll(int x, int y, int width, int height) {
		super(x, y, width, height, Component.literal("Panel Scroll Widget"));
		setMargin(0, 0, 0, 0);
		setPadding(0, 0, 0, 0);
	}
	@Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}
	@Override public List<Positioner<?>> getChildren() {return children;}
	@Override public void addChild(Positioner<?> child) {children.add(child);}
	@Override public BoxDimensions getMargin() {return margin;}
	@Override public BoxDimensions getPadding() {return padding;}
	@Override public int getInternalHeight() {return Integer.MAX_VALUE;}

	@Override
	public ResponsiveLayout setMargin(int left, int top, int right, int bottom) {
		margin = new BoxDimensions(left, top, right, bottom);
		return this;
	}

	@Override
	public ResponsiveLayout setPadding(int left, int top, int right, int bottom) {
		padding = new BoxDimensions(left, top, right, bottom);
		return this;
	}

	@Override
	public int contentHeight() {return 0;} //TODO

	@Override
	public List<Positioner<?>> visibleChildren() {
		return new ArrayList<>(children).stream().filter(poser -> poser.get() instanceof AbstractWidget widget && widget.visible).toList();
	}

	@Override
	protected int getInnerHeight() {
		return visibleChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0);
	}

	@Override
	public int maxScrollAmount() {
		return Math.max(1, this.getInnerHeight() - this.height) /2;
	}

	@Override
	protected double scrollRate() {return Math.min(50, maxScrollAmount()/100);}

	private List<PanelWidget> widgets() {
		return getChildren().stream()
				.filter(poser -> poser.get() instanceof PanelWidget)
				.map(poser -> (PanelWidget)poser.get())
				.toList();
	}

	@Override
	protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		setPadding(padding.left(), -(int)scrollAmount(), padding.right(), padding.bottom());
		arrangeElements();
		visibleChildren().forEach(poser -> {
			AbstractWidget widget = ((AbstractWidget)poser.get());
			widget.setFocused(widget.isMouseOver(mouseX, mouseY + scrollAmount()));
			widget.render(guiGraphics, mouseX, mouseY, partialTick);});
	}

	@Override
	public boolean applyFilter(Filter filter) {
		for (PanelWidget widget : widgets()) {
            widget.visible = !widget.applyFilter(filter);
		}
		return false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

	@Override
	public boolean mouseClicked(MouseButtonEvent mbe, boolean isRelease) {
		double transY = mbe.y() + this.scrollAmount();
		for (AbstractWidget widget : widgets()) {
			if (widget.visible && widget.isMouseOver(mbe.x(), transY) && widget.mouseClicked(mbe, isRelease)) {
				widget.setFocused(true);
				break;
			}
		}
		return super.mouseClicked(mbe, isRelease);
	}
}
