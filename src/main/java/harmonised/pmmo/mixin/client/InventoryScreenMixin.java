package harmonised.pmmo.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.client.gui.component.PlayerStatsComponent;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    @Shadow private boolean widthTooNarrow;
    @Shadow @Final private RecipeBookComponent recipeBookComponent;
    @Shadow private boolean buttonClicked;
    
    @Unique private static final ResourceLocation PLAYER_STATS_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/gui/player_stats.png");
    @Unique private final PlayerStatsComponent playerStatsComponent = new PlayerStatsComponent();
    @Unique private PlayerStatsComponent getPlayerStatsComponent() {
        return playerStatsComponent;
    }
    
    public InventoryScreenMixin(InventoryMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }
    
    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;init(IILnet/minecraft/client/Minecraft;ZLnet/minecraft/world/inventory/RecipeBookMenu;)V"))
    public void pmmo$init(CallbackInfo ci) {
        this.playerStatsComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
        if (this.playerStatsComponent.isVisible()) {
            this.leftPos = this.playerStatsComponent.updateScreenPosition(this.width, this.imageWidth);
        }
        this.addRenderableWidget(new ImageButton(this.leftPos + 126, this.height / 2 - 22, 20, 18, 148, 0, 19, PLAYER_STATS_LOCATION,
            (button) -> {
                if (this.recipeBookComponent.isVisible()) {
                    this.recipeBookComponent.toggleVisibility();
                }
                this.playerStatsComponent.toggleVisibility();
                this.leftPos = this.playerStatsComponent.updateScreenPosition(this.width, this.imageWidth);
                ((ImageButton) button).setPosition(this.leftPos + 104 + 22, this.height / 2 - 22);
            }
        ));
        this.addWidget(this.playerStatsComponent);
    }
    
    @Inject(method = "mouseReleased(DDI)Z", at = @At("HEAD"))
    public void pmmo$recipeButton(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
        if (this.buttonClicked && this.playerStatsComponent.isVisible()) {
            this.playerStatsComponent.toggleVisibility();
        }
    }
    
    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    public void pmmo$mouseClicked(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
        if (this.playerStatsComponent.mouseClicked(pMouseX, pMouseY, pButton)) {
            this.setFocused(this.playerStatsComponent);
            cir.setReturnValue(true);
        }
    }
    
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", ordinal = 1), cancellable = true)
    public void pmmo$render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo ci){
        if (this.playerStatsComponent.isVisible()) {
            if (this.widthTooNarrow) {
                this.playerStatsComponent.toggleVisibility();
            } else {
                this.playerStatsComponent.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
                super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
        }
    }
    
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.playerStatsComponent.mouseScrolled(pMouseX, pMouseY, pDelta)) {
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }
    
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.playerStatsComponent.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
}
