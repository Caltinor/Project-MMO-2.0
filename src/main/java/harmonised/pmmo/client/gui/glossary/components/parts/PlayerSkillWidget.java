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
    private static final int PROGRESS_BAR_FULL_WIDTH = 94;
    private static final int PROGRESS_BAR_HEIGHT = 5;
    private static final int ACCENT_BAR_WIDTH = 3;
    private static final int ACCENT_INSET = 5;
    private static final int ACCENT_BOTTOM_THICKNESS = 2;
    private static final int ACCENT_TINT_ALPHA = 0x26;
    private static final float NAME_SCALE = 0.85f;
    private static final float HOVER_TEXT_SCALE = 0.60f;

    private final SkillData skillData;
    private final Color skillColor;
    private final String skillName;
    private final Experience xp;
    private final Font font = Minecraft.getInstance().font;

    private Integer accentColor = null;
    private boolean drawBottom = false;

    public PlayerSkillWidget(int width, String skillName, SkillData data) {
        super(data.getColor(), width);
        setHeight(HEIGHT);
        this.skillName = skillName;
        this.skillData = data;
        this.skillColor = new Color(data.getColor());
        this.xp = Core.get(LogicalSide.CLIENT).getData().getXpMap(null).getOrDefault(skillName, new Experience());
    }

    public PlayerSkillWidget withAccent(int color) {
        this.accentColor = color;
        return this;
    }

    public PlayerSkillWidget withCloseBottom(boolean closeBottom) {
        this.drawBottom = closeBottom;
        return this;
    }

    public Integer getAccentColor() {
        return accentColor;
    }

    @Override public void resize() {setHeight(HEIGHT);}

    @Override
    public boolean applyFilter(GlossaryFilter.Filter filter) {
        String text = filter.getTextFilter();
        if (text == null || text.isEmpty()) return false;
        return !matchesSearch(skillName, text.toLowerCase());
    }

    public static boolean matchesSearch(String skillKey, String lowerQuery) {
        if (skillKey.toLowerCase().contains(lowerQuery)) return true;
        String translationKey = "pmmo." + skillKey;
        String translated = Component.translatable(translationKey).getString();
        if (translated.equals(translationKey)) return false; // no real translation, avoid matching the literal key
        return translated.toLowerCase().contains(lowerQuery);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        renderAccent(graphics);
        renderIcon(graphics);
        renderProgressBar(graphics);
        renderSkillName(graphics);
        renderLevel(graphics);
    }

    private void renderBackground(GuiGraphics graphics) {
        graphics.blitSprite(BACKGROUND_SPRITES.get(this.isActive(), this.isFocused()), this.getX(), this.getY(), this.width, this.height);
    }

    private void renderAccent(GuiGraphics graphics) {
        if (accentColor == null) return;
        int argb = 0xFF000000 | accentColor;
        int tintArgb = (ACCENT_TINT_ALPHA << 24) | (accentColor & 0x00FFFFFF);
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), tintArgb);
        graphics.fill(this.getX(), this.getY(), this.getX() + ACCENT_BAR_WIDTH, this.getBottom(), argb);
        if (drawBottom) {
            graphics.fill(this.getX(), this.getBottom() - ACCENT_BOTTOM_THICKNESS, this.getRight(), this.getBottom(), argb);
        }
    }

    private void renderIcon(GuiGraphics graphics) {
        int iconSize = skillData.getIconSize();
        graphics.blit(skillData.getIcon(), this.getX() + ICON_LEFT_PAD + contentInset(), this.getY() + ICON_LEFT_PAD,
                ICON_SIZE, ICON_SIZE, 0, 0, iconSize, iconSize, iconSize, iconSize);
    }

    private void renderSkillName(GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(this.getX() + CONTENT_LEFT_PAD + contentInset(), this.getY() + 5, 0);
        graphics.pose().scale(NAME_SCALE, NAME_SCALE, 1.0f);
        graphics.drawString(font, Component.translatable("pmmo." + skillName), 0, 0, skillColor.getRGB());
        graphics.pose().popPose();
    }

    private void renderLevel(GuiGraphics graphics) {
        String level = String.valueOf(xp.getLevel().getLevel());
        int x = this.getX() + this.width - LEVEL_RIGHT_PAD - font.width(level);
        graphics.drawString(font, level, x, this.getY() + 5, skillColor.getRGB());
    }

    private void renderProgressBar(GuiGraphics graphics) {
        int renderX = this.getX() + CONTENT_LEFT_PAD + contentInset();
        int renderY = this.getY() + (font.lineHeight + 6);
        int barWidth = PROGRESS_BAR_FULL_WIDTH - contentInset();
        if (this.isFocused()) {
            renderHoverText(graphics, renderX, renderY);
        } else {
            renderProgressFill(graphics, renderX, renderY, barWidth);
        }
    }

    private void renderHoverText(GuiGraphics graphics, int x, int y) {
        MutableComponent text = Component.literal("Next lvl: %s xp".formatted(xpToNext()));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y - 1, 0);
        graphics.pose().scale(HOVER_TEXT_SCALE, HOVER_TEXT_SCALE, 1.0f);
        graphics.drawString(font, text, 0, 0, skillColor.getRGB());
        graphics.pose().popPose();
    }

    private void renderProgressFill(GuiGraphics graphics, int x, int y, int barWidth) {
        graphics.setColor(skillColor.getRed() / 255.0f, skillColor.getGreen() / 255.0f, skillColor.getBlue() / 255.0f, skillColor.getAlpha() / 255.0f);
        graphics.blit(TEXTURE_LOCATION, x, y, barWidth, PROGRESS_BAR_HEIGHT, 0.0F, 217.0F, 102, 5, 256, 256);
        long threshold = xp.getLevel().getXpToNext();
        int fillWidth = threshold <= 0 ? 0 : (int) Math.min((xp.getXp() * (long) barWidth) / threshold, (long) barWidth);
        graphics.blit(TEXTURE_LOCATION, x, y, fillWidth, PROGRESS_BAR_HEIGHT, 0.0F, 223.0F, 102, 5, 256, 256);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private int contentInset() {
        return accentColor == null ? 0 : ACCENT_INSET;
    }

    private long xpToNext() {
        return xp.getLevel().getXpToNext() - xp.getXp();
    }
}
