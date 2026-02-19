package harmonised.pmmo.client.gui.glossary;

import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.GlossaryFilter;
import harmonised.pmmo.api.client.types.GuiEnumGroup;
import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.component.SelectionWidget;
import harmonised.pmmo.client.gui.glossary.components.CollapsingPanel;
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
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Glossary extends Screen {
    private final Font font;
    private final Screen priorScreen;
    private PanelWidget targetedObject = new PanelWidget(0xFF000000, 400);
    private final List<Positioner<?>> coreDetailData = new ArrayList<>();

    public Glossary() {this(null);}
    public Glossary(Screen priorScreen) {
        super(Component.literal("glossary"));
        this.priorScreen = priorScreen;
        this.font = Minecraft.getInstance().font;
    }

    public Glossary setTargetedObject(PanelWidget widget) {
        this.targetedObject = widget;
        return this;
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

    @Override
    protected void init() {
        super.init();
        ResponsiveLayout outer = new ResponsiveLayout.Impl(this.width-8, this.height, DisplayType.INLINE);
        DetailScroll content = new DetailScroll(this.width / 3 - 3, 0, this.width, this.height);
        if (targetedObject != null)
            content.addChild((AbstractWidget) targetedObject, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
        if (coreDetailData.isEmpty())
            buildContent(coreDetailData, content.getWidth(), content);
        else
            coreDetailData.forEach(content::addChild);
        int selectionWidth = this.width/3 - 17;
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

    private CompletableFuture<List<Positioner.Widget>> widgetAsync(Supplier<List<Positioner.Widget>> supplies, Executor executor) {
        return CompletableFuture.supplyAsync(supplies, executor).handle((result, err) -> {
            if (err != null) {
                err.printStackTrace();
                return new ArrayList<>();
            }
            return result;
        });
    }

    private CompletableFuture<List<Positioner.Layout>> layoutAsync(Supplier<List<Positioner.Layout>> supplies, Executor executor) {
        return CompletableFuture.supplyAsync(supplies, executor).handle((result, err) -> {
            if (err != null) {
                err.printStackTrace();
                return new ArrayList<>();
            }
            return result;
        });
    }

    private void buildContent(List<Positioner<?>> cache, int width, ResponsiveLayout layout) {
        //OMG threads are soooo cool.  you wouldn't get it.
        Executor executor= Executors.newCachedThreadPool();
        List<Positioner.Widget> explanation = this.font.getSplitter()
                .splitLines(LangProvider.LOADING_EXPLANATION.asComponent().getString(), this.width/2, Style.EMPTY)
                .stream().map(fcs -> new Positioner.Widget(
                        new StringWidget(Component.literal(fcs.getString()), font),
                        PositionType.STATIC.constraint,
                        SizeConstraints.builder().absoluteHeight(12).build()))
                .toList();

        explanation.forEach(layout::addChild);

        CompletableFuture<Positioner<?>> server = CompletableFuture.supplyAsync(() -> new Positioner.Layout(new ServerConfigPanelWidget(width),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build()), executor);

        CompletableFuture<List<Positioner.Widget>> skills = widgetAsync(() -> Config.skills().skills().entrySet().stream().map(entry -> new Positioner.Widget(
                    new SkillsConfigPanelWidget(width, entry.getKey(), entry.getValue()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()
            )).toList(), executor);

        CompletableFuture<List<Positioner.Layout>> anticheese = layoutAsync(() -> {
            List<Positioner.Layout> children = new ArrayList<>();
            children.add(new Positioner.Layout(
                    AntiCheesePanelWidget.AFK(0x88394045, width, Config.anticheese().afk(), Config.anticheese().afkSubtract()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()
            ));
            children.add(new Positioner.Layout(
                    AntiCheesePanelWidget.DIM(0x88394045, width, Config.anticheese().diminish()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()
            ));
            children.add(new Positioner.Layout(
                    AntiCheesePanelWidget.NORM(0x88394045, width, Config.anticheese().normal()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build()
            ));
            return children;
        }, executor);

        CompletableFuture<List<Positioner.Layout>> items = layoutAsync(() ->
        CreativeModeTabs.searchTab().getDisplayItems().stream().map(stack -> new Positioner.Layout(
                new ItemObjectPanelWidget(0x882e332e, width, stack),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build())
        ).toList(), executor);

        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        CompletableFuture<List<Positioner.Layout>> blocks = layoutAsync(() ->
        access.lookupOrThrow(Registries.BLOCK).listElements().map(ref -> new Positioner.Layout(
            new BlockObjectPanelWidget(0x882e2f33, width, ref.value()),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build()
        )).toList(), executor);

        CompletableFuture<List<Positioner.Layout>> entities = layoutAsync(() ->
        access.lookupOrThrow(Registries.ENTITY_TYPE).listElements()
            .map(ref -> ref.value().create(Minecraft.getInstance().level, EntitySpawnReason.COMMAND))
            .filter(Objects::nonNull)
            .map(entity -> new Positioner.Layout(
                new EntityObjectPanelWidget(0x88394045, width, entity),
                PositionType.STATIC.constraint,
                SizeConstraints.builder().internalHeight().build())
        ).toList(), executor);

        CompletableFuture<List<Positioner.Layout>> biomes = layoutAsync(() ->
        access.lookupOrThrow(Registries.BIOME).listElements()
                .filter(Objects::nonNull)
                .map(biome -> new Positioner.Layout(
                    new BiomeObjectPanelWidget(0x88394045, width, biome),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build())).toList(), executor);

        CompletableFuture<List<Positioner.Layout>> dimensions = layoutAsync(() ->
        Minecraft.getInstance().getConnection().levels().stream()
                .filter(Objects::nonNull)
                .map(key -> new Positioner.Layout(
                    new DimensionObjectPanelWidget(0x88394045, width, key),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build())).toList(), executor);

        CompletableFuture<List<Positioner.Layout>> effects = layoutAsync(() ->
        access.lookupOrThrow(Registries.MOB_EFFECT).listElements()
                .filter(Objects::nonNull)
                .map(holder -> new Positioner.Layout(
                    new EffectsObjectPanelWidget(0x88394045, width, holder.value()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build())).toList(), executor);

        CompletableFuture<List<Positioner.Layout>> enchantments = layoutAsync(() ->
        access.lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .filter(Objects::nonNull)
                .map(enchant -> new Positioner.Layout(
                    new EnchantmentsObjectPanelWidget(0x88394045, width, enchant.value()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build())).toList(), executor);

        CompletableFuture<List<Positioner.Layout>> perks = layoutAsync(() ->
        Config.perks().perks().entrySet().stream().map(entry -> new Positioner.Layout(
                    new PerkObjectPanelWidget(0x88394045, width, entry.getKey(), entry.getValue()),
                    PositionType.STATIC.constraint,
                    SizeConstraints.builder().internalHeight().build())).toList(), executor);

        CompletableFuture.allOf(
                server,
                skills,
                anticheese,
                items,
                blocks,
                entities,
                biomes,
                dimensions,
                effects,
                enchantments,
                perks
        ).thenRun(() -> {
            try {
                cache.add(server.get());
                cache.addAll(skills.join());
                cache.addAll(anticheese.join());
                cache.addAll(items.join());
                cache.addAll(blocks.join());
                cache.addAll(entities.join());
                cache.addAll(biomes.join());
                cache.addAll(dimensions.join());
                cache.addAll(effects.join());
                cache.addAll(enchantments.join());
                cache.addAll(perks.join());
                explanation.forEach(poser -> poser.get().visible = false);
                cache.forEach(layout::addChild);
            } catch (Exception e) {
                MsLoggy.ERROR.log(MsLoggy.LOG_CODE.GUI, e.getLocalizedMessage());
            }
        });
    }
}
