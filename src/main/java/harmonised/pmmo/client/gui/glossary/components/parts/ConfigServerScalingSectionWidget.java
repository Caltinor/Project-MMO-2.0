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
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigServerScalingSectionWidget extends ReactiveWidget {
    private final Set<String> skills = new HashSet<>();
    public ConfigServerScalingSectionWidget(ServerData.MobScaling data) {
        super(0,0,0,0);
        if (data.enabled()) {
            skills.addAll(data.ratios().values().stream().map(Map::keySet).flatMap(Set::stream).toList());
            var registry = Minecraft.getInstance().player.registryAccess().registryOrThrow(Registries.ATTRIBUTE);

            addString(LangProvider.GLOSSARY_CONFIG_SERVER_SCALING_HEADER.asComponent().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE, ChatFormatting.LIGHT_PURPLE), PositionType.STATIC.constraint, textConstraint);
            addString(LangProvider.GLOSSARY_CONFIG_SERVER_SCALING_DESC.asComponent().withStyle(ChatFormatting.GRAY), PositionType.STATIC.constraint, textConstraint);
            addString(LangProvider.GLOSSARY_CONFIG_SERVER_SCALING_AOE.asComponent(data.aoe()), PositionConstraints.offset(10, 0), textConstraint);
            addString(LangProvider.GLOSSARY_CONFIG_SERVER_SCALING_BOSS.asComponent(DP.dpSoft((data.bossScaling()-1) * 100)), PositionConstraints.offset(10, 0), textConstraint);
            MutableComponent formula = data.useExponential()
                ? LangProvider.GLOSSARY_CONFIG_SERVER_SCALING_FORMULA_EXPO.asComponent(data.powerBase(), data.perLevel(), data.baseLevel())
                : LangProvider.GLOSSARY_CONFIG_SERVER_SCALING_FORMULA_LIN.asComponent(data.baseLevel(), data.perLevel());
            addString(formula, PositionConstraints.offset(10, 0), textConstraint);
            addString(LangProvider.GLOSSARY_CONFIG_SERVER_SCALING_RATIOS.asComponent(), PositionConstraints.offset(10, 0), textConstraint);
            data.ratios().forEach((attrID, map) -> {
                MutableComponent header = Component.translatable(registry.get(attrID).getDescriptionId());
                if (map.size() == 1) {
                    Map.Entry<String, Double> solo = map.entrySet().iterator().next();
                    addString(header.append(": ").append(LangProvider.skill(solo.getKey()).append(": ").append(solo.getValue().toString())), PositionConstraints.offset(20, 0), textConstraint);
                }
                if (map.size() > 1) {
                    map.forEach((skill, ratio) ->
                            addString(LangProvider.skill(skill).append(": ").append(ratio.toString()), PositionConstraints.offset(30, 0), textConstraint));
                }
            });
        }
        setHeight(getChildren().size() * 12);
    }

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = !filter.matchesSkill(skills)
                || filter.getEnumGroup() != null
                || !filter.matchesSelection(SELECTION.MOB_SCALING);
        setHeight(filtered ? 0 : getChildren().size() * 12);
        return filtered;
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
