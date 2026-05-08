package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.skill_side_panel.SkillsSidePanel;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Adds PMMO's collapsible skills panel to the player's inventory screen.
 * Most of the work lives inside {@link SkillsSidePanel}; this handler just
 * computes where to anchor the panel and registers it as a screen listener.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ScreenHandler {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        // Anchor the panel below any existing widgets that occupy the upper-left
        // area (e.g. effect icons added by other mods). We pick the bottom-most
        // edge among them so we don't visually overlap.
        int anchorY = event.getListenersList().stream()
                .filter(listener -> listener.getRectangle().left() < SkillsSidePanel.PANEL_WIDTH)
                .map(listener -> listener.getRectangle().bottom())
                .max(Integer::compareTo).orElse(0);
        int panelHeight = Math.max(0, screen.height - anchorY);

        event.addListener(new SkillsSidePanel(0, anchorY, panelHeight));
    }
}
