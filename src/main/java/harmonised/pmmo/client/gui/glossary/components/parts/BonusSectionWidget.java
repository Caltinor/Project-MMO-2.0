package harmonised.pmmo.client.gui.glossary.components.parts;

import com.mojang.datafixers.util.Pair;
import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.GuiEnumGroup;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
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

public class BonusSectionWidget extends ReactiveWidget {
    Map<ModifierDataType, Map<String, Double>> bonuses = new HashMap<>();
    final List<String> skills;
    final List<GuiEnumGroup> types;
    private BonusSectionWidget(Map<ModifierDataType, Map<String, Double>> nbtBonuses, Function<ResponsiveLayout,Map<ModifierDataType, Map<String, Double>>> layoutBuilder) {
        super(0, 0, 0, 0);
        //store them in the widget for use in the filter
        bonuses.putAll(nbtBonuses);
        bonuses.putAll(layoutBuilder.apply(this));
        skills = bonuses.values().stream().map(Map::keySet).flatMap(Set::stream).toList();
        types = new ArrayList<>(bonuses.keySet());
        setHeight((getChildren().size() * 12) + 2);
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();
    private static Map<ModifierDataType, Map<String, Double>> buildLayout(ResponsiveLayout layout, Map<ModifierDataType, Map<String, Double>> nbtBonuses, ObjectType type, ResourceLocation id) {
        Font font = Minecraft.getInstance().font;
        Core core = Core.get(LogicalSide.CLIENT);
        Map<ModifierDataType, Map<String, Double>> regBonuses = new HashMap<>();
        List<Positioner<?>> contentWidgets = new ArrayList<>();
        for (ModifierDataType bonus : ModifierDataType.values()) {
            Map<String, Double> skillmap;
            if (!nbtBonuses.getOrDefault(bonus, new HashMap<>()).isEmpty()) {
                //TODO replace with a widget that when hovered gives the NBT logic via tooltip
                contentWidgets.addAll(setSkills(nbtBonuses.get(bonus), new StringWidget(bonus.tooltip.asComponent().append(" [NBT]"), font).alignLeft(), font));
            }
            else if (!(skillmap = core.getObjectModifierMap(type, id, bonus, new CompoundTag())).isEmpty()){
                regBonuses.put(bonus, skillmap);
                MutableComponent combine = bonus.tooltip.asComponent();
                if (skillmap.size() == 1) {
                    String skill = skillmap.keySet().iterator().next();
                    if (skillmap.get(skill) > 0)
                        combine.append(" ").append(LangProvider.skill(skill)).append(":").append(" "+DP.dpSoft((skillmap.get(skill)*100)-100)+"%");
                    contentWidgets.add(new Positioner.Widget(new StringWidget(combine, font).alignLeft(), PositionType.STATIC.constraint, textConstraint));
                }
                else {
                    contentWidgets.addAll(setSkills(skillmap, new StringWidget(combine, font).alignLeft(), font));
                }
            }
        }
        if (!contentWidgets.isEmpty()) {
            layout.addChild(new StringWidget(LangProvider.GLOSSARY_SECTION_BONUS.asComponent().withStyle(ChatFormatting.BLUE), font).alignLeft(), PositionType.STATIC.constraint, textConstraint);
            contentWidgets.forEach(layout::addChild);
            layout.addChild(new DividerWidget(100, 1, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        }
        return regBonuses;
    }

    private static List<Positioner<?>> setSkills(Map<String, Double> map, AbstractWidget header, Font font) {
        List<Positioner<?>> skillWidgets = new ArrayList<>(List.of(new Positioner.Widget(header, PositionType.STATIC.constraint, textConstraint)));
        MutableComponent prefix = Component.literal(map.values().stream().filter(s -> s > 0).count() > 1 ? "   " : "");
        map.forEach((skill, value) -> {
            if (value != 0)
                skillWidgets.add(new Positioner.Widget(new StringWidget(prefix.copy().append(LangProvider.skill(skill)).append(":").append(" "+DP.dpSoft((value*100)-100)+"%"), font).alignLeft(),
                        PositionType.STATIC.constraint, textConstraint));
        });
        return skillWidgets.size() > 1 ? skillWidgets : new ArrayList<>();
    }

    public static BonusSectionWidget create(ItemStack stack) {
        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        ResourceLocation id = RegistryUtil.getId(access, stack);
        var nbtBonuses = Arrays.stream(ModifierDataType.values())
                .map(type -> Pair.of(type, Core.get(LogicalSide.CLIENT).getTooltipRegistry().getBonusTooltipData(id, type, stack)))
                .filter(pair -> !pair.getSecond().isEmpty())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return new BonusSectionWidget(nbtBonuses, layout -> buildLayout(layout, nbtBonuses, ObjectType.ITEM, id));
    }

    public static BonusSectionWidget create(Holder<Biome> biome) {
        ResourceLocation id = biome.unwrapKey().orElse(ResourceKey.create(Registries.BIOME, Reference.mc("missing"))).location();
        return new BonusSectionWidget(new HashMap<>(), layout -> buildLayout(layout, new HashMap<>(), ObjectType.BIOME, id));
    }

    public static BonusSectionWidget create(ResourceLocation id) {
        return new BonusSectionWidget(new HashMap<>(), layout -> buildLayout(layout, new HashMap<>(), ObjectType.DIMENSION, id));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = bonuses.isEmpty()
                || !filter.matchesSelection(SELECTION.BONUS)
                || (!filter.getSkill().isEmpty() && !skills.contains(filter.getSkill()))
                || (!filter.matchesEnum(types));
        this.setHeight(filtered ? 0 : (getChildren().size() * 12) + 2);
        return filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
