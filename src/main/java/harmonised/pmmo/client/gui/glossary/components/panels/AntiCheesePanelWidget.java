package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.client.gui.glossary.components.parts.AntiCheeseSettingWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.neoforged.fml.LogicalSide;

import java.util.Map;

public class AntiCheesePanelWidget extends ObjectPanelWidget {
    public enum Type {
        AFK(LangProvider.GLOSSARY_CONFIG_ANTI_HEADER_AFK),
        DIM(LangProvider.GLOSSARY_CONFIG_ANTI_HEADER_DIM),
        NORM(LangProvider.GLOSSARY_CONFIG_ANTI_HEADER_NORM);

        final LangProvider.Translation header;
        Type(LangProvider.Translation header) {this.header = header;}
    }

    public static AntiCheesePanelWidget AFK(int color, int width, Map<EventType, CheeseTracker.Setting> settings, boolean afkSubtract) {
        return new AntiCheesePanelWidget(color, width, settings, Type.AFK, afkSubtract);
    }
    public static AntiCheesePanelWidget DIM(int color, int width, Map<EventType, CheeseTracker.Setting> settings) {
        return new AntiCheesePanelWidget(color, width, settings, Type.DIM, false);
    }
    public static AntiCheesePanelWidget NORM(int color, int width, Map<EventType, CheeseTracker.Setting> settings) {
        return new AntiCheesePanelWidget(color, width, settings, Type.NORM, false);
    }
    public AntiCheesePanelWidget(int color, int width, Map<EventType, CheeseTracker.Setting> settings, Type type, boolean afkSubtract) {
        super(color, width, Core.get(LogicalSide.CLIENT));
        this.setPadding(2, 0, 0, 0);
        if (!settings.isEmpty()) {
            addString(type.header.asComponent()
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(ChatFormatting.BOLD)
                            .withStyle(ChatFormatting.BLUE),
                    PositionType.STATIC.constraint, textConstraint);
            if (type == Type.AFK && afkSubtract)
                addString(LangProvider.GLOSSARY_CONFIG_ANTI_AFKSUB.asComponent()
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
                        PositionType.STATIC.constraint, textConstraint);
            settings.forEach((event, config) -> addChild((AbstractWidget)
                    new AntiCheeseSettingWidget(event, config, type),
                    PositionConstraints.offset(10, 0),
                    SizeConstraints.builder().internalHeight().build()));
            addChild(new DividerWidget(427, 2, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder()
                    .absoluteHeight(2).build());
            this.setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
        }
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    public boolean applyFilter(Filter filter) {
        return !filter.matchesObject(OBJECT.NONE) || !filter.matchesSelection(SELECTION.XP);
    }
}
