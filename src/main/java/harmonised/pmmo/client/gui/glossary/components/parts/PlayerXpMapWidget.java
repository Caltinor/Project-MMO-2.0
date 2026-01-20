package harmonised.pmmo.client.gui.glossary.components.parts;

import com.mojang.datafixers.util.Pair;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.storage.Experience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.LogicalSide;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerXpMapWidget extends ReactiveWidget {
    public PlayerXpMapWidget(Map<String, Experience> map) {this(map, 12, new HashMap<>());}
    public PlayerXpMapWidget(Map<String, Experience> map, int iconSize, Map<String, Double> modifiers) {
        super(0,0,0,0);
        int currentRow = 0;
        List<Pair<String, Experience>> orderedSkills = map.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Experience>>comparingLong(a -> a.getValue().getLevel().getLevel()).reversed())
                .map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList();
        Font font = Minecraft.getInstance().font;
        for (Pair<String, Experience> skill : orderedSkills) {
            SkillData data = Config.skills().get(skill.getFirst());
            //First Column skill levels
            String rawXp = rawXpLine(skill.getSecond(), data);
            int width = font.width(rawXp) + 4;
            addString(Component.literal(rawXp).withColor(data.getColor()),
                    PositionConstraints.grid(currentRow, 0),
                    SizeConstraints.builder().absoluteHeight(12).maxWidthAbsolute(width).build());
            //Second column icon or name as configured
            if (Config.SKILL_LIST_USE_ICONS.get())
                addChild(new SkillIconWidget(data.getIcon(), data.getIconSize()),
                        PositionConstraints.grid(currentRow, 1),
                        SizeConstraints.builder().absoulteWidth(iconSize).absoluteHeight(iconSize).build());
            else {
                Component skillComp = LangProvider.skill(skill.getFirst());
                int strWidth = font.width(skillComp);
                addString(skillComp,
                        PositionConstraints.grid(currentRow, 1),
                        SizeConstraints.builder().absoluteHeight(12).maxWidthAbsolute(strWidth).build());
            }
            //Third Column bonuses
            if (modifiers.containsKey(skill.getFirst()))
                addString(Component.literal(bonusLine(modifiers.get(skill.getFirst()))).withColor(data.getColor()),
                        PositionConstraints.grid(currentRow, 2), textConstraint);
            currentRow++;
        }
        setHeight(currentRow * 12);
    }

    private static String rawXpLine(Experience xpValue, SkillData data) {
        double level = ((DataMirror) Core.get(LogicalSide.CLIENT).getData()).getXpWithPercentToNextLevel(xpValue);
        if (level > data.getMaxLevel())
            return "" + data.getMaxLevel();
        if (level > Config.server().levels().maxLevel())
            return "" + Config.server().levels().maxLevel();
        else
            return DP.dpCustom(Math.floor(level * 100D) / 100D, 2);
    }

    private static String bonusLine(double bonus) {
        if (bonus != 1d) {
            bonus = (Math.max(0, bonus) -1) * 100d;
            return (bonus >= 0 ? "+" : "-")+DP.dp(bonus)+"%";
        }
        else return "";
    }

    @Override
    public boolean applyFilter(Filter filter) {
        return !filter.getTextFilter().isEmpty()
                || !filter.matchesSelection(SELECTION.XP)
                || !filter.matchesObject(OBJECT.ENTITY);
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.GRID;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
