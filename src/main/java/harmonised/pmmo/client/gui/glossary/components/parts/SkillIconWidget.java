package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SkillIconWidget extends AbstractWidget {
    private final Identifier icon;
    private final int iconSize;
    public SkillIconWidget(Identifier icon, int iconSize) {
        super(0, 0, 18, 18, Component.literal("skill icon"));
        this.icon = icon;
        this.iconSize = iconSize;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, this.getX() + 3, this.getY() + 3,  0, 0,18, 18, iconSize, iconSize, iconSize, iconSize);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
