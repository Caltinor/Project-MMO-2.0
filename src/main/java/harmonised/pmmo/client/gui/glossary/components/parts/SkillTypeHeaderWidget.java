package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.config.codecs.SkillTypeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SkillTypeHeaderWidget extends PanelWidget {
    private static final int HEADER_BAND_HEIGHT = 11;
    private static final int ACCENT_BAR_WIDTH = 3;
    private static final int ACCENT_INSET = 5;
    private static final int ACCENT_BOTTOM_THICKNESS = 2;
    private static final int ACCENT_TINT_ALPHA = 0x19;
    private static final int TEXT_INSET = 5;
    private static final int TEXT_TOP_OFFSET = 2;
    private static final int BAND_BACKGROUND = 0x40000000;

    private final Component label;
    private final int accentColor;
    private final Font font = Minecraft.getInstance().font;

    public SkillTypeHeaderWidget(int width, String typeKey, SkillTypeData data, List<PlayerSkillWidget> rows) {
        super(0, width);
        this.label = data.getDisplayName(typeKey);
        this.accentColor = data.getColor();
        // Children stack vertically. Top padding reserves space for the header band;
        // left padding shifts rows right of the colored bar.
        setPadding(ACCENT_INSET, HEADER_BAND_HEIGHT, 0, 0);
        for (PlayerSkillWidget row : rows) {
            addChild((AbstractWidget) row, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
        }
        resize();
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    public void resize() {
        int rowsHeight = visibleChildren().stream()
                .mapToInt(positioner -> positioner.get().getHeight())
                .sum();
        setHeight(HEADER_BAND_HEIGHT + rowsHeight);
    }

    @Override
    public boolean applyFilter(GlossaryFilter.Filter filter) {
        boolean anyVisible = false;
        for (AbstractWidget child : widgets()) {
            boolean hide = child instanceof GlossaryFilter filterable && filterable.applyFilter(filter);
            child.visible = !hide;
            if (!hide) {
                anyVisible = true;
            }
        }
        resize();
        return !anyVisible;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        arrangeElements();
        resize();

        // Strip any incoming alpha so the tint and frame both build their ARGB from a clean RGB.
        int rgb = accentColor & 0x00FFFFFF;

        // Translucent band fill behind the label, only over the band area.
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getY() + HEADER_BAND_HEIGHT, BAND_BACKGROUND);

        // mouseY arrives in screen-space; the parent scroll has pushed a vertical
        // translate so we undo it here to know which row the cursor is over.
        // Without this, the row's hover-text wouldn't react to cursor movement
        // because its own getY() lives in scrolled content-space.
        int contentMouseY = mouseY - (int) graphics.pose().last().pose().m31();
        for (AbstractWidget row : widgets()) {
            if (!row.visible) continue;
            row.setFocused(row.isMouseOver(mouseX, contentMouseY));
            row.render(graphics, mouseX, mouseY, partialTick);
        }

        // Faint accent tint over the rows area, drawn after rows so it alpha-blends
        // on top of their opaque background sprites.
        int tintArgb = (ACCENT_TINT_ALPHA << 24) | rgb;
        graphics.fill(this.getX(), this.getY() + HEADER_BAND_HEIGHT, this.getRight(), this.getBottom(), tintArgb);

        // Colored frame, drawn on top so it overlays the row backgrounds at the edges.
        int solidArgb = 0xFF000000 | rgb;
        graphics.fill(this.getX(), this.getY(), this.getX() + ACCENT_BAR_WIDTH, this.getBottom(), solidArgb);
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getY() + 1, solidArgb);
        graphics.fill(this.getX(), this.getBottom() - ACCENT_BOTTOM_THICKNESS, this.getRight(), this.getBottom(), solidArgb);

        // Header label sits on top of the band background and the left bar.
        graphics.drawString(font, label, this.getX() + ACCENT_BAR_WIDTH + TEXT_INSET, this.getY() + TEXT_TOP_OFFSET, accentColor);
    }
}
