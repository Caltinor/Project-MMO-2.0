package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.codecs.ServerData;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class ConfigServerVeinSectionWidget extends ReactiveWidget {
    private VeinBlacklistSectionWidget blacklist;
    public ConfigServerVeinSectionWidget(ServerData.VeinMiner data) {
        super(0,0,0,0);
        if (data.enabled()) {
            addString(LangProvider.GLOSSARY_CONFIG_SERVER_VEIN_HEADER.asComponent()
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE, ChatFormatting.UNDERLINE)
                    , PositionType.STATIC.constraint, textConstraint);
            if (!data.requireSettings()) {
                addString(LangProvider.GLOSSARY_CONFIG_SERVER_VEIN_CONSUME.asComponent(data.defaultConsume()), PositionConstraints.offset(10, 0), textConstraint);
                if (data.chargeModifier() != 1.0)
                    addString(LangProvider.GLOSSARY_CONFIG_SERVER_VEIN_MODIFIER.asComponent(DP.dpSoft((data.chargeModifier()-1) * 100)), PositionConstraints.offset(10, 0), textConstraint);
            }
            this.blacklist = new VeinBlacklistSectionWidget(data.blacklist());
            addChild((AbstractWidget) blacklist, PositionConstraints.offset(10, 0), SizeConstraints.builder().internalHeight().build());}
        setHeight(visibleChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
    }

    @Override
    public boolean applyFilter(Filter filter) {
        blacklist.visible = !blacklist.applyFilter(filter);
        boolean filtered = !filter.matchesSelection(SELECTION.VEIN) || !filter.getSkill().isEmpty();
        setHeight(filtered ? 0 : visibleChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
        return filtered;
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
