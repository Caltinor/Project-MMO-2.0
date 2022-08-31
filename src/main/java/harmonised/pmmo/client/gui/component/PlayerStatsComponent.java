package harmonised.pmmo.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStatsComponent extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    protected static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/gui/player_stats.png");
    private final Core core = Core.get(LogicalSide.CLIENT);
    protected Minecraft minecraft;
    private boolean visible;
    private int xOffset;
    private int width;
    private int height;
    private boolean widthTooNarrow;
    
    protected PlayerStatsScroller statsScroller;
    
    public static final int IMAGE_WIDTH = 147;
    public static final int IMAGE_HEIGHT = 166;
    private static final int OFFSET_X_POSITION = 86;
    
    public void init(int p_100310_, int p_100311_, Minecraft p_100312_, boolean p_100313_) {
        this.minecraft = p_100312_;
        this.width = p_100310_;
        this.height = p_100311_;
        this.widthTooNarrow = p_100313_;
        
        if (this.visible) {
            this.initVisuals();
        }
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, 100.0D);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i = (this.width - 147) / 2 - this.xOffset;
            int j = (this.height - 166) / 2;
            this.blit(poseStack, i, j, 0, 0, 147, 166);
            this.statsScroller.render(poseStack, mouseX, mouseY, partialTicks);
            poseStack.popPose();
        }
    }
    
    public void initVisuals() {
        this.xOffset = this.widthTooNarrow ? 0 : 86;
        int i = (this.width - 147) / 2 - this.xOffset;
        int j = (this.height - 166) / 2;
        
        this.statsScroller = new PlayerStatsScroller(Minecraft.getInstance(), 131, 150, j + 8, i + 8);
        this.statsScroller.populateAbilities(core, this.minecraft);
    }
    
    public int updateScreenPosition(int p_181402_, int p_181403_) {
        int i;
        if (this.isVisible() && !this.widthTooNarrow) {
            i = 177 + (p_181402_ - p_181403_ - 200) / 2;
        } else {
            i = (p_181402_ - p_181403_) / 2;
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
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (!this.isVisible()) return false;
        return this.statsScroller.mouseClicked(pMouseX, pMouseY, pButton) || GuiEventListener.super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (!this.isVisible()) return false;
        return this.statsScroller.mouseScrolled(pMouseX, pMouseY, pDelta) || GuiEventListener.super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }
    
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (!this.isVisible()) return false;
        return this.statsScroller.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) || GuiEventListener.super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
    
    @Override @NotNull public NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
    @Override public void updateNarration(@NotNull NarrationElementOutput pNarrationElementOutput) { }
    
    static class PlayerStatsScroller extends ScrollPanel {
        private Map<String, Double> modifiers = new HashMap<>();
        private final List<String> skillsKeys = new ArrayList<>();
        private final List<StatComponent> abilities = new ArrayList<>();
        
        public PlayerStatsScroller(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, left, 1, 6,
                0x00FFFFFF, 0x00FFFFFF, 0x88212121, 0xFF000000, 0xFF555555);
        }
        
        public void populateAbilities(Core core, Minecraft minecraft) {
            IDataStorage dataStorage = core.getData();
    
            modifiers = core.getConsolidatedModifierMap(minecraft.player);
            skillsKeys.addAll(dataStorage.getXpMap(null).keySet());
            skillsKeys.sort(Comparator.<String>comparingLong(skill -> dataStorage.getXpRaw(null, skill)).reversed());
            
            for (String skillKey : skillsKeys) {
                long currentXP = dataStorage.getXpRaw(null, skillKey);
                double level = ((DataMirror) dataStorage).getXpWithPercentToNextLevel(currentXP);
                int skillMaxLevel = SkillsConfig.SKILLS.get().getOrDefault(skillKey, SkillData.Builder.getDefault()).getMaxLevel();
                level = level > skillMaxLevel ? skillMaxLevel : level;
                String tempString = DP.dp(Math.floor(level * 100D) / 100D);
                int color = core.getDataConfig().getSkillColor(skillKey);
                
                abilities.add(new StatComponent(minecraft, Component.translatable("pmmo." + skillKey).getString(), color, this.left + 1, this.top));
            }
        }
    
        @Override
        protected void drawPanel(PoseStack poseStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            for (int i = 0; i < abilities.size(); i++) {
                StatComponent component = abilities.get(i);
                int y = (int) (relativeY + (i * (component.getHeight() + 1)) - scrollDistance);
                
                component.setPosition(component.x, y);
                component.render(poseStack, mouseX, mouseY, (float) (i * Math.PI));
            }
        }
        
        @Override
        protected int getContentHeight() {
            return (int) (abilities.size() * (StatComponent.BASE_HEIGHT / 1.3));
        }
        
        @Override @NotNull public NarrationPriority narrationPriority() { return NarrationPriority.NONE; }
        @Override public void updateNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {}
    }
    
    static class StatComponent extends ImageButton {
        private final Minecraft minecraft;
        private final String skillName;
        private final Color skillColor;
        
        private static final int BASE_WIDTH = 123;
        private static final int BASE_HEIGHT = 24;
        
        public StatComponent(Minecraft minecraft, String skillName, int skillColor, int pX, int pY) {
            super(pX, pY, 123, 24, 0, 167, 25, TEXTURE_LOCATION, pButton -> {});
            this.minecraft = minecraft;
            this.skillName = skillName;
            this.skillColor = new Color(skillColor);
        }
    
        @Override
        public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            super.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);
            MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
            TextureAtlasSprite sprite = skillName.equals("Agility") ? mobeffecttexturemanager.get(MobEffects.MOVEMENT_SPEED) :
                skillName.equals("Mining") ? mobeffecttexturemanager.get(MobEffects.DIG_SPEED) : mobeffecttexturemanager.get(MobEffects.ABSORPTION);
            
            RenderSystem.setShaderTexture(0, sprite.atlas().location());
            blit(pPoseStack, this.x + 3, this.y + 3, 0, 18, 18, sprite);
    
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
            RenderSystem.setShaderColor(skillColor.getRed() / 255.0f, skillColor.getGreen() / 255.0f, skillColor.getBlue() / 255.0f, skillColor.getAlpha() / 255.0f);
            blit(pPoseStack, this.x + 24, this.y + (minecraft.font.lineHeight + 6), 93, 5, 0.0F, 217.0F, 102, 5, 256, 256);
    
            GuiComponent.drawString(pPoseStack, minecraft.font, skillName, this.x + 24, this.y + 5, skillColor.getRGB());
        }
    }
}
