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
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.LogicalSide;

import java.util.Optional;

public class AttributePanel extends PanelWidget {
    private final String name;
    private final String skill;
    private final boolean invalidAttribute;

    public AttributePanel(int width, Player player, CompoundTag config) {
        super(0x88394045, width);
        Identifier attribID = Reference.of(config.getStringOr(APIUtils.ATTRIBUTE, "missing"));
        Optional<Holder.Reference<Attribute>> attribute = player.registryAccess()
                .lookupOrThrow(Registries.ATTRIBUTE).get(ResourceKey.create(Registries.ATTRIBUTE, attribID));
        MutableComponent title = LangProvider.PERK_ATTRIBUTE.asComponent();
        this.name = title.toString();
        this.skill = config.getString(APIUtils.SKILLNAME).orElse(null);
        this.invalidAttribute = attribute.isEmpty();
        if (!invalidAttribute) {
            long skillLevel = skill == null ? 0 : Core.get(LogicalSide.CLIENT).getData().getLevel(skill, null);
            addString(title.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), PositionType.STATIC.constraint, textConstraint);
            MutableComponent descr = LangProvider.PERK_ATTRIBUTE_DESC.asComponent();
            addString(descr.withStyle(ChatFormatting.GRAY), PositionType.STATIC.constraint, textConstraint);

            double perLevel = config.getDoubleOr(APIUtils.PER_LEVEL, 0);
            double maxBoost = config.getDoubleOr(APIUtils.MAX_BOOST, 0);
            double boost = Math.min(perLevel * skillLevel, maxBoost) + config.getDoubleOr(APIUtils.BASE, 0);
            AttributeModifier.Operation operation = config.getBooleanOr(APIUtils.MULTIPLICATIVE, true) ? AttributeModifier.Operation.ADD_MULTIPLIED_BASE : AttributeModifier.Operation.ADD_VALUE;
            AttributeModifier mod = new AttributeModifier(attribID, boost, operation);
            MutableComponent attribMsg = attribute.get().value().toComponent(mod, TooltipFlag.NORMAL).withStyle(ChatFormatting.BOLD);
            //display of the actual modification
            addString(attribMsg, PositionConstraints.offset(10, 0), textConstraint);
            addString(LangProvider.PERK_ATTRIBUTE_STATUS_2.asComponent(perLevel), PositionConstraints.offset(10, 0), textConstraint);

            PerkRenderer.commonElements(this, config);

            addChild(new DividerWidget(200, 2, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder()
                    .absoluteHeight(2).build());
        }
        this.setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    public boolean applyFilter(Filter filter) {
        return invalidAttribute
                || !(filter.matchesSkill(skill))
                || !filter.matchesObject(OBJECT.PERKS)
                || (!filter.matchesTextFilter("pmmo:attribute")
                    && !filter.matchesTextFilter(name));
    }
}
