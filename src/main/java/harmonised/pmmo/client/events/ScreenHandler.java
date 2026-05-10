package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.skill_side_panel.SkillsSidePanel;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ScreenHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        int detectedTop = event.getListenersList().stream()
                .filter(listener -> listener.getRectangle().left() < SkillsSidePanel.PANEL_WIDTH)
                .map(listener -> listener.getRectangle().bottom())
                .max(Integer::compareTo).orElse(0);
        int anchorY = detectedTop + Math.max(0, Config.SKILL_PANEL_TOP_MARGIN.get());
        int bottomMargin = Math.max(0, Config.SKILL_PANEL_BOTTOM_MARGIN.get());
        int panelHeight = Math.max(0, screen.height - anchorY - bottomMargin);

        event.addListener(new SkillsSidePanel(0, anchorY, panelHeight));
    }
}
