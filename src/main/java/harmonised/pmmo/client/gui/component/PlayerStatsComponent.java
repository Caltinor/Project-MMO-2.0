package harmonised.pmmo.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerStatsComponent extends AbstractWidget {
    protected static final ResourceLocation TEXTURE_LOCATION = Reference.rl("textures/gui/player_stats.png");
    protected static final Core core = Core.get(LogicalSide.CLIENT);
    protected Minecraft minecraft;
    private boolean visible;
    private int xOffset;
    private int width;
    private int height;
    private boolean widthTooNarrow;
    private int timesInventoryChanged;
    
    protected PlayerStatsScroller statsScroller;
    
    public static final int IMAGE_WIDTH = 147;
    public static final int IMAGE_HEIGHT = 166;
    private static final int OFFSET_X_POSITION = 86;
    
    public PlayerStatsComponent() {
		super(0, 0, 0, 0, Component.empty());
	}

    public void init(int width, int height, Minecraft minecraft, boolean widthTooNarrow) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
        this.widthTooNarrow = widthTooNarrow;
        this.timesInventoryChanged = minecraft.player.getInventory().getTimesChanged();
        
        if (this.visible) {
            this.initVisuals();
        }
    }
    
    public void tick() {
        if (this.isVisible()) {
            if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
                this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
            }
        }
    }
    
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible()) {
            int i = (this.width - IMAGE_WIDTH) / 2 - this.xOffset;
            int j = (this.height - IMAGE_HEIGHT) / 2;
            graphics.blit(TEXTURE_LOCATION,
                    i, j, i + IMAGE_WIDTH, j + IMAGE_HEIGHT
                    , 0f, IMAGE_WIDTH/256f, 0f, IMAGE_HEIGHT/256f);
            this.statsScroller.render(graphics, mouseX, mouseY, partialTicks);
        }
    }
    
    public void initVisuals() {
        this.xOffset = this.widthTooNarrow ? 0 : OFFSET_X_POSITION;
        int i = (this.width - IMAGE_WIDTH) / 2 - this.xOffset;
        int j = (this.height - IMAGE_HEIGHT) / 2;
        
        this.statsScroller = new PlayerStatsScroller(Minecraft.getInstance(), 131, 150, j + 8, i + 8);
        this.statsScroller.populateAbilities(core, this.minecraft);
    }
    
    public int updateScreenPosition(int x, int y) {
        int i;
        if (this.isVisible() && !this.widthTooNarrow) {
            i = 177 + (x - y - 200) / 2;
        } else {
            i = (x - y) / 2;
        }
        
        return i;
    }
    
    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }
    
    public boolean isVisible() {
        return this.visible;
    }
    
    protected void setVisible(boolean visible) {
        if (visible) {
            this.initVisuals();
        }
        this.visible = visible;
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent mouseEvent, boolean doubleClicked) {
        if (!this.isVisible()) return false;
        return this.statsScroller.mouseClicked(mouseEvent, doubleClicked) || super.mouseClicked(mouseEvent, doubleClicked);
    }
    
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta, double delta) {
        if (!this.isVisible()) return false;
        return this.statsScroller.mouseScrolled(pMouseX, pMouseY, pDelta, delta) || super.mouseScrolled(pMouseX, pMouseY, pDelta, delta);
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent mouseEvent, double pDragX, double pDragY) {
        if (!this.isVisible()) return false;
        return this.statsScroller.mouseDragged(mouseEvent, pDragX, pDragY) || super.mouseDragged(mouseEvent, pDragX, pDragY);
    }
    
    @Override @NotNull public NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
    @Override protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) { }
    
    static class PlayerStatsScroller extends ScrollPanel {
        private final List<String> skillsKeys = new ArrayList<>();
        private final List<StatComponent> abilities = new ArrayList<>();
        
        public PlayerStatsScroller(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, left, 1, 6,
                0x00FFFFFF, 0x00FFFFFF, 0x88212121);
        }
        
        public void populateAbilities(Core core, Minecraft minecraft) {
            IDataStorage dataStorage = core.getData();

            this.skillsKeys.addAll(dataStorage.getXpMap(null).keySet().stream()
                    .filter(skill -> Config.skills().get(skill).getShowInList())
                    .toList());
            this.skillsKeys.sort(Comparator.<String>comparingLong(skill -> dataStorage.getXp(null, skill)).reversed());
            
            for (String skillKey : this.skillsKeys) {
                SkillData skillData = Config.skills().get(skillKey);
                this.abilities.add(new StatComponent(minecraft, this.left + 1, this.top, skillKey, skillData));
            }
        }
    
        @Override
        protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, int mouseX, int mouseY) {
            for (StatComponent component : this.abilities) {
                component.setPosition(component.getX(), relativeY);
                component.render(guiGraphics, mouseX, mouseY, Minecraft.getInstance().getFrameTimeNs());
                
                relativeY += StatComponent.BASE_HEIGHT + this.border;
            }
        }
        
        @Override
        protected int getScrollAmount() {
            return StatComponent.BASE_HEIGHT + this.border;
        }
        
        @Override
        protected int getContentHeight() {
            int height = this.abilities.size() * (StatComponent.BASE_HEIGHT + this.border);
            if (height < this.bottom - this.top - 1) {
                height = this.bottom - this.top - 1;
            }
            return height;
        }
        
        @Override @NotNull public NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
        @Override public void updateNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {}
    }
    
    static class StatComponent extends ImageButton {
        private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
            Reference.rl("stat_background"),
            Reference.rl("stat_background_highlighted")
        );
        
        private final Minecraft minecraft;
    
        private final String skillName;
        private final SkillData skillData;
        
        private final Color skillColor;
        private final Experience xp;
        
        private static final int BASE_HEIGHT = 24;
        
        public StatComponent(Minecraft minecraft, int pX, int pY, String skillKey, SkillData skillData) {
            super(pX, pY, 123, 24, BACKGROUND_SPRITES, pButton -> {});
            this.minecraft = minecraft;
            this.skillName = Component.translatable("pmmo." + skillKey).getString();
            this.skillData = skillData;
            
            this.skillColor = new Color(skillData.getColor());
            this.xp = core.getData().getXpMap(null).getOrDefault(skillKey, new Experience());
        }
    
        @Override
        public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
            graphics.blit(RenderPipelines.GUI_TEXTURED, skillData.getIcon(),
                    this.getX() + 3, this.getY() + 3,
                    0, 0,
                    16, 16,
                    skillData.getIconSize(), skillData.getIconSize(),
                    skillData.getIconSize(), skillData.getIconSize());

            renderProgressBar(graphics);
            graphics.drawString(minecraft.font, skillName, this.getX() + 24, this.getY() + 5, skillColor.getRGB());
            graphics.drawString(minecraft.font, String.valueOf(xp.getLevel().getLevel()), (this.getX() + this.width - 5) - minecraft.font.width(String.valueOf(xp.getLevel().getLevel())), this.getY() + 5, skillColor.getRGB());
            if (pMouseX > this.getX() + 3 && pMouseX < this.getX() + 19
                && pMouseY > this.getY() + 3 && pMouseY < this.getY() + 19)
                renderPerkTooltip(graphics, pMouseX, pMouseY, skillName);
        }
        
        public void renderProgressBar(GuiGraphics graphics) {
            int renderX = this.getX() + 24;
            int renderY = this.getY() + (minecraft.font.lineHeight + 6);
            if (this.isHovered()) {
                MutableComponent text = Component.literal("%s => %s".formatted(xpToNext(), this.xp.getLevel().getLevel() +1));
                graphics.drawString(minecraft.font, text, renderX, renderY-1, this.skillColor.getRGB());
            }
            else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION,
                        renderX, renderY,
                        0, 217,
                        94, 5,
                        101, 5,256, 256);

                float percent = (float)this.xp.getXp() / (float)this.xp.getLevel().getXpToNext() ;
                int xp = (int) (percent * 94F);
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION,
                        renderX, renderY,
                        0, 223,
                        xp, 5,
                        101, 5, 256, 256,(0xFF << 24) | skillColor.getRGB());
            }
        }

        public void renderPerkTooltip(GuiGraphics graphics, int mouseX, int mouseY, String skill) {
            List<FormattedCharSequence> holder = new ArrayList<>();
            Player player = Minecraft.getInstance().player;
            Config.perks().perks().getOrDefault(EventType.SKILL_UP, new ArrayList<>()).stream()
                .filter(nbt -> skill.isEmpty() || !nbt.contains(APIUtils.SKILLNAME) || LangProvider.skill(nbt.getString(APIUtils.SKILLNAME).get()).getString().equals(skill))
                .forEach(nbt -> {
                    ResourceLocation perkID = Reference.of(nbt.getString("perk").get());
                    nbt.putLong(APIUtils.SKILL_LEVEL, nbt.contains(APIUtils.SKILLNAME)
                            ? Core.get(player.level()).getData().getLevel(nbt.getString(APIUtils.SKILLNAME).get(), player.getUUID())
                            : 0);
                    holder.add(Component.translatable("perk."+perkID.getNamespace()+"."+perkID.getPath()).getVisualOrderText());
                    holder.add(core.getPerkRegistry().getDescription(perkID).copy().getVisualOrderText());
                    core.getPerkRegistry().getStatusLines(perkID, player, nbt).stream()
                        .map(component -> component.getVisualOrderText())
                        .forEach(holder::add);
            });
            graphics.setTooltipForNextFrame(holder, mouseX, mouseY);
        }

        private long xpToNext() {
            return this.xp.getLevel().getXpToNext() - this.xp.getXp();
        }
    }
}
