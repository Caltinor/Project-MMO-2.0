package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.codecs.ServerData;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigServerXpSectionWidget extends ReactiveWidget {
    private final Set<String> skills = new HashSet<>();

    public ConfigServerXpSectionWidget(ServerData.XpGains data) {
        super(0,0,0,0);
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_XP_HEADER.asComponent().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE, ChatFormatting.GREEN),
                PositionType.STATIC.constraint, textConstraint);
        String reuse = DP.dpSoft(data.reusePenalty() * 100);
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_XP_REUSE.asComponent(reuse), PositionType.STATIC.constraint, textConstraint);
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_XP_PLAYER.asComponent().withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA),
                PositionType.STATIC.constraint, textConstraint);
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_XP_PLAYER_DESC.asComponent().withStyle(ChatFormatting.GRAY),
                PositionType.STATIC.constraint, textConstraint);
        data.playerEvents().forEach((event, map) -> {
            MutableComponent header = event.tooltipTranslation.asComponent();
            if (map.size() == 1) {
                Map.Entry<String, Double> solo = map.entrySet().iterator().next();
                header.append(":  ").append(LangProvider.skill(solo.getKey()).append(": ").append(DP.dpSoft(solo.getValue())));
            }
            addString(header, PositionConstraints.offset(10, 0), textConstraint);
            if (map.size() > 1) {
                map.forEach((skill, xp) -> {
                    addString(LangProvider.skill(skill).append(": ").append(DP.dpSoft(xp)), PositionConstraints.offset(20, 0), textConstraint);
                });
            }
        });
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_XP_DMG_HEADER.asComponent().withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                PositionType.STATIC.constraint, textConstraint);
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_XP_DMG_DESC.asComponent().withStyle(ChatFormatting.GRAY),
                PositionType.STATIC.constraint, textConstraint);
        data.damageXp().forEach((event, dmgMap) -> {
            addString(event.tooltipTranslation.asComponent().withStyle(ChatFormatting.BOLD), PositionConstraints.offset(10, 0), textConstraint);
            dmgMap.forEach((dmgType, map) -> {
                MutableComponent header = Component.literal(dmgType);
                if (map.size() == 1) {
                    Map.Entry<String, Long> solo = map.entrySet().iterator().next();
                    header.append(":  ").append(LangProvider.skill(solo.getKey()).append(": ").append(DP.dpSoft(solo.getValue())));
                }
                addString(header, PositionConstraints.offset(20, 0), textConstraint);
                if (map.size() > 1) {
                    map.forEach((skill, xp) -> {
                        addString(LangProvider.skill(skill).append(": ").append(DP.dpSoft(xp)), PositionConstraints.offset(30, 0), textConstraint);
                    });
                }
            });
        });
        setHeight(getChildren().size() * 12);
    }

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = !filter.matchesSkill(skills) || !filter.matchesSelection(SELECTION.XP);
        setHeight(filtered ? 0 : getChildren().size() * 12);
        return filtered;
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
