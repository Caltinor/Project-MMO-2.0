package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.component.PMMOButton;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ScreenHandler {
    
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
    
        if (screen instanceof InventoryScreen inv && !Config.HIDE_SKILL_BUTTON.get()) {
            event.addListener(new PMMOButton(inv, inv.getGuiLeft() + 126, inv.height / 2 - 22, 20, 18));
        }
    }
    
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen inventory) {
            inventory.renderables.forEach(widget -> {
                if (widget instanceof PMMOButton button) {
                    button.setPosition(inventory.getGuiLeft() + Config.SKILL_BUTTON_X.get(), inventory.height / 2 + Config.SKILL_BUTTON_Y.get());
                }
            });
        }
    }
}
