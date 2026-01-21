package harmonised.pmmo.client.gui.glossary.components.parts;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
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
    private final SkillData skillData;
    private final Color skillColor;
    private final String skillName;
    private final Experience xp;
    Font font = Minecraft.getInstance().font;

    public PlayerSkillWidget(int width, String skillName, SkillData data) {
        super(data.getColor(), width);
        setHeight(24);
        this.skillName = skillName;
        this.skillData = data;
        this.skillColor = new Color(data.getColor());
        this.xp = Core.get(LogicalSide.CLIENT).getData().getXpMap(null).getOrDefault(skillName, new Experience());
    }

    @Override public void resize() {setHeight(24);}

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(RenderPipelines.GUI, BACKGROUND_SPRITES.get(this.isActive(), this.isFocused()), this.getX(), this.getY(), this.width, this.height);
        graphics.blit(RenderPipelines.GUI, skillData.getIcon(), this.getX() + 3, this.getY() + 3, 18, 18, 0, 0, skillData.getIconSize(), skillData.getIconSize(), skillData.getIconSize(), skillData.getIconSize());

        renderProgressBar(graphics);
        graphics.drawString(font, skillName, this.getX() + 24, this.getY() + 5, skillColor.getRGB());
        graphics.drawString(font, String.valueOf(xp.getLevel().getLevel()), (this.getX() + this.width - 5) - font.width(String.valueOf(xp.getLevel().getLevel())), this.getY() + 5, skillColor.getRGB());
    }

    public void renderProgressBar(GuiGraphics graphics) {
        int renderX = this.getX() + 24;
        int renderY = this.getY() + (font.lineHeight + 6);
        if (this.isFocused()) {
            MutableComponent text = Component.literal("%s => %s".formatted(xpToNext(), this.xp.getLevel().getLevel() +1));
            graphics.drawString(font, text, renderX, renderY-1, this.skillColor.getRGB());
        }
        else {
//            graphics.setColor(skillColor.getRed() / 255.0f, skillColor.getGreen() / 255.0f, skillColor.getBlue() / 255.0f, skillColor.getAlpha() / 255.0f);
            graphics.blit(RenderPipelines.GUI, TEXTURE_LOCATION, renderX, renderY, 0, 217, 94, 5, 102, 5, 256, 256);

            float percent = 100.0f / xpToNext();
            int xp = (int) Math.min(Math.floor(percent * this.xp.getXp()), 94);
            graphics.blit(RenderPipelines.GUI, TEXTURE_LOCATION, renderX, renderY, 0, 223, xp, 5, 102, 5, 256, 256);

//            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private long xpToNext() {
        return this.xp.getLevel().getXpToNext() - this.xp.getXp();
    }
}
