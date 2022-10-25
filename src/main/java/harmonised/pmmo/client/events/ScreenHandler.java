package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.component.PMMOButton;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ScreenHandler {
    
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.InitScreenEvent.Post event) {
        Screen screen = event.getScreen();
    
        if (screen instanceof InventoryScreen inv) {
            event.addListener(new PMMOButton(inv, inv.leftPos + 126, inv.height / 2 - 22, 20, 18, 148, 0, 19));
        }
    }
    
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.DrawScreenEvent.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen inventory) {
            inventory.renderables.forEach(widget -> {
                if (widget instanceof PMMOButton button) {
                    button.setPosition(inventory.leftPos + 126, inventory.height / 2 - 22);
                }
            });
        }
    }
}
