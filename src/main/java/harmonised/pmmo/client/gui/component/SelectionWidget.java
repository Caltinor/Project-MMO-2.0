package harmonised.pmmo.client.gui.component;

import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SelectionWidget<T extends SelectionWidget.SelectionEntry<?>> extends AbstractWidget {
    protected static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(
        Reference.mc("widget/button"),
        Reference.mc("widget/button_disabled"),
        Reference.mc("widget/button_highlighted")
    );
    private static final Identifier SORT_UP_SPRITE = Reference.mc("statistics/sort_up");
    private static final Identifier SORT_DOWN_SPRITE = Reference.mc("statistics/sort_down");
    
    private static final int ENTRY_HEIGHT = 20;
    private final Component title;
    private final Consumer<T> selectCallback;
    private List<T> entries = new ArrayList<>();
    private T selected = null;
    private boolean extended = false;
    private int scrollOffset = 0;
    private Font font = Minecraft.getInstance().font;

    public SelectionWidget(int x, int y, int width, Component title, Consumer<T> selectCallback) {
        super(x, y, width, ENTRY_HEIGHT, Component.empty());
        this.title = title;
        this.selectCallback = selectCallback;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
//        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
//        RenderSystem.enableBlend();
//        RenderSystem.enableDepthTest();
        Identifier location = BUTTON_SPRITES.get(this.isActive(), this.isMouseOver(mouseX, mouseY));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, location, this.getX(), this.getY(), this.getWidth(), this.getHeight()/*, 20, 4, 200, 20, 0, 66*/);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (selected != null)
            selected.render(graphics, getX(), getY(), width, false, getFGColor(), alpha);
        else {
            graphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
            graphics.drawString(font, title, getX() + 6, getY() + (height - 8) / 2, getFGColor() | Mth.ceil(alpha * 255.0F) << 24);
            graphics.disableScissor();
        }

        if (extended) {
//            graphics.pose().pushPose();
            graphics.pose().translate(0, 0);

            int boxHeight = Math.max(1, ENTRY_HEIGHT * Math.min(entries.size(), 4)) + 2;

            graphics.fill(RenderPipelines.GUI, getX(),     getY() + ENTRY_HEIGHT - 1, getX() + width,     getY() + ENTRY_HEIGHT + boxHeight - 1, 0xFFFFFFFF);
            graphics.fill(RenderPipelines.GUI, getX() + 1, getY() + ENTRY_HEIGHT,     getX() + width - 1, getY() + ENTRY_HEIGHT + boxHeight - 2, 0xFF000000);

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SORT_UP_SPRITE, getX() + width - 22, getY() + 1, 20, 18);

            T hoverEntry = getEntryAtPosition(mouseX, mouseY);

            for (int i = 0; i < 4; i++) {
                int idx = i + scrollOffset;
                if (idx < entries.size()) {
                    int entryY = getY() + ((i + 1) * ENTRY_HEIGHT);

                    T entry = entries.get(idx);
                    entry.render(graphics, getX() + 1, entryY, width - 2, entry == hoverEntry, getFGColor(), alpha);
                }
            }

            if (entries.size() > 4) {
                float scale = 4F / (float)entries.size();
                int scrollY = getY() + (int)(ENTRY_HEIGHT * scrollOffset * scale) + ENTRY_HEIGHT;
                int barHeight = (int)(ENTRY_HEIGHT * 4 * scale + 1);
                int scrollBotY = Math.min(scrollY + barHeight, getY() + ENTRY_HEIGHT + boxHeight - 2);

                graphics.fill(RenderPipelines.GUI, getX() + width - 5, scrollY,     getX() + width - 1, scrollBotY,     0xFF666666);
                graphics.fill(RenderPipelines.GUI, getX() + width - 4, scrollY + 1, getX() + width - 2, scrollBotY - 1, 0xFFAAAAAA);
            }

//            graphics.pose().popPose();
        }
        else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SORT_DOWN_SPRITE, getX() + width - 22, getY() + 1, 20, 18);
        }
    }

    @Override
    public int getHeight() {
        if (extended) {
            return ENTRY_HEIGHT * (Math.min(entries.size(), 4) + 1) + 1;
        }
        return ENTRY_HEIGHT;
    }
    
    public boolean isExtended() { return extended; }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseEvent, boolean doubleClicked) {
        if (active && mouseEvent.x() >= getX() && mouseEvent.x() <= getX() + width && mouseEvent.y() >= getY() && mouseEvent.y() <= getY() + getHeight()) {
            int maxX = getX() + width - (entries.size() > 4 ? 5 : 0);
            int maxY = getY() + ENTRY_HEIGHT * Math.min(entries.size() + 1, 5);
            if (extended && mouseEvent.x() < maxX && mouseEvent.y() > (getY() + ENTRY_HEIGHT) && mouseEvent.y() < maxY)
                setSelected(getEntryAtPosition(mouseEvent.x(), mouseEvent.y()), true);

            if ((mouseEvent.y() < getY() + ENTRY_HEIGHT && mouseEvent.x() < getX() + width) || mouseEvent.x() < maxX) {
                extended = !extended;
                scrollOffset = 0;
            }

            playDownSound(Minecraft.getInstance().getSoundManager());

            return true;
        }

        extended = false;
        scrollOffset = 0;

        return super.mouseClicked(mouseEvent, doubleClicked);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double other) {
        int maxY = getY() + ENTRY_HEIGHT * Math.min(entries.size() + 1, 5);
        if (extended && mouseX >= getX() && mouseX <= getX() + width && mouseY > getY() + ENTRY_HEIGHT && mouseY < maxY) {
            if (other <= 0 && scrollOffset < entries.size() - 4)
                scrollOffset++;
            else if (other > 0 && scrollOffset > 0)
                scrollOffset--;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta, other);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        if (!active || !visible) { return false; }
        return pMouseX >= getX() && pMouseY >= getY() && pMouseX < (getX() + width) && pMouseY < (getY() + getHeight());
    }

    private T getEntryAtPosition(double x, double y) {
        if (x < getX() || x > getX() + width || y < (getY() + ENTRY_HEIGHT) || y > (getY() + (ENTRY_HEIGHT * 5)))
            return null;

        double posY = y - (getY() + ENTRY_HEIGHT);
        int idx = (int) (posY / ENTRY_HEIGHT) + scrollOffset;

        return idx < entries.size() ? entries.get(idx) : null;
    }

    public SelectionWidget<T> setEntries(Collection<T> entry) { entries = new ArrayList<>(entry); return this;}

    public void setSelected(T selected, boolean notify) {
        this.selected = selected;
        if (notify && selectCallback != null)
            selectCallback.accept(selected);
    }

    public T getSelected() { return selected; }

    public Stream<T> stream() { return entries.stream(); }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) { }

    public static class SelectionEntry<T> implements GuiEventListener {
    	private Font font = Minecraft.getInstance().font;
        public final Component message;
        public T reference;

        public SelectionEntry(Component message, T reference) { 
        	this.message = message; 
        	this.reference = reference;
        }

        public void render(GuiGraphics graphics, int x, int y, int width, boolean hovered, int fgColor, float alpha) {
            if (hovered)
                graphics.fill(RenderPipelines.GUI, x, y, x + width, y + ENTRY_HEIGHT, 0xFFA0A0A0);

            FormattedCharSequence text = Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(message, width - 12)));
            graphics.drawString(font, text, x + 6, y + 6, fgColor | Mth.ceil(alpha * 255.0F) << 24);
        }

		@Override
		public void setFocused(boolean p_265728_) {}

		@Override
		public boolean isFocused() {return false;}
    }
}
