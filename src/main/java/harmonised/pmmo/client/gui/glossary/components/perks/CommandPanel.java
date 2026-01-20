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
import harmonised.pmmo.core.perks.FeaturePerks;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

public class CommandPanel extends PanelWidget {
    private final String name;

    public CommandPanel(int width, Player player, CompoundTag config) {
        super(0x88394045, width);
        MutableComponent title = LangProvider.PERK_COMMAND.asComponent();
        MutableComponent descr = LangProvider.PERK_COMMAND_DESC.asComponent();
        this.name = title.toString();
        addString(title.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), PositionType.STATIC.constraint, textConstraint);
        addString(descr.withStyle(ChatFormatting.GRAY), PositionType.STATIC.constraint, textConstraint);
        boolean isFunction = config.contains(FeaturePerks.FUNCTION);
        addString(LangProvider.PERK_COMMAND_STATUS_1.asComponent(
                isFunction ? "Function" : "Command",
                config.getString(isFunction ? FeaturePerks.FUNCTION : FeaturePerks.COMMAND)
        ), PositionConstraints.offset(10, 0), textConstraint);

        PerkRenderer.commonElements(this, config);
        addChild(new DividerWidget(200, 2, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder()
                .absoluteHeight(2).build());
        this.setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    public boolean applyFilter(Filter filter) {
        return !filter.matchesObject(OBJECT.PERKS)
                || (!filter.matchesTextFilter("pmmo:command")
                    && !filter.matchesTextFilter(name));
    }
}
