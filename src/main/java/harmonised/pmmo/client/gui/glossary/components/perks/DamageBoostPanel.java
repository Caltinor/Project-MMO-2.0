package harmonised.pmmo.client.gui.glossary.components.perks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.perks.PerkRenderer;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ItemStackWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.perks.FeaturePerks;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DamageBoostPanel extends PanelWidget {
    private final String name;
    private final String skill;

    public DamageBoostPanel(int width, Player player, CompoundTag config) {
        super(0x88394045, width);
        MutableComponent title = LangProvider.PERK_DAMAGE_BOOST.asComponent();
        MutableComponent descr = LangProvider.PERK_DAMAGE_BOOST_DESC.asComponent();
        this.name = title.toString();
        this.skill = config.contains(APIUtils.SKILLNAME) ? config.getString(APIUtils.SKILLNAME) : null;
        long skillLevel = skill == null ? 0 : Core.get(LogicalSide.CLIENT).getData().getLevel(skill, null);
        addString(title.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), PositionType.STATIC.constraint, textConstraint);
        addString(descr.withStyle(ChatFormatting.GRAY), PositionType.STATIC.constraint, textConstraint);
        //When the following damage types are applied
        List<String> dmgTypes = config.getList(APIUtils.DAMAGE_TYPE_IN, Tag.TAG_STRING).stream().map(Tag::getAsString).toList();
        MutableComponent dmgTypeHeader = LangProvider.PERK_DAMAGE_BOOST_STATUS_6.asComponent();
        if (dmgTypes.isEmpty())
            dmgTypeHeader.append(LangProvider.PERK_DAMAGE_BOOST_STATUS_1a.asComponent());
        addString(dmgTypeHeader, PositionConstraints.offset(10, 0), textConstraint);
        for (String dmgType : dmgTypes) {
            addString(Component.literal(dmgType), PositionConstraints.offset(20, 0), textConstraint);
        }
        //When using any of the following items
        List<String> applicalbeTo = config.getList(FeaturePerks.APPLICABLE_TO, CompoundTag.TAG_STRING).stream().map(Tag::getAsString).toList();
        MutableComponent applicableHeader = LangProvider.PERK_DAMAGE_BOOST_STATUS_1.asComponent();
        if (applicalbeTo.isEmpty())
            applicableHeader.append(LangProvider.PERK_DAMAGE_BOOST_STATUS_1a.asComponent());
        addString(applicableHeader, PositionConstraints.offset(10, 0), textConstraint);
        var itemReg = Minecraft.getInstance().player.registryAccess().lookupOrThrow(Registries.ITEM);
        AtomicInteger inlineYOffset = new AtomicInteger();
        for (String value : applicalbeTo) {
            if (value.startsWith("#"))
                addString(LangProvider.PERK_DAMAGE_BOOST_STATUS_3.asComponent(value), PositionConstraints.offset(20, 0), textConstraint);
            else if (value.contains(":*"))
                addString(LangProvider.PERK_DAMAGE_BOOST_STATUS_4.asComponent(value.substring(0, value.length()-2)), PositionConstraints.offset(20, 0), textConstraint);
            else {
            itemReg.get(ResourceKey.create(Registries.ITEM, Reference.of(value))).ifPresentOrElse(itemRef -> {
                    addChild(new ItemStackWidget(itemRef.value()), PositionConstraints.offset(20, 0), SizeConstraints.builder().absoulteWidth(18).absoluteHeight(18).build());
                    addString(itemRef.value().getDefaultInstance().getDisplayName(), PositionType.STATIC.constraint, textConstraint);
                    inlineYOffset.getAndAdd(12);
                },
                () -> addString(Component.literal(value), PositionConstraints.offset(20, 0), textConstraint));
            }
        }
        //display the actual boosted values
        int maxBoost = config.getInt(APIUtils.MAX_BOOST);
        double perLevel = config.getDouble(APIUtils.PER_LEVEL);
        float damageModification = (float)(config.getDouble(APIUtils.BASE) + perLevel * (double)skillLevel);
        damageModification = Math.min(maxBoost, damageModification);
        addString(LangProvider.PERK_DAMAGE_BOOST_STATUS_2.asComponent(damageModification >= 0 ? "+" : "-", DP.dpSoft(damageModification), DP.dpSoft(perLevel)), PositionConstraints.offset(10, 0), textConstraint);
        addString(LangProvider.PERK_DAMAGE_BOOST_STATUS_5.asComponent(maxBoost), PositionConstraints.offset(10, 0), textConstraint);

        PerkRenderer.commonElements(this, config);
        addChild(new DividerWidget(200, 2, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder()
                .absoluteHeight(2).build());
        this.setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0) - inlineYOffset.get());
    }

    @Override
    public boolean applyFilter(Filter filter) {
        return !(filter.matchesSkill(skill))
                || !filter.matchesObject(OBJECT.PERKS)
                || (!filter.matchesTextFilter("pmmo:damage_boost")
                    && !filter.matchesTextFilter(name));
    }
}
