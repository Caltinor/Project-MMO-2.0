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

public class ConfigServerPartySectionWidget extends ReactiveWidget {
    private final Set<String> skills = new HashSet<>();
    public ConfigServerPartySectionWidget(ServerData.Party data) {
        super(0,0,0,0);
        Font font = Minecraft.getInstance().font;
        skills.addAll(data.bonus().keySet());

        addString(LangProvider.GLOSSARY_CONFIG_SERVER_PARTY_HEADER.asComponent().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE, ChatFormatting.YELLOW), PositionType.STATIC.constraint, textConstraint);
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_PARTY_RANGE.asComponent(data.range()), PositionConstraints.offset(10, 0), textConstraint);
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_PARTY_BONUS.asComponent(), PositionConstraints.offset(10, 0), textConstraint);
        data.bonus().forEach((skill, bonus) -> {
            addString(LangProvider.skill(skill).append(": ").append(DP.dpSoft((bonus-1) * 100)).append("%"), PositionConstraints.offset(20, 0), textConstraint);
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
