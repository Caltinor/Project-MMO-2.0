package harmonised.pmmo.client.events;

import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value= Dist.CLIENT)
public class ScreenHandler {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/gui/player_stats.png");
    
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();
    
        if (screen instanceof InventoryScreen invScreen) {
            invScreen.renderables.forEach(widget -> {
                if (widget instanceof ImageButton imgButton) {
                    if (imgButton.resourceLocation.equals(TEXTURE_LOCATION)) {
                        imgButton.setPosition(invScreen.leftPos + 104 + 22, invScreen.height / 2 - 22);
                    } else if (imgButton.resourceLocation.equals(RECIPE_BUTTON_LOCATION)) {
                        imgButton.setPosition(invScreen.leftPos + 104, invScreen.height / 2 - 22);
                    }
                }
            });
        }
    }
}
