package harmonised.pmmo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.client.gui.component.PMMOButton;
import harmonised.pmmo.client.gui.component.PlayerStatsComponent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

public class PlayerStatsScreen extends EffectRenderingInventoryScreen<InventoryMenu> {
    public static final ResourceLocation PLAYER_STATS_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/gui/player_stats.png");
    private final PlayerStatsComponent playerStatsComponent = new PlayerStatsComponent();
    
    public float xMouse;
    public float yMouse;
    public boolean widthTooNarrow;
    
    public PlayerStatsScreen(Player player) {
        super(player.inventoryMenu, player.getInventory(), Component.translatable("container.crafting"));
        this.passEvents = true;
        this.titleLabelX = 97;
    }
    
    @Override
    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        
        this.playerStatsComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
        this.playerStatsComponent.toggleVisibility();
        this.leftPos = this.playerStatsComponent.updateScreenPosition(this.width, this.imageWidth);
        
        this.addRenderableWidget(new PMMOButton(this, this.leftPos + Config.SKILL_BUTTON_X.get() - 22, this.height / 2 +Config.SKILL_BUTTON_Y.get(), 20, 18, 148, 0, 19));
        this.addWidget(this.playerStatsComponent);
    }
    
    protected void containerTick() {
        this.playerStatsComponent.tick();
    }
    
    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        if (this.playerStatsComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);
            this.playerStatsComponent.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        } else {
            this.playerStatsComponent.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
        this.xMouse = (float)pMouseX;
        this.yMouse = (float)pMouseY;
    }
    
    protected void renderLabels(PoseStack pPoseStack, int pX, int pY) {
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }
    
    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int i = this.leftPos;
        int j = this.topPos;
        GuiComponent.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderEntityInInventoryFollowsMouse(pPoseStack, i + 51, j + 75, 30, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.minecraft.player);
    }
    
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return (!this.widthTooNarrow || !this.playerStatsComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.playerStatsComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.setFocused(this.playerStatsComponent);
            return true;
        }
        return (!this.widthTooNarrow || !this.playerStatsComponent.isVisible()) && super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.playerStatsComponent.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
    
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.playerStatsComponent.mouseScrolled(pMouseX, pMouseY, pDelta)) {
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }
    
    public PlayerStatsComponent getPlayerStatsComponent() {
        return playerStatsComponent;
    }
}
