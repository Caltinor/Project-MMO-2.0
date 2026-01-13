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
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

import java.util.List;

public class EffectsPanel extends PanelWidget {
    private final String name;
    private final String skill;

    public EffectsPanel(int width, Player player, CompoundTag config) {
        super(0x88394045, width);
        MutableComponent title = LangProvider.PERK_EFFECT.asComponent();
        MutableComponent descr = LangProvider.PERK_EFFECT_DESC.asComponent();
        this.name = title.toString();
        this.skill = config.contains(APIUtils.SKILLNAME) ? config.getString(APIUtils.SKILLNAME) : null;
        long skillLevel = skill == null ? 0 : Core.get(LogicalSide.CLIENT).getData().getLevel(skill, null);
        addString(title.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), PositionType.STATIC.constraint, textConstraint);
        addString(descr.withStyle(ChatFormatting.GRAY), PositionType.STATIC.constraint, textConstraint);

        var effectID = ResourceKey.create(Registries.MOB_EFFECT ,Reference.of(config.getString(APIUtils.EFFECT)));
        Minecraft.getInstance().player.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).get(effectID).ifPresent(effect -> {
            addString(LangProvider.PERK_EFFECT_STATUS_1.asComponent(effect.value().getDisplayName()).withStyle(ChatFormatting.BOLD), PositionConstraints.offset(10, 0), textConstraint);
            int modifier = config.getInt(APIUtils.MODIFIER) + 1;
            double per_level = config.getDouble(APIUtils.DURATION) * config.getDouble(APIUtils.PER_LEVEL);
            double duration = (per_level * skillLevel)/20;
            addString(LangProvider.PERK_EFFECT_STATUS_2.asComponent(modifier, duration), PositionConstraints.offset(10, 0), textConstraint);
            addString(LangProvider.PERK_EFFECT_STATUS_3.asComponent(DP.dpSoft(per_level/20)), PositionConstraints.offset(10, 0), textConstraint);
        });

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
                || (!filter.matchesTextFilter("pmmo:effect")
                    && !filter.matchesTextFilter(name));
    }
}
