package harmonised.pmmo.client.events;

import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.CollapsingPanel;
import harmonised.pmmo.client.gui.glossary.components.DetailScroll;
import harmonised.pmmo.client.gui.glossary.components.parts.PlayerSkillWidget;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ScreenHandler {
    
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof InventoryScreen) {
            int y = event.getListenersList().stream()
                    .filter(listener -> listener.getRectangle().left() < 130)
                    .map(gel -> gel.getRectangle().bottom())
                    .max(Integer::compareTo).orElse(0);
            CollapsingPanel panel = new CollapsingPanel(0, y, 130, screen.height, false);
            DetailScroll scroll = new DetailScroll(0, 0, 103, screen.height) {
                @Override protected boolean scrollbarVisible() {return false;}
            };

            Config.skills().skills().forEach((skill, data) -> scroll.addChild(
                    (AbstractWidget) new PlayerSkillWidget(100, skill, data),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()
            ));
            scroll.arrangeElements();

            panel.addChild((AbstractWidget) scroll, PositionType.STATIC.constraint, SizeConstraints.builder().minHeightPercent(1.0).minWidthPercent(1.0).build());
            panel.arrangeElements();

            event.addListener(panel);
        }
    }
}
