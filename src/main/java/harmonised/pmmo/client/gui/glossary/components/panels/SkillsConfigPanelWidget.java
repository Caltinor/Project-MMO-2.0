package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.SkillIconWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.LogicalSide;

import java.util.Map;

public class SkillsConfigPanelWidget extends ObjectPanelWidget{
    private final String skill;
    private final int internalHeight;
    public SkillsConfigPanelWidget(int width, String skill, SkillData data) {
        super(0x88394045, width, Core.get(LogicalSide.CLIENT));
        setPadding(4, 0, 0, 0);
        this.skill = skill;
        addChild(new SkillIconWidget(data.getIcon(), data.getIconSize()), PositionConstraints.grid(0, 0), SizeConstraints.builder().absoluteHeight(18).absoulteWidth(18).build());
        addString(LangProvider.skill(skill), PositionConstraints.grid(0, 1), textConstraint);
        addString(LangProvider.GLOSSARY_CONFIG_SKILLS_MAX.asComponent(data.maxLevel().orElse(Config.server().levels().maxLevel())), PositionConstraints.grid(1, 1), textConstraint);
        int skillGroupEntryHeight = 0;
        if (data.isSkillGroup()) {
            double denominator = data.getGroup().values().stream().reduce(Double::sum).orElse(1d);
            addString(LangProvider.GLOSSARY_CONFIG_SKILLS_GROUP.asComponent(), PositionConstraints.grid(2, 1), textConstraint);
            skillGroupEntryHeight += 12;
            for (Map.Entry<String, Double> entry : data.getGroup().entrySet()) {
                addString(Component.literal("   ").append(LangProvider.skill(entry.getKey())).append(": ").append(DP.dpSoft((entry.getValue() / denominator) * 100)).append("%"),
                        PositionConstraints.grid(skillGroupEntryHeight / 12 + 2, 1),
                        textConstraint);
                skillGroupEntryHeight += 12;
            };
        }
        if (data.getAfkExempt()) {
            addString(LangProvider.GLOSSARY_CONFIG_SKILLS_AFK.asComponent().withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_RED),
                    PositionConstraints.grid(skillGroupEntryHeight / 12 + 2, 1), textConstraint);
            skillGroupEntryHeight += 12;
        }
        addChild(new DividerWidget(this.width, 2, 0xFFFFFFFF), PositionConstraints.grid(skillGroupEntryHeight/12 + 3,1), SizeConstraints.builder().absoluteHeight(2).build());
        internalHeight = skillGroupEntryHeight + 32;
        setHeight(internalHeight);
    }

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = !filter.matchesSkill(skill) || !filter.matchesObject(OBJECT.NONE);
        setHeight(filtered ? 0 : internalHeight);
        return filtered;
    }
}
