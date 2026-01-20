package harmonised.pmmo.client.gui.glossary.components.parts;

import com.mojang.datafixers.util.Pair;
import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.GuiEnumGroup;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.NBTUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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

public class ReqSectionWidget extends ReactiveWidget {
    Map<ReqType, Map<String, Long>> reqs = new HashMap<>();
    List<String> skills = new ArrayList<>();
    List<GuiEnumGroup> types = new ArrayList<>();
    private ReqSectionWidget(Map<ReqType, Map<String, Long>> nbtReqs, Function<ReqSectionWidget,Map<ReqType, Map<String, Long>>> layoutBuilder) {
        super(0, 0, 0, 0);
        //store them in the widget for use in the filter
        reqs.putAll(nbtReqs);
        reqs.putAll(layoutBuilder.apply(this));
        types.addAll(reqs.keySet());
        skills = reqs.entrySet().stream().map(entry -> entry.getValue().keySet()).flatMap(Set::stream).toList();
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}
    @Override
    public void resize() {
        getChildren().stream()
                .filter(poser -> poser.get() instanceof NBTSettingWidget)
                .forEach(poser -> ((NBTSettingWidget) poser.get()).visible = this.visible);
        super.resize();
    }

    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();
    private static Map<ReqType, Map<String, Long>> buildLayout(ReqSectionWidget layout, Map<ReqType, List<LogicEntry>> nbtSettings, Map<ReqType, Map<String, Long>> nbtReqs, ObjectType type, ResourceLocation id) {
        Font font = Minecraft.getInstance().font;
        Core core = Core.get(LogicalSide.CLIENT);
        Map<ReqType, Map<String, Long>> regReqs = new HashMap<>();
        List<Positioner<?>> contentWidgets = new ArrayList<>();

        Map<ResourceLocation, Integer> negativeEffects = core.getLoader().getLoader(type).getData(id).getNegativeEffect();
        if (!negativeEffects.isEmpty()) {
            contentWidgets.add(new Positioner.Widget(new StringWidget(LangProvider.BIOME_EFFECT_NEG.asComponent().withStyle(ChatFormatting.UNDERLINE), font).alignLeft(), PositionType.STATIC.constraint, textConstraint));
            negativeEffects.forEach((key, value) -> {
                MutableComponent effect = Component.literal("   ").append(Minecraft.getInstance().player.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).get(ResourceKey.create(Registries.MOB_EFFECT, key)).get().value().getDisplayName());
                contentWidgets.add(new Positioner.Widget(new StringWidget(effect.append(" ").append(String.valueOf(value + 1)), font).alignLeft(), PositionType.STATIC.constraint, textConstraint));
            });
        }

        for (ReqType req : ReqType.values()) {
            Map<String, Long> skillmap;
            if (nbtSettings.containsKey(req)) {
                contentWidgets.add(new Positioner.Widget(new NBTSettingWidget(
                        nbtSettings.get(req),
                        nbtReqs.getOrDefault(req, new HashMap<>()),
                        req.tooltipTranslation.asComponent()),
                    PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build()));
            }
            else if (!(skillmap = core.getCommonReqData(new HashMap<>(), type, id, req, new CompoundTag())).isEmpty()){
                regReqs.put(req, skillmap);
                MutableComponent combine = req.tooltipTranslation.asComponent();
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
        if (!contentWidgets.isEmpty()) {
            layout.addChild(new StringWidget(LangProvider.REQ_HEADER.asComponent().withStyle(ChatFormatting.RED), font).alignLeft(), PositionType.STATIC.constraint, textConstraint);
            contentWidgets.forEach(layout::addChild);
            layout.addChild(new DividerWidget(100, 1, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        }
        return regReqs;
    }

    private static List<Positioner<?>> setSkills(Map<String, Long> map, AbstractWidget header, Font font) {
        List<Positioner<?>> skillWidgets = new ArrayList<>(List.of(new Positioner.Widget(header, PositionType.STATIC.constraint, textConstraint)));
        MutableComponent prefix = Component.literal(map.values().stream().filter(s -> s > 0).count() > 1 ? "   " : "");
        map.forEach((skill, value) -> {
            if (value > 0)
                skillWidgets.add(new Positioner.Widget(new StringWidget(prefix.copy().append(LangProvider.skill(skill)).append(": ").append(value.toString()), font).alignLeft(),
                        PositionType.STATIC.constraint, textConstraint));
        });
        return skillWidgets.size() > 1 ? skillWidgets : new ArrayList<>();
    }

    public static ReqSectionWidget create(ItemStack stack) {
        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        ResourceLocation id = RegistryUtil.getId(access, stack);
        ObjectData setting = Core.get(LogicalSide.CLIENT).getLoader().ITEM_LOADER.getData().getOrDefault(id, ObjectData.build().end());
        var reqSettings = setting.nbtReqs();
        var nbtReqs = Arrays.stream(ReqType.ITEM_APPLICABLE_EVENTS)
                .filter(req -> setting.nbtReqs().containsKey(req))
                .map(req -> Pair.of(req, setting.getReqs(req, TagUtils.stackTag(stack, access))))
                .filter(pair -> !pair.getSecond().isEmpty())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return new ReqSectionWidget(nbtReqs, layout -> buildLayout(layout, reqSettings, nbtReqs, ObjectType.ITEM, id));
    }

    public static ReqSectionWidget create(Block block, BlockEntity be) {
        ResourceLocation id = RegistryUtil.getId(block);
        ObjectData setting = Core.get(LogicalSide.CLIENT).getLoader().BLOCK_LOADER.getData().getOrDefault(id, ObjectData.build().end());
        var reqSettings = setting.nbtReqs();
        Map<ReqType, Map<String, Long>> nbtReqs = be != null ?
                Arrays.stream(ReqType.BLOCK_APPLICABLE_EVENTS)
                .filter(req -> setting.nbtReqs().containsKey(req))
                .map(req -> Pair.of(req, setting.getReqs(req, TagUtils.tileTag(be))))
                .filter(pair -> !pair.getSecond().isEmpty())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                : new HashMap<>();
        return new ReqSectionWidget(nbtReqs, layout -> buildLayout(layout, reqSettings, nbtReqs, ObjectType.BLOCK, id));
    }

    public static ReqSectionWidget create(Entity entity) {
        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        ResourceLocation id = RegistryUtil.getId(access, entity);
        ObjectData setting = Core.get(LogicalSide.CLIENT).getLoader().ENTITY_LOADER.getData().getOrDefault(id, ObjectData.build().end());
        var reqSettings = setting.nbtReqs();
        var nbtReqs = Arrays.stream(ReqType.ENTITY_APPLICABLE_EVENTS)
                .filter(req -> setting.nbtReqs().containsKey(req))
                .map(req -> Pair.of(req, setting.getReqs(req, TagUtils.entityTag(entity))))
                .filter(pair -> !pair.getSecond().isEmpty())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return new ReqSectionWidget(nbtReqs, layout -> buildLayout(layout, reqSettings, nbtReqs, ObjectType.ENTITY, id));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        return reqs.isEmpty()
                || !filter.matchesSelection(SELECTION.REQS)
                || !filter.matchesEnum(types)
                || (!filter.getSkill().isEmpty() && !skills.contains(filter.getSkill()));
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
