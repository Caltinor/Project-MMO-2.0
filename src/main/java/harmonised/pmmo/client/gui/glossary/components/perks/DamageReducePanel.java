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

public class DamageReducePanel extends PanelWidget {
    private final String name;
    private final String skill;

    public DamageReducePanel(int width, Player player, CompoundTag config) {
        super(0x88394045, width);
        MutableComponent title = LangProvider.PERK_DAMAGE_REDUCE.asComponent();
        MutableComponent descr = LangProvider.PERK_DAMAGE_REDUCE_DESC.asComponent();
        this.name = title.toString();
        this.skill = config.getString(APIUtils.SKILLNAME).orElse(null);
        long skillLevel = skill == null ? 0 : Core.get(LogicalSide.CLIENT).getData().getLevel(skill, null);
        addString(title.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), PositionType.STATIC.constraint, textConstraint);
        addString(descr.withStyle(ChatFormatting.GRAY), PositionType.STATIC.constraint, textConstraint);
        //When the following damage types are applied
        List<String> dmgTypes = config.getListOrEmpty(APIUtils.DAMAGE_TYPE_IN).stream().map(t -> t.asString().orElse("")).toList();
        MutableComponent dmgTypeHeader = LangProvider.PERK_FALL_SAVE_STATUS_2.asComponent();
        if (dmgTypes.isEmpty())
            dmgTypeHeader.append(LangProvider.PERK_DAMAGE_BOOST_STATUS_1a.asComponent());
        addString(dmgTypeHeader, PositionConstraints.offset(10, 0), textConstraint);
        for (String dmgType : dmgTypes) {
            addString(Component.literal(dmgType), PositionConstraints.offset(20, 0), textConstraint);
        }
        //display the actual boosted values
        int maxBoost = config.getIntOr(APIUtils.MAX_BOOST, 0);
        float perLevel = config.getFloatOr(APIUtils.PER_LEVEL, 0);
        float saved = (perLevel * (float)skillLevel) + config.getFloatOr(APIUtils.BASE, 0);
        saved = Math.min(maxBoost, saved);
        addString(LangProvider.PERK_FALL_SAVE_STATUS_1.asComponent(saved >= 0 ? "+" : "-", DP.dpSoft(saved), DP.dpSoft(perLevel)), PositionConstraints.offset(10, 0), textConstraint);
        addString(LangProvider.PERK_FALL_SAVE_STATUS_3.asComponent(maxBoost), PositionConstraints.offset(10, 0), textConstraint);

        PerkRenderer.commonElements(this, config);
        addChild(new DividerWidget(200, 2, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder()
                .absoluteHeight(2).build());
        this.setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
    }

    @Override
    public boolean applyFilter(Filter filter) {
        return !(filter.matchesSkill(skill))
                || !filter.matchesObject(OBJECT.PERKS)
                || (!filter.matchesTextFilter("pmmo:damage_reduce")
                    && !filter.matchesTextFilter(name));
    }
}
