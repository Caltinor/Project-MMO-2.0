package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

import java.awt.Color;

public class PlayerSkillWidget extends PanelWidget {
    protected static final ResourceLocation TEXTURE_LOCATION = Reference.rl("textures/gui/player_stats.png");
    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
            Reference.rl("stat_background"),
            Reference.rl("stat_background_highlighted")
    );

    private static final int HEIGHT = 24;
    private static final int ICON_SIZE = 18;
    private static final int ICON_LEFT_PAD = 3;
    private static final int CONTENT_LEFT_PAD = 24;
    private static final int LEVEL_RIGHT_PAD = 7;
    private static final int PROGRESS_BAR_RIGHT_MARGIN = 3;
    private static final int PROGRESS_BAR_HEIGHT = 5;
    private static final float NAME_SCALE = 0.85f;
    private static final float HOVER_TEXT_SCALE = 0.60f;

    private final SkillData skillData;
    private final Color skillColor;
    private final String skillName;
    private final Experience xp;
    private final Font font = Minecraft.getInstance().font;

    public PlayerSkillWidget(int width, String skillName, SkillData data) {
        super(data.getColor(), width);
        setHeight(HEIGHT);
        this.skillName = skillName;
        this.skillData = data;
        this.skillColor = new Color(data.getColor());
        this.xp = Core.get(LogicalSide.CLIENT).getData().getXpMap(null).getOrDefault(skillName, new Experience());
    }

    @Override public void resize() {setHeight(HEIGHT);}

    /**
     * Hidden when the search query is non-empty AND matches neither the raw skill key
     * nor the translated display name. The translation match is skipped when no lang
     * entry is registered (in which case {@code Component.translatable(...).getString()}
     * returns the literal key like "pmmo.mining" and would falsely match queries like "pmmo").
     */
    @Override
    public boolean applyFilter(GlossaryFilter.Filter filter) {
        String text = filter.getTextFilter();
        if (text == null || text.isEmpty()) return false;
        String query = text.toLowerCase();
        if (skillName.toLowerCase().contains(query)) return false;
        String translationKey = "pmmo." + skillName;
        String translated = Component.translatable(translationKey).getString();
        if (translated.equals(translationKey)) return true;
        return !translated.toLowerCase().contains(query);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(BACKGROUND_SPRITES.get(this.isActive(), this.isFocused()), this.getX(), this.getY(), this.width, this.height);

        int iconSourceSize = skillData.getIconSize();
        graphics.blit(skillData.getIcon(), this.getX() + ICON_LEFT_PAD, this.getY() + ICON_LEFT_PAD,
                ICON_SIZE, ICON_SIZE, 0, 0, iconSourceSize, iconSourceSize, iconSourceSize, iconSourceSize);

        renderProgressBar(graphics);

        // Skill name, scaled to 85% via PoseStack so it fits next to the level number.
        graphics.pose().pushPose();
        graphics.pose().translate(this.getX() + CONTENT_LEFT_PAD, this.getY() + 5, 0);
        graphics.pose().scale(NAME_SCALE, NAME_SCALE, 1.0f);
        graphics.drawString(font, Component.translatable("pmmo." + skillName), 0, 0, skillColor.getRGB());
        graphics.pose().popPose();

        // Right-aligned level number.
        String level = String.valueOf(xp.getLevel().getLevel());
        graphics.drawString(font, level, this.getX() + this.width - LEVEL_RIGHT_PAD - font.width(level), this.getY() + 5, skillColor.getRGB());
    }

    /**
     * Bottom slot beneath the skill name. Shows a colored progress bar by default;
     * while focused (hover) it switches to a small "Next lvl: X xp" readout.
     */
    private void renderProgressBar(GuiGraphics graphics) {
        int renderX = this.getX() + CONTENT_LEFT_PAD;
        int renderY = this.getY() + (font.lineHeight + 6);
        if (this.isFocused()) {
            MutableComponent text = Component.literal("Next lvl: %s xp".formatted(xp.getLevel().getXpToNext() - xp.getXp()));
            graphics.pose().pushPose();
            graphics.pose().translate(renderX, renderY - 1, 0);
            graphics.pose().scale(HOVER_TEXT_SCALE, HOVER_TEXT_SCALE, 1.0f);
            graphics.drawString(font, text, 0, 0, skillColor.getRGB());
            graphics.pose().popPose();
            return;
        }
        int barWidth = this.width - CONTENT_LEFT_PAD - PROGRESS_BAR_RIGHT_MARGIN;
        graphics.setColor(skillColor.getRed() / 255.0f, skillColor.getGreen() / 255.0f, skillColor.getBlue() / 255.0f, skillColor.getAlpha() / 255.0f);
        graphics.blit(TEXTURE_LOCATION, renderX, renderY, barWidth, PROGRESS_BAR_HEIGHT, 0.0F, 217.0F, 102, 5, 256, 256);

        long threshold = xp.getLevel().getXpToNext();
        int fillWidth = threshold <= 0 ? 0 : (int) Math.min((xp.getXp() * (long) barWidth) / threshold, (long) barWidth);
        graphics.blit(TEXTURE_LOCATION, renderX, renderY, fillWidth, PROGRESS_BAR_HEIGHT, 0.0F, 223.0F, 102, 5, 256, 256);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
