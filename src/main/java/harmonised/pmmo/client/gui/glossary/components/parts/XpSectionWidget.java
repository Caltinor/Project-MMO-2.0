package harmonised.pmmo.client.gui.glossary.components.parts;

import com.mojang.datafixers.util.Pair;
import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class XpSectionWidget extends ReactiveWidget {
    Map<EventType, Map<String, Long>> xpAwards = new HashMap<>();
    List<String> skills = new ArrayList<>();
    private XpSectionWidget(Map<EventType, Map<String, Long>> nbtXp, Function<ResponsiveLayout, Map<EventType, Map<String, Long>>> layoutBuilder) {
        super(0, 0, 0, 0);
        //store them in the widget for use in the filter
        xpAwards.putAll(nbtXp);
        xpAwards.putAll(layoutBuilder.apply(this));
        skills = xpAwards.entrySet().stream().map(entry -> entry.getValue().keySet()).flatMap(Set::stream).toList();
        this.setHeight((getChildren().size() * 12) + 2);
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();
    private static Map<EventType, Map<String, Long>> buildLayout(ResponsiveLayout layout, Map<EventType, Map<String, Long>> nbtReqs, ObjectType type, ResourceLocation id) {
        Font font = Minecraft.getInstance().font;
        Core core = Core.get(LogicalSide.CLIENT);
        ObjectData objectData = (ObjectData) core.getLoader().getLoader(type).getData(id);
        Map<EventType, Map<String, Long>> regXP = new HashMap<>();
        List<Positioner<?>> contentWidgets = new ArrayList<>();
        for (EventType xpType : EventType.values()) {
            if (EventType.is(EventType.DAMAGE_TYPES, xpType)) continue;
            Map<String, Long> skillmap;
            if (!nbtReqs.getOrDefault(xpType, new HashMap<>()).isEmpty()) {
                //TODO replace with a widget that when hovered gives the NBT logic via tooltip
                contentWidgets.addAll(setSkills(nbtReqs.get(xpType), new StringWidget(xpType.tooltipTranslation.asComponent().append(" [NBT]"), font).alignLeft(), font));
            }
            else if (!(skillmap = core.getCommonXpAwardData(new HashMap<>(), xpType, id, Minecraft.getInstance().player, type, new CompoundTag())).isEmpty()){
                regXP.put(xpType, skillmap);
                MutableComponent combine = xpType.tooltipTranslation.asComponent();
                if (skillmap.size() == 1) {
                    String skill = skillmap.keySet().iterator().next();
                    if (skillmap.get(skill) > 0)
                        combine.append(" ").append(LangProvider.skill(skill)).append(": ").append(skillmap.get(skill).toString());
                    contentWidgets.add(new Positioner.Widget(new StringWidget(combine, font).alignLeft(), PositionType.STATIC.constraint, textConstraint));
                }
                else {
                    contentWidgets.addAll(setSkills(skillmap, new StringWidget(combine, font).alignLeft(), font));
                }
            }
        }
        for (EventType dmgType : EventType.DAMAGE_TYPES) {
            var dmgMap = objectData.damageXpValues().getOrDefault(dmgType, new HashMap<>());
            if (!dmgMap.isEmpty()) {
                contentWidgets.add(new Positioner.Widget(new StringWidget(dmgType.tooltipTranslation.asComponent(), font).alignLeft(), PositionType.STATIC.constraint, textConstraint));
                for (String dmgKey : dmgMap.keySet()) {
                    MutableComponent prefix = Component.literal("   ");
                    var skillmap = dmgMap.getOrDefault(dmgKey, new HashMap<>());
                    MutableComponent combine = prefix.copy().append(dmgKey);
                    if (skillmap.size() == 1) {
                        String skill = skillmap.keySet().iterator().next();
                        if (skillmap.get(skill) > 0)
                            combine.append(" ").append(LangProvider.skill(skill)).append(": ").append(skillmap.get(skill).toString());
                        contentWidgets.add(new Positioner.Widget(new StringWidget(combine, font).alignLeft(), PositionType.STATIC.constraint, textConstraint));
                    }
                    else {
                        contentWidgets.addAll(setSkills(skillmap, new StringWidget(combine, font).alignLeft(), font, true));
                    }
                }
            }
        }
        if (!contentWidgets.isEmpty()) {
            layout.addChild(new StringWidget(LangProvider.EVENT_HEADER.asComponent().withStyle(ChatFormatting.GREEN), font).alignLeft(), PositionType.STATIC.constraint, textConstraint);
            contentWidgets.forEach(layout::addChild);
            layout.addChild(new DividerWidget(100, 1, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        }
        return regXP;
    }

    private static List<Positioner<?>> setSkills(Map<String, Long> map, AbstractWidget header, Font font) {
        return setSkills(map, header, font, false);
    }
    private static List<Positioner<?>> setSkills(Map<String, Long> map, AbstractWidget header, Font font, boolean dmgEntry) {
        List<Positioner<?>> skillWidgets = new ArrayList<>(List.of(new Positioner.Widget(header, PositionType.STATIC.constraint, textConstraint)));
        MutableComponent prefix = Component.literal(map.values().stream().filter(s -> s > 0).count() > 1 ? "   " : "").append(dmgEntry? "   ": "");
        map.forEach((skill, value) -> {
            if (value > 0)
                skillWidgets.add(new Positioner.Widget(new StringWidget(prefix.copy().append(LangProvider.skill(skill)).append(": ").append(value.toString()), font).alignLeft(),
                        PositionType.STATIC.constraint, textConstraint));
        });
        return skillWidgets.size() > 1 ? skillWidgets : new ArrayList<>();
    }

    public static XpSectionWidget create(ItemStack stack) {
        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        ResourceLocation id = RegistryUtil.getId(access, stack);
        var nbtXp = Arrays.stream(EventType.ITEM_APPLICABLE_EVENTS)
                .map(xpType -> Pair.of(xpType, Core.get(LogicalSide.CLIENT).getTooltipRegistry().getItemXpGainTooltipData(id, xpType, stack)))
                .filter(pair -> !pair.getSecond().isEmpty())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return new XpSectionWidget(nbtXp, layout -> buildLayout(layout, nbtXp, ObjectType.ITEM, id));
    }

    public static XpSectionWidget create(Block block) {
        ResourceLocation id = RegistryUtil.getId(block);
        Map<EventType, Map<String, Long>> nbtXp = new HashMap<>();
        return new XpSectionWidget(nbtXp, layout -> buildLayout(layout, nbtXp, ObjectType.BLOCK, id));
    }

    public static XpSectionWidget create(Entity entity) {
        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        ResourceLocation id = RegistryUtil.getId(access, entity);
        var nbtXp = Arrays.stream(EventType.ENTITY_APPLICABLE_EVENTS)
                .map(xpType -> Pair.of(xpType, Core.get(LogicalSide.CLIENT).getTooltipRegistry().getEntityXpGainTooltipData(id, xpType, entity)))
                .filter(pair -> !pair.getSecond().isEmpty())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return new XpSectionWidget(nbtXp, layout -> buildLayout(layout, nbtXp, ObjectType.ENTITY, id));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = xpAwards.isEmpty()
                || !filter.matchesSelection(SELECTION.XP)
                || (!filter.getSkill().isEmpty() && !skills.contains(filter.getSkill()));
        this.setHeight(filtered ? 0 : (getChildren().size() * 12) + 2);
        return  filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
