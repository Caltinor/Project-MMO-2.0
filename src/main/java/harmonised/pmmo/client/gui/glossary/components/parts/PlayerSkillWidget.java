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
    private static final int ACCENT_BAR_WIDTH = 3;
    private static final int ACCENT_INSET = 5;

    private static final int LEVEL_RIGHT_PAD = 7;

    private final SkillData skillData;
    private final Color skillColor;
    private final String skillName;
    private final Experience xp;
    private Integer accentColor = null;
    private boolean drawBottom = false;
    Font font = Minecraft.getInstance().font;

    public PlayerSkillWidget(int width, String skillName, SkillData data) {
        super(data.getColor(), width);
        setHeight(24);
        this.skillName = skillName;
        this.skillData = data;
        this.skillColor = new Color(data.getColor());
        this.xp = Core.get(LogicalSide.CLIENT).getData().getXpMap(null).getOrDefault(skillName, new Experience());
    }

    public PlayerSkillWidget withAccent(int color) {
        this.accentColor = color;
        return this;
    }

    public PlayerSkillWidget closeBottom() {
        this.drawBottom = true;
        return this;
    }

    private int contentInset() {return accentColor == null ? 0 : ACCENT_INSET;}

    @Override public void resize() {setHeight(24);}

    @Override
    public boolean applyFilter(GlossaryFilter.Filter filter) {
        String text = filter.getTextFilter();
        if (text == null || text.isEmpty()) return false;
        String lower = text.toLowerCase();
        return !Component.translatable("pmmo." + skillName).getString().toLowerCase().contains(lower)
                && !skillName.toLowerCase().contains(lower);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(BACKGROUND_SPRITES.get(this.isActive(), this.isFocused()), this.getX(), this.getY(), this.width, this.height);
        if (accentColor != null) {
            int argb = 0xFF000000 | accentColor;
            int tintArgb = 0x26000000 | (accentColor & 0x00FFFFFF);
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), tintArgb);
            graphics.fill(this.getX(), this.getY(), this.getX() + ACCENT_BAR_WIDTH, this.getBottom(), argb);
            if (drawBottom) {
                graphics.fill(this.getX(), this.getBottom() - 2, this.getRight(), this.getBottom(), argb);
            }
        }
        int inset = contentInset();
        graphics.blit(skillData.getIcon(), this.getX() + 3 + inset, this.getY() + 3, 18, 18, 0, 0, skillData.getIconSize(), skillData.getIconSize(), skillData.getIconSize(), skillData.getIconSize());

        renderProgressBar(graphics);
        float nameScale = 0.85f;
        graphics.pose().pushPose();
        graphics.pose().translate(this.getX() + 24 + inset, this.getY() + 5, 0);
        graphics.pose().scale(nameScale, nameScale, 1.0f);
        graphics.drawString(font, Component.translatable("pmmo." + skillName), 0, 0, skillColor.getRGB());
        graphics.pose().popPose();
        graphics.drawString(font, String.valueOf(xp.getLevel().getLevel()), (this.getX() + this.width - LEVEL_RIGHT_PAD) - font.width(String.valueOf(xp.getLevel().getLevel())), this.getY() + 5, skillColor.getRGB());
    }

    public void renderProgressBar(GuiGraphics graphics) {
        int inset = contentInset();
        int renderX = this.getX() + 24 + inset;
        int renderY = this.getY() + (font.lineHeight + 6);
        int barWidth = 94 - inset;
        if (this.isFocused()) {
            MutableComponent text = Component.literal("Next lvl: %s xp".formatted(xpToNext()));
            float scale = 0.60f;
            graphics.pose().pushPose();
            graphics.pose().translate(renderX, renderY - 1, 0);
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawString(font, text, 0, 0, this.skillColor.getRGB());
            graphics.pose().popPose();
        }
        else {
            graphics.setColor(skillColor.getRed() / 255.0f, skillColor.getGreen() / 255.0f, skillColor.getBlue() / 255.0f, skillColor.getAlpha() / 255.0f);
            graphics.blit(TEXTURE_LOCATION, renderX, renderY, barWidth, 5, 0.0F, 217.0F, 102, 5, 256, 256);

            long threshold = this.xp.getLevel().getXpToNext();
            int fillWidth = threshold <= 0 ? 0 : (int) Math.min((this.xp.getXp() * (long) barWidth) / threshold, (long) barWidth);
            graphics.blit(TEXTURE_LOCATION, renderX, renderY, fillWidth, 5, 0.0F, 223.0F, 102, 5, 256, 256);

            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private long xpToNext() {
        return this.xp.getLevel().getXpToNext() - this.xp.getXp();
    }
}
