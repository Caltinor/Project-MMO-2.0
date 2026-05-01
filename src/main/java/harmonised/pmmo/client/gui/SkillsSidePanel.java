package harmonised.pmmo.client.gui;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

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

    private final DetailScroll scroll;

    public SkillsSidePanel(int x, int y, int height) {
        super(x, y, PANEL_WIDTH, height, false);
        int scrollHeight = Math.max(0, height - SEARCH_HEIGHT - VERTICAL_PADDING);

        SearchBox searchBar = new SearchBox(ROW_WIDTH, SEARCH_HEIGHT);
        scroll = createScroll(scrollHeight);
        populate();
        scroll.arrangeElements();

        searchBar.setResponder(text -> {
            scroll.applyFilter(new GlossaryFilter.Filter(text == null ? "" : text));
            recomputeGroupEdges();
        });

        addChild(searchBar, PositionType.STATIC.constraint, fixedRow(SEARCH_HEIGHT));
        addChild((AbstractWidget) scroll, PositionType.STATIC.constraint, fixedRow(scrollHeight));
        arrangeElements();
    }

    private static DetailScroll createScroll(int height) {
        return new DetailScroll(0, 0, SCROLL_WIDTH, height) {
            @Override protected boolean scrollbarVisible() {return false;}
        };
    }

    private static SizeConstraints fixedRow(int height) {
        return SizeConstraints.builder().absoluteHeight(height).minWidthPercent(1.0).build();
    }

    private void addScrollRow(AbstractWidget widget) {
        scroll.addChild(widget, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
    }

    private void populate() {
        Map<String, SkillData> visibleSkills = collectVisibleSkills();
        Set<String> placed = new HashSet<>();
        for (Map.Entry<String, SkillTypeData> entry : orderedTypes()) {
            addTypeGroup(entry.getKey(), entry.getValue(), visibleSkills, placed);
        }
        addUntypedSkills(visibleSkills, placed);
    }

    private static Map<String, SkillData> collectVisibleSkills() {
        Map<String, SkillData> visible = new LinkedHashMap<>();
        Config.skills().skills().forEach((key, data) -> {
            if (data.getShowInList()) visible.put(key, data);
        });
        return visible;
    }

    private static List<Map.Entry<String, SkillTypeData>> orderedTypes() {
        return Config.skills().types().entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, SkillTypeData> e) -> e.getValue().getOrder())
                        .thenComparing(Map.Entry::getKey))
                .toList();
    }

    private void addTypeGroup(String typeKey, SkillTypeData typeData, Map<String, SkillData> visibleSkills, Set<String> placed) {
        List<String> groupSkills = new ArrayList<>();
        for (String skill : typeData.getSkills()) {
            if (!visibleSkills.containsKey(skill) || !placed.add(skill)) continue;
            groupSkills.add(skill);
        }
        if (groupSkills.isEmpty()) return;

        addScrollRow(new SkillTypeHeaderWidget(ROW_WIDTH, typeKey, typeData, groupSkills));
        for (int i = 0; i < groupSkills.size(); i++) {
            String skill = groupSkills.get(i);
            boolean isLast = i == groupSkills.size() - 1;
            addScrollRow(new PlayerSkillWidget(ROW_WIDTH, skill, visibleSkills.get(skill))
                    .withAccent(typeData.getColor())
                    .withCloseBottom(isLast));
        }
    }

    private void addUntypedSkills(Map<String, SkillData> visibleSkills, Set<String> placed) {
        visibleSkills.entrySet().stream()
                .filter(e -> !placed.contains(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> addScrollRow(new PlayerSkillWidget(ROW_WIDTH, e.getKey(), e.getValue())));
    }

    /**
     * Recompute which row in each group draws its bottom edge. After a filter pass
     * a row that was originally last in its group may no longer be visible — the
     * new last visible row of each color group should close the frame.
     */
    private void recomputeGroupEdges() {
        List<AbstractWidget> visible = scroll.getChildren().stream()
                .map(p -> p.get() instanceof AbstractWidget w ? w : null)
                .filter(w -> w != null && w.visible)
                .toList();
        for (int i = 0; i < visible.size(); i++) {
            if (!(visible.get(i) instanceof PlayerSkillWidget current) || current.getAccentColor() == null) continue;
            AbstractWidget next = i + 1 < visible.size() ? visible.get(i + 1) : null;
            current.withCloseBottom(isGroupBoundary(current, next));
        }
    }

    private static boolean isGroupBoundary(PlayerSkillWidget current, AbstractWidget next) {
        if (!(next instanceof PlayerSkillWidget nextSkill)) return true;
        if (nextSkill.getAccentColor() == null) return true;
        return !current.getAccentColor().equals(nextSkill.getAccentColor());
    }

    /**
     * EditBox for the side panel:
     *   - keyPressed consumes any key while focused (except ESC/TAB) so inventory hotkeys like 'e' don't close the screen
     *   - renders a clear (×) glyph at the right edge while there is text
     *   - clicking the × clears the field
     */
    private static class SearchBox extends EditBox {
        private static final int CLEAR_GLYPH_SIZE = 8;
        private static final int CLEAR_GLYPH_RIGHT_MARGIN = 2;
        private static final int CLEAR_COLOR_DEFAULT = 0xFFAAAAAA;
        private static final int CLEAR_COLOR_HOVER = 0xFFFFFFFF;

        SearchBox(int width, int height) {
            super(Minecraft.getInstance().font, 0, 0, width, height, Component.literal("Search"));
            setHint(Component.literal("Search..."));
            setBordered(true);
        }

        private int clearGlyphLeft() {
            return this.getX() + this.width - CLEAR_GLYPH_SIZE - CLEAR_GLYPH_RIGHT_MARGIN;
        }

        private int clearGlyphTop() {
            return this.getY() + (this.height - CLEAR_GLYPH_SIZE) / 2;
        }

        private boolean isOverClearGlyph(double mx, double my) {
            if (this.getValue().isEmpty()) return false;
            int left = clearGlyphLeft();
            int top = clearGlyphTop();
            return mx >= left && mx < left + CLEAR_GLYPH_SIZE
                    && my >= top && my < top + CLEAR_GLYPH_SIZE;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
            if (handled || !isFocused()) return handled;
            return keyCode != GLFW.GLFW_KEY_ESCAPE && keyCode != GLFW.GLFW_KEY_TAB;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            if (this.getValue().isEmpty()) return;
            int color = isOverClearGlyph(mouseX, mouseY) ? CLEAR_COLOR_HOVER : CLEAR_COLOR_DEFAULT;
            graphics.drawString(Minecraft.getInstance().font, "×", clearGlyphLeft() + 1, clearGlyphTop(), color, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isOverClearGlyph(mouseX, mouseY)) {
                this.setValue("");
                this.setFocused(true);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
