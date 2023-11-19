package harmonised.pmmo.client.gui.component;

import harmonised.pmmo.client.gui.PlayerStatsScreen;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;

public class PMMOButton extends ImageButton {
    private static final WidgetSprites SPRITES = new WidgetSprites(
        new ResourceLocation(Reference.MOD_ID, "pmmo_button"),
        new ResourceLocation(Reference.MOD_ID, "pmmo_button_highlighted")
    );
    
    public PMMOButton(Screen parent, int pX, int pY, int pWidth, int pHeight) {
        super(pX, pY, pWidth, pHeight, SPRITES,
            (button) -> {
                Minecraft minecraft = Minecraft.getInstance();
                
                if (parent instanceof PlayerStatsScreen) {
                    minecraft.setScreen(new InventoryScreen(minecraft.player));
                }
                if (parent instanceof InventoryScreen) {
                    minecraft.setScreen(new PlayerStatsScreen(minecraft.player));
                }
        });
    }
}
