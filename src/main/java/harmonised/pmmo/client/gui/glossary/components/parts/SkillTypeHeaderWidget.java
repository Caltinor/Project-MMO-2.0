package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.config.codecs.SkillTypeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SkillTypeHeaderWidget extends PanelWidget {
    private static final int ACCENT_BAR_WIDTH = 3;
    private static final int TEXT_INSET = 5;

    private final Component label;
    private final int accentColor;
    private final List<String> skillKeys;
    private final Font font = Minecraft.getInstance().font;

    private static final int HEIGHT = 11;

    public SkillTypeHeaderWidget(int width, String typeKey, SkillTypeData data, List<String> skillKeys) {
        super(0x40000000, width);
        setHeight(HEIGHT);
        this.label = data.getDisplayName(typeKey);
        this.accentColor = data.getColor();
        this.skillKeys = List.copyOf(skillKeys);
    }

    @Override
    public boolean applyFilter(GlossaryFilter.Filter filter) {
        String text = filter.getTextFilter();
        if (text == null || text.isEmpty()) return false;
        String lower = text.toLowerCase();
        return skillKeys.stream().noneMatch(key ->
                Component.translatable("pmmo." + key).getString().toLowerCase().contains(lower)
                        || key.toLowerCase().contains(lower));
    }

    @Override public void resize() {setHeight(HEIGHT);}

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        int argb = 0xFF000000 | accentColor;
        graphics.fill(this.getX(), this.getY(), this.getX() + ACCENT_BAR_WIDTH, this.getBottom(), argb);
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getY() + 1, argb);
        int textY = this.getY() + 2;
        graphics.drawString(font, label, this.getX() + ACCENT_BAR_WIDTH + TEXT_INSET, textY, accentColor);
    }
}
