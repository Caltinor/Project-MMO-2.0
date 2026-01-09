package harmonised.pmmo.client.gui.glossary;

import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.api.client.types.GuiEnumGroup;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.component.SelectionWidget;
import harmonised.pmmo.client.gui.glossary.components.CollapsingPanel;
import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.client.gui.glossary.components.DetailScroll;
import harmonised.pmmo.client.gui.glossary.components.panels.AntiCheesePanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.BiomeObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.BlockObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.DimensionObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.EffectsObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.EnchantmentsObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.EntityObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.ItemObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.PerkObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.ServerConfigPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.SkillsConfigPanelWidget;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Glossary extends Screen {
    private final Font font;
    private final Screen priorScreen;
    private GlossaryFilter.Filter inboundFilter;

    public Glossary(GlossaryFilter.Filter filter) {this(null, filter);}
    public Glossary() {this(null, null);}
    public Glossary(Screen priorScreen, GlossaryFilter.Filter filter) {
        super(Component.literal("glossary"));
        this.priorScreen = priorScreen;
        this.font = Minecraft.getInstance().font;
        this.inboundFilter = filter;
    }

    //widgets
    SelectionWidget<SelectionWidget.SelectionEntry<SELECTION>> selectionWidget;
    SelectionWidget<SelectionWidget.SelectionEntry<OBJECT>> objectWidget;
    SelectionWidget<SelectionWidget.SelectionEntry<String>> skillWidget;
    SelectionWidget<SelectionWidget.SelectionEntry<GuiEnumGroup>> enumWidget;
    EditBox searchBar;

    private GlossaryFilter.Filter getFilter(String searchTerm) {
        GlossaryFilter.Filter filter = new GlossaryFilter.Filter(searchTerm);
        if (selectionWidget.getSelected() != null) filter.with(selectionWidget.getSelected().reference);
        if (objectWidget.getSelected() != null) filter.with(objectWidget.getSelected().reference);
        if (skillWidget.getSelected() != null) filter.with(skillWidget.getSelected().reference);
        if (enumWidget.getSelected() != null) filter.with(enumWidget.getSelected().reference);
        return filter;
    }

    public Glossary withFilter(GlossaryFilter.Filter filter) {
        this.inboundFilter = filter;
        return this;
    }

    @Override
    protected void init() {
        super.init();
        ResponsiveLayout outer = new ResponsiveLayout.Impl(this.width-8, this.height, DisplayType.INLINE);
        DetailScroll content = new DetailScroll(this.width/3 -3, 0, this.width, this.height);
        int selectionWidth = this.width/3 - 17;
        buildContent(content, content.getWidth());
        searchBar = new EditBox(font, 8, 11, selectionWidth, 20, Component.literal("search bar"));
        searchBar.setResponder(str -> {content.applyFilter(getFilter(str));});
        selectionWidget = SELECTION.createSelectionWidget(8, 31, selectionWidth, choice -> {
            SELECTION.onSelect(choice, objectWidget);
            content.applyFilter(getFilter(searchBar.getValue()));
        });
        objectWidget = OBJECT.createSelectionWidget(8, 51, selectionWidth, choice -> {
            OBJECT.onSelect(selectionWidget.getSelected(), choice, enumWidget);
            content.applyFilter(getFilter(searchBar.getValue()));
        });
        skillWidget = this.createSkillsWidget(8, 71, selectionWidth, choice -> {content.applyFilter(getFilter(searchBar.getValue()));});
        enumWidget = new SelectionWidget<>(8, 91, selectionWidth, LangProvider.GLOSSARY_DEFAULT_ENUM.asComponent(), choice -> {
            content.applyFilter(getFilter(searchBar.getValue()));
        });
        enumWidget.setEntries(OBJECT.ALL);

        SizeConstraints buttonConstraints = SizeConstraints.builder().absoluteHeight(20).minWidthPercent(1.0).build();

        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        ResponsiveLayout panel = new CollapsingPanel(0, 0, this.width/3, this.height)
                .addCallback(t -> outer.arrangeElements())
                .setPadding(29, 48, 40, 36, scale)

                .addChild(searchBar, PositionType.STATIC.constraint, buttonConstraints)
                .addChild(selectionWidget, PositionType.STATIC.constraint, buttonConstraints)
                .addChild(objectWidget, PositionType.STATIC.constraint, buttonConstraints)
                .addChild(skillWidget, PositionType.STATIC.constraint, buttonConstraints)
                .addChild(enumWidget, PositionType.STATIC.constraint, buttonConstraints)
                .addChild(new Button.Builder(Component.literal("Exit"), button -> Minecraft.getInstance().setScreen(priorScreen))
                        .bounds(8, this.height-29, selectionWidth, 18)
                        .build(), PositionType.STATIC.constraint, buttonConstraints)
                ;


        outer
            .addChild(panel , PositionType.STATIC.constraint, SizeConstraints.builder().internalWidth().build())
            .addChild((ResponsiveLayout) content, PositionType.STATIC.constraint, SizeConstraints.DEFAULT);
        outer.arrangeElements();
        outer.visitWidgets(this::addRenderableWidget);

        if (this.inboundFilter != null) {
            content.applyFilter(inboundFilter);
        }
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        //this exists to make sure ContentScroll isn't registered twice because it's both a layout and a widget.
        if (this.renderables.contains(widget)) return widget;
        return super.addRenderableWidget(widget);
    }

    private SelectionWidget<SelectionWidget.SelectionEntry<String>> createSkillsWidget(int x, int y, int width, Consumer<SelectionWidget.SelectionEntry<String>> selectCallback) {
        var widget = new SelectionWidget<>(x, y, width, LangProvider.GLOSSARY_DEFAULT_SKILL.asComponent(), selectCallback);
        List<SelectionWidget.SelectionEntry<String>> skills = new ArrayList<>(List.of(new SelectionWidget.SelectionEntry<>(Component.literal("Any Skill"), "")));
        skills.addAll(Config.skills().skills().keySet().stream()
                .sorted()
                .map(skill -> new SelectionWidget.SelectionEntry<>(
                        Component.translatable("pmmo."+skill).setStyle(CoreUtils.getSkillStyle(skill)),
                        skill))
                .toList());
        widget.setEntries(skills);
        return widget;
    }

    private void buildContent(ResponsiveLayout layout, int width) {
        layout.addChild((ResponsiveLayout)
                new ServerConfigPanelWidget(width),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build());
        Config.skills().skills().forEach((skill, data) -> layout.addChild((AbstractWidget)
                new SkillsConfigPanelWidget(width, skill, data),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build()
        ));
        layout.addChild((ResponsiveLayout)
                AntiCheesePanelWidget.AFK(0x88394045, width, Config.anticheese().afk(), Config.anticheese().afkSubtract()),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build()
        );
        layout.addChild((ResponsiveLayout)
                AntiCheesePanelWidget.DIM(0x88394045, width, Config.anticheese().diminish()),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build()
        );
        layout.addChild((ResponsiveLayout)
                AntiCheesePanelWidget.NORM(0x88394045, width, Config.anticheese().normal()),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build()
        );
        CreativeModeTabs.searchTab().getDisplayItems().forEach(stack -> layout.addChild((ResponsiveLayout)
                new ItemObjectPanelWidget(0x882e332e, width, stack),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build())
        );
        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        access.lookupOrThrow(Registries.BLOCK).listElements().forEach(ref -> layout.addChild((ResponsiveLayout)
            new BlockObjectPanelWidget(0x882e2f33, width, ref.value()),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build()
        ));
        access.lookupOrThrow(Registries.ENTITY_TYPE).listElements()
                .map(ref -> ref.value().create(Minecraft.getInstance().level))
                .filter(Objects::nonNull)
                .forEach(entity -> layout.addChild((ResponsiveLayout)
                    new EntityObjectPanelWidget(0x88394045, width, entity),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()));
        access.lookupOrThrow(Registries.BIOME).listElements()
                .filter(Objects::nonNull)
                .forEach(biome -> layout.addChild((ResponsiveLayout)
                    new BiomeObjectPanelWidget(0x88394045, width, biome),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()));
        Minecraft.getInstance().getConnection().levels().stream()
                .filter(Objects::nonNull)
                .forEach(key -> layout.addChild((ResponsiveLayout)
                    new DimensionObjectPanelWidget(0x88394045, width, key),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()));
        access.lookupOrThrow(Registries.MOB_EFFECT).listElements()
                .filter(Objects::nonNull)
                .forEach(holder -> layout.addChild((ResponsiveLayout)
                    new EffectsObjectPanelWidget(0x88394045, width, holder.value()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()));
        access.lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .filter(Objects::nonNull)
                .forEach(enchant -> layout.addChild((ResponsiveLayout)
                    new EnchantmentsObjectPanelWidget(0x88394045, width, enchant.value()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()));
        Config.perks().perks().forEach((event, configs) -> layout.addChild((ResponsiveLayout)
                    new PerkObjectPanelWidget(0x88394045, width, event, configs),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()));
    }
}
