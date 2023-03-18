package harmonised.pmmo.client.gui;

import org.joml.Quaternionf;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.client.gui.component.PMMOButton;
import harmonised.pmmo.client.gui.component.PlayerStatsComponent;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
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
        
        this.addRenderableWidget(new PMMOButton(this, this.leftPos + 104, this.height / 2 - 22, 20, 18, 148, 0, 19));
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
        renderEntityInInventory(i + 51, j + 75, 30, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.minecraft.player);
    }
    
    public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, float pMouseX, float pMouseY, LivingEntity pLivingEntity) {
        float f = (float) Math.atan(pMouseX / 40.0F);
        float f1 = (float) Math.atan(pMouseY / 40.0F);
        renderEntityInInventoryRaw(pPosX, pPosY, pScale, f, f1, pLivingEntity);
    }
    @SuppressWarnings("deprecation")
	public static void renderEntityInInventoryRaw(int pPosX, int pPosY, int pScale, float angleXComponent, float angleYComponent, LivingEntity pLivingEntity) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(pPosX, pPosY, 1050.0D);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float)pScale, (float)pScale, (float)pScale);
        Quaternionf quaternion = (new Quaternionf()).rotateZ(180f);
        Quaternionf quaternion1 = (new Quaternionf()).rotateX(angleYComponent * 20.0F);
        quaternion.mul(quaternion1);
        posestack1.mulPose(quaternion);
        float f2 = pLivingEntity.yBodyRot;
        float f3 = pLivingEntity.getYRot();
        float f4 = pLivingEntity.getXRot();
        float f5 = pLivingEntity.yHeadRotO;
        float f6 = pLivingEntity.yHeadRot;
        pLivingEntity.yBodyRot = 180.0F + angleXComponent * 20.0F;
        pLivingEntity.setYRot(180.0F + angleXComponent * 40.0F);
        pLivingEntity.setXRot(-angleYComponent * 20.0F);
        pLivingEntity.yHeadRot = pLivingEntity.getYRot();
        pLivingEntity.yHeadRotO = pLivingEntity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(pLivingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        pLivingEntity.yBodyRot = f2;
        pLivingEntity.setYRot(f3);
        pLivingEntity.setXRot(f4);
        pLivingEntity.yHeadRotO = f5;
        pLivingEntity.yHeadRot = f6;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
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
