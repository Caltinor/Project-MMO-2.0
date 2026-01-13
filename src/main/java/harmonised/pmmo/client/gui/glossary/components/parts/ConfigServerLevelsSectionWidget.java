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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigServerLevelsSectionWidget extends ReactiveWidget {
    private final Set<String> skills = new HashSet<>();
    public ConfigServerLevelsSectionWidget(ServerData.Levels data) {
        super(0,0,0,0);
        Font font = Minecraft.getInstance().font;
        addChild(new StringWidget(LangProvider.GLOSSARY_CONFIG_SERVER_LEVEL_HEADER.asComponent()
                        .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD), font).alignLeft(),
                PositionType.STATIC.constraint, textConstraint);
        addChild(new StringWidget(LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_HEADER.asComponent()
                        .withStyle(ChatFormatting.GREEN), font).alignLeft(),
                PositionType.STATIC.constraint, textConstraint);
        if (data.staticLevels().getFirst() != -1) {
            for (int i = 0; i < data.staticLevels().size(); i++) {
                addChild(new StringWidget(LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_STATIC_LEVEL.asComponent(i+1, data.staticLevels().get(i)), font).alignLeft(),
                        PositionConstraints.offset(10, 0), textConstraint);
            }
        }
        else {
            addChild(new StringWidget(LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_FORMULA
                            .asComponent(data.xpMin(), data.xpBase(), data.perLevel()), font).alignLeft(),
                    PositionConstraints.offset(10, 0), textConstraint);
        }
        addChild(new StringWidget(LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_MAX.asComponent(data.maxLevel()).withStyle(ChatFormatting.RED), font).alignLeft(),
                PositionConstraints.offset(10, 0), textConstraint);
        if (data.globalModifier() != 1.0 || !data.skillModifiers().containsKey("example_skill")) {
            addChild(new StringWidget(LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_MODIFIERS_HEADER.asComponent().withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE), font).alignLeft(),
                    PositionType.STATIC.constraint, textConstraint);
            if (data.globalModifier() != 1.0) {
                double value = (data.globalModifier() - 1) * 100;
                String modifier = (value > 0 ? "+" : "") + DP.dpSoft(value)+ "%";
                addChild(new StringWidget(LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_MODIFIERS_GLOBAL.asComponent(modifier), font).alignLeft(),
                        PositionConstraints.offset(10,0), textConstraint);
            }
            skills.addAll(data.skillModifiers().keySet());
            for (Map.Entry<String,Double> entry : data.skillModifiers().entrySet()) {
                if (entry.getKey().equals("example_skill")) continue;
                double value = (entry.getValue()-1) * 100;
                String modifier = (value > 0 ? "+" : "") + DP.dpSoft(value) + "%";
                addChild(new StringWidget(LangProvider.skill(entry.getKey()).append(": ").append(modifier), font).alignLeft(),
                        PositionConstraints.offset(10, 0), textConstraint);
            }
        }
        if (data.lossOnDeath() != 0.0) {
            MutableComponent excess = data.loseOnlyExcess()
                    ? LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_DEATH_EXCESS.asComponent()
                    : LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_DEATH_TOTAL.asComponent();
            String loss = DP.dpSoft(data.lossOnDeath() * 100);
            MutableComponent death = LangProvider.GLOSSARY_CONFIG_SERVER_LEVELS_DEATH.asComponent(loss).append(excess);
            addChild(new StringWidget(death.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE), font).alignLeft(), PositionType.STATIC.constraint, textConstraint);
        }
        setHeight(getChildren().size() * 12);
    }

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = !filter.matchesSkill(skills)
                || filter.getEnumGroup() != null
                || (!filter.matchesSelection(SELECTION.XP) && !filter.matchesSelection(SELECTION.BONUS));
        setHeight(filtered ? 0 : getChildren().size() * 12);
        return filtered;
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
