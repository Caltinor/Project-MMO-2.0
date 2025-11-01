package harmonised.pmmo.client.gui;

import harmonised.pmmo.client.gui.component.PMMOButton;
import harmonised.pmmo.client.gui.component.PlayerStatsComponent;
import harmonised.pmmo.config.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.player.Player;

public class PlayerStatsScreen extends InventoryScreen {
    private final PlayerStatsComponent playerStatsComponent = new PlayerStatsComponent();
    
    public float xMouse;
    public float yMouse;
    public boolean widthTooNarrow;
    
    public PlayerStatsScreen(Player player) {
        super(player);
        this.titleLabelX = 97;
    }
    
    @Override
    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        
        this.playerStatsComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
        this.playerStatsComponent.toggleVisibility();
        this.leftPos = this.playerStatsComponent.updateScreenPosition(this.width, this.imageWidth);

        this.addRenderableWidget(new PMMOButton(this, this.leftPos + Config.SKILL_BUTTON_X.get() - 22, this.height / 2 +Config.SKILL_BUTTON_Y.get(), 20, 18));
        this.addWidget(this.playerStatsComponent);
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 181, this.height / 2 - 22);
    }

    public void containerTick() {
        this.playerStatsComponent.tick();
    }
    
    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        this.renderBackground(graphics, pMouseX, pMouseY, pPartialTick);
        if (this.playerStatsComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(graphics, pPartialTick, pMouseX, pMouseY);
            this.playerStatsComponent.render(graphics, pMouseX, pMouseY, pPartialTick);
        } else {
            this.playerStatsComponent.render(graphics, pMouseX, pMouseY, pPartialTick);
            super.render(graphics, pMouseX, pMouseY, pPartialTick);
        }
    
        this.renderTooltip(graphics, pMouseX, pMouseY);
        this.xMouse = (float)pMouseX;
        this.yMouse = (float)pMouseY;
    }
    
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return (!this.widthTooNarrow || !this.playerStatsComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }
    
    @Override
    public boolean mouseClicked(MouseButtonEvent mouseEvent, boolean pButton) {
        if (this.playerStatsComponent.mouseClicked(mouseEvent, pButton)) {
            this.setFocused(this.playerStatsComponent);
            return true;
        }
        return (!this.widthTooNarrow || !this.playerStatsComponent.isVisible()) && super.mouseClicked(mouseEvent, pButton);
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent mouseEvent, double pDragX, double pDragY) {
        if (this.playerStatsComponent.mouseDragged(mouseEvent, pDragX, pDragY)) {
            return true;
        }
        return super.mouseDragged(mouseEvent, pDragX, pDragY);
    }
    
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta, double other) {
        if (this.playerStatsComponent.mouseScrolled(pMouseX, pMouseY, pDelta, other)) {
            return true;
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta, other);
    }

    public PlayerStatsComponent getPlayerStatsComponent() {
        return playerStatsComponent;
    }
}
