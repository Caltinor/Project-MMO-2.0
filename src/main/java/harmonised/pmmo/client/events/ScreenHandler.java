package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.skill_side_panel.SkillsSidePanel;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ScreenHandler {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        int y = event.getListenersList().stream()
                .filter(listener -> listener.getRectangle().left() < SkillsSidePanel.PANEL_WIDTH)
                .map(gel -> gel.getRectangle().bottom())
                .max(Integer::compareTo).orElse(0);
        event.addListener(new SkillsSidePanel(0, y, screen.height - y));
    }
}
