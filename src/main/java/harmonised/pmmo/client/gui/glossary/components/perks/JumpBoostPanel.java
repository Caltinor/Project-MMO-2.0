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
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

public class JumpBoostPanel extends PanelWidget {
    private final String name;
    private final String skill;

    public JumpBoostPanel(int width, Player player, CompoundTag config) {
        super(0x88394045, width);
        MutableComponent title = LangProvider.PERK_JUMP_BOOST.asComponent();
        MutableComponent descr = LangProvider.PERK_JUMP_BOOST_DESC.asComponent();
        this.name = title.toString();
        this.skill = config.getString(APIUtils.SKILLNAME).orElse(null);
        long skillLevel = skill == null ? 0 : Core.get(LogicalSide.CLIENT).getData().getLevel(skill, null);
        addString(title.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), PositionType.STATIC.constraint, textConstraint);
        addString(descr.withStyle(ChatFormatting.GRAY), PositionType.STATIC.constraint, textConstraint);
        double boost = config.getDoubleOr(APIUtils.PER_LEVEL, 0) * (double)skillLevel;
        addString(LangProvider.PERK_JUMP_BOOST_STATUS_1.asComponent(boost), PositionConstraints.offset(10, 0), textConstraint);
        addString(LangProvider.PERK_JUMP_BOOST_STATUS_2.asComponent(config.getDoubleOr(APIUtils.MAX_BOOST, 0d)), PositionConstraints.offset(10, 0), textConstraint);

        PerkRenderer.commonElements(this, config);
        addChild(new DividerWidget(200, 2, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder()
                .absoluteHeight(2).build());
        this.setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    public boolean applyFilter(Filter filter) {
        return !(filter.matchesSkill(skill))
                || !filter.matchesObject(OBJECT.PERKS)
                || (!filter.matchesTextFilter("pmmo:jump_boost")
                    && !filter.matchesTextFilter(name));
    }
}
