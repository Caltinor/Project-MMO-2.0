package harmonised.pmmo.client.gui.skill_side_panel;

import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.CollapsingPanel;
import harmonised.pmmo.client.gui.glossary.components.DetailScroll;
import harmonised.pmmo.client.gui.glossary.components.SearchBox;
import harmonised.pmmo.client.gui.glossary.components.parts.PlayerSkillWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.SkillTypeHeaderWidget;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.config.codecs.SkillTypeData;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SkillsSidePanel extends CollapsingPanel {
    public static final int PANEL_WIDTH = 130;
    private static final int SCROLL_WIDTH = 103;
    private static final int ROW_WIDTH = 100;
    private static final int SEARCH_HEIGHT = 14;
    private static final int VERTICAL_PADDING = 12;

    /** Sorts type entries by their declared {@code order} field, alphabetically by key for ties. */
    private static final Comparator<Map.Entry<String, SkillTypeData>> BY_TYPE_ORDER =
            Comparator.<Map.Entry<String, SkillTypeData>>comparingInt(entry -> entry.getValue().getOrder())
                    .thenComparing(Map.Entry::getKey);

    private final DetailScroll detailScroll;

    public SkillsSidePanel(int x, int y, int height) {
        super(x, y, PANEL_WIDTH, height, Config.SKILL_PANEL_OPEN_BY_DEFAULT.get());
        int scrollHeight = Math.max(0, height - SEARCH_HEIGHT - VERTICAL_PADDING);

        SearchBox searchBar = new SearchBox(ROW_WIDTH, SEARCH_HEIGHT);
        detailScroll = new DetailScroll(0, 0, SCROLL_WIDTH, scrollHeight) {
            @Override protected boolean scrollbarVisible() {return false;}
        };
        populate();

        searchBar.setResponder(text -> {
            String query = text == null ? "" : text.toLowerCase();
            detailScroll.applyFilter(new GlossaryFilter.Filter(query));
            if (!query.isEmpty()) detailScroll.scrollToTop();
        });

        addChild(searchBar, PositionType.STATIC.constraint, fixedRow(SEARCH_HEIGHT));
        addChild((AbstractWidget) detailScroll, PositionType.STATIC.constraint, fixedRow(scrollHeight));
        arrangeElements();
    }

    private static SizeConstraints fixedRow(int height) {
        return SizeConstraints.builder().absoluteHeight(height).minWidthPercent(1.0).build();
    }

    private void addScrollRow(AbstractWidget widget) {
        detailScroll.addChild(widget, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
    }

    /**
     * After a click is routed through this panel, drop focus on every child that
     * wasn't the one clicked — and on every child if the click missed all of them.
     * Keeps the search bar from holding stale focus when the user clicks a skill row
     * or empty panel space.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled) {
            defocusChildrenExceptAt(mouseX, mouseY);
        } else {
            defocusAllChildren();
        }
        return handled;
    }

    private void defocusAllChildren() {
        for (AbstractWidget child : widgets()) {
            if (child.isFocused()) {
                child.setFocused(false);
            }
        }
    }

    private void defocusChildrenExceptAt(double mouseX, double mouseY) {
        for (AbstractWidget child : widgets()) {
            if (!child.isFocused() || child.isMouseOver(mouseX, mouseY)) continue;
            child.setFocused(false);
        }
    }

    /**
     * Builds the body of the scroll: type headers (each owning its skill rows) in
     * configured order, then any leftover untyped skills sorted alphabetically.
     */
    private void populate() {
        Map<String, SkillData> visibleSkills = new LinkedHashMap<>();
        for (Map.Entry<String, SkillData> entry : Config.skills().skills().entrySet()) {
            if (entry.getValue().getShowInList()) visibleSkills.put(entry.getKey(), entry.getValue());
        }

        // Tracks skills already claimed by a type so duplicates across types render only once.
        Set<String> placedSkills = new HashSet<>();

        List<Map.Entry<String, SkillTypeData>> orderedTypes = Config.skills().types().entrySet().stream()
                .sorted(BY_TYPE_ORDER)
                .toList();
        for (Map.Entry<String, SkillTypeData> typeEntry : orderedTypes) {
            String typeKey = typeEntry.getKey();
            SkillTypeData typeData = typeEntry.getValue();
            List<PlayerSkillWidget> rows = new ArrayList<>();
            for (String skillKey : typeData.getSkills()) {
                // Set.add returns false if already present — doubles as dedupe.
                if (!visibleSkills.containsKey(skillKey) || !placedSkills.add(skillKey)) continue;

                rows.add(new PlayerSkillWidget(ROW_WIDTH, skillKey, visibleSkills.get(skillKey)));
            }
            if (!rows.isEmpty()) {
                addScrollRow(new SkillTypeHeaderWidget(ROW_WIDTH, typeKey, typeData, rows));
            }
        }

        // Untyped skills: anything visible that no type claimed, alphabetized.
        List<Map.Entry<String, SkillData>> untyped = visibleSkills.entrySet().stream()
                .filter(entry -> !placedSkills.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .toList();
        for (Map.Entry<String, SkillData> entry : untyped) {
            addScrollRow(new PlayerSkillWidget(ROW_WIDTH, entry.getKey(), entry.getValue()));
        }
    }
}
