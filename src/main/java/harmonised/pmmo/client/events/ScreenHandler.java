package harmonised.pmmo.client.events;

import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.CollapsingPanel;
import harmonised.pmmo.client.gui.glossary.components.DetailScroll;
import harmonised.pmmo.client.gui.glossary.components.parts.PlayerSkillWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.SkillTypeHeaderWidget;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.config.codecs.SkillTypeData;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ScreenHandler {
    private static final int PANEL_WIDTH = 130;
    private static final int SCROLL_WIDTH = 103;
    private static final int ROW_WIDTH = 100;
    private static final int SEARCH_HEIGHT = 14;
    private static final int PANEL_VERTICAL_PADDING = 12;

    private static EditBox currentSearchBar;

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen)) return;
        EditBox bar = currentSearchBar;
        if (bar == null || !bar.isFocused()) return;
        if (!bar.isMouseOver(event.getMouseX(), event.getMouseY())) {
            bar.setFocused(false);
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen)) return;

        int y = event.getListenersList().stream()
                .filter(listener -> listener.getRectangle().left() < PANEL_WIDTH)
                .map(gel -> gel.getRectangle().bottom())
                .max(Integer::compareTo).orElse(0);
        int panelHeight = Math.max(0, screen.height - y);
        int scrollHeight = Math.max(0, panelHeight - SEARCH_HEIGHT - PANEL_VERTICAL_PADDING);

        CollapsingPanel panel = new CollapsingPanel(0, y, PANEL_WIDTH, panelHeight, false);
        EditBox searchBar = new EditBox(Minecraft.getInstance().font, 0, 0, ROW_WIDTH, SEARCH_HEIGHT, Component.literal("Search")) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
                if (!handled && this.isFocused()
                        && keyCode != GLFW.GLFW_KEY_ESCAPE
                        && keyCode != GLFW.GLFW_KEY_TAB) {
                    return true;
                }
                return handled;
            }
        };
        searchBar.setHint(Component.literal("Search..."));
        searchBar.setBordered(true);
        currentSearchBar = searchBar;

        DetailScroll scroll = new DetailScroll(0, 0, SCROLL_WIDTH, scrollHeight) {
            @Override protected boolean scrollbarVisible() {return false;}
        };
        populateSkillList(scroll);
        scroll.arrangeElements();

        searchBar.setResponder(text -> {
            scroll.applyFilter(new GlossaryFilter.Filter(text == null ? "" : text));
            recomputeGroupEdges(scroll);
        });

        panel.addChild(searchBar, PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(SEARCH_HEIGHT).minWidthPercent(1.0).build());
        panel.addChild((AbstractWidget) scroll, PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(scrollHeight).minWidthPercent(1.0).build());
        panel.arrangeElements();

        event.addListener(panel);
    }

    private static void recomputeGroupEdges(DetailScroll scroll) {
        List<AbstractWidget> visibleChildren = new ArrayList<>();
        scroll.getChildren().forEach(poser -> {
            if (poser.get() instanceof AbstractWidget w && w.visible) visibleChildren.add(w);
        });
        for (int i = 0; i < visibleChildren.size(); i++) {
            if (!(visibleChildren.get(i) instanceof PlayerSkillWidget current)) continue;
            if (current.getAccentColor() == null) continue;
            AbstractWidget next = i + 1 < visibleChildren.size() ? visibleChildren.get(i + 1) : null;
            boolean isLastInGroup = !(next instanceof PlayerSkillWidget nextSkill)
                    || nextSkill.getAccentColor() == null
                    || !current.getAccentColor().equals(nextSkill.getAccentColor());
            current.setCloseBottom(isLastInGroup);
        }
    }

    private static void populateSkillList(DetailScroll scroll) {
        Set<String> hidden = Config.skillTypes().hiddenSkills();
        Map<String, SkillData> allSkills = new LinkedHashMap<>();
        Config.skills().skills().forEach((key, data) -> {
            if (!hidden.contains(key)) allSkills.put(key, data);
        });

        List<Map.Entry<String, SkillTypeData>> orderedTypes = Config.skillTypes().skillTypes().entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, SkillTypeData> e) -> e.getValue().getOrder())
                        .thenComparing(Map.Entry::getKey))
                .toList();

        Map<String, Boolean> placed = new LinkedHashMap<>();
        allSkills.keySet().forEach(s -> placed.put(s, false));

        for (Map.Entry<String, SkillTypeData> entry : orderedTypes) {
            String typeKey = entry.getKey();
            SkillTypeData typeData = entry.getValue();
            List<String> groupSkills = new ArrayList<>();
            for (String skill : typeData.getSkills()) {
                if (!allSkills.containsKey(skill)) continue;
                if (Boolean.TRUE.equals(placed.get(skill))) continue;
                groupSkills.add(skill);
                placed.put(skill, true);
            }
            if (groupSkills.isEmpty()) continue;

            scroll.addChild(
                    (AbstractWidget) new SkillTypeHeaderWidget(ROW_WIDTH, typeKey, typeData, groupSkills),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()
            );
            for (int i = 0; i < groupSkills.size(); i++) {
                String skill = groupSkills.get(i);
                PlayerSkillWidget widget = new PlayerSkillWidget(ROW_WIDTH, skill, allSkills.get(skill)).withAccent(typeData.getColor());
                if (i == groupSkills.size() - 1) widget.closeBottom();
                scroll.addChild(
                        (AbstractWidget) widget,
                        PositionType.STATIC.constraint,
                        SizeConstraints.builder().internalHeight().build()
                );
            }
        }

        placed.entrySet().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .sorted()
                .forEach(skill -> scroll.addChild(
                        (AbstractWidget) new PlayerSkillWidget(ROW_WIDTH, skill, allSkills.get(skill)),
                        PositionType.STATIC.constraint,
                        SizeConstraints.builder().internalHeight().build()
                ));
    }
}
