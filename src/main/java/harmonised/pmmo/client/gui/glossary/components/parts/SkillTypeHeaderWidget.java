package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.config.codecs.SkillTypeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SkillTypeHeaderWidget extends PanelWidget {
    private final Component label;
    private final int textColor;
    private final Font font = Minecraft.getInstance().font;

    public SkillTypeHeaderWidget(int width, String typeKey, SkillTypeData data) {
        super(0x40000000, width);
        setHeight(12);
        this.label = data.getDisplayName(typeKey);
        this.textColor = data.getColor();
    }

    @Override public void resize() {setHeight(12);}

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        int textY = this.getY() + (this.height - font.lineHeight) / 2 + 1;
        graphics.drawString(font, label, this.getX() + 4, textY, textColor);
    }
}
