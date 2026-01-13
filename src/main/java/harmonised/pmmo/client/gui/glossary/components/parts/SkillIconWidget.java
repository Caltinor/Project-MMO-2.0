package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SkillIconWidget extends AbstractWidget {
    private final ResourceLocation icon;
    private final int iconSize;
    public SkillIconWidget(ResourceLocation icon, int iconSize) {
        super(0, 0, 18, 18, Component.literal("skill icon"));
        this.icon = icon;
        this.iconSize = iconSize;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        guiGraphics.blit(icon, this.getX() + 3, this.getY() + 3, 18, 18, 0, 0, iconSize, iconSize, iconSize, iconSize);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
