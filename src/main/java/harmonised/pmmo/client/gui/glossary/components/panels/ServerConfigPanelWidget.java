package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.parts.ConfigServerLevelsSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ConfigServerPartySectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ConfigServerScalingSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ConfigServerVeinSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ConfigServerXpSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.ServerData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.LogicalSide;

public class ServerConfigPanelWidget extends ObjectPanelWidget{
    private final ConfigServerLevelsSectionWidget levels;
    private final ConfigServerXpSectionWidget xpGains;
    private final ConfigServerPartySectionWidget party;
    private final ConfigServerScalingSectionWidget scaling;
    private final ConfigServerVeinSectionWidget vein;

    public ServerConfigPanelWidget(int width) {
        super(0x88394045, width, Core.get(LogicalSide.CLIENT));
        setPadding(4, 0, 0, 0);
        Font font = Minecraft.getInstance().font;
        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        ServerData data = Config.server();
        addString(LangProvider.GLOSSARY_CONFIG_SERVER_HEADER.asComponent().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                PositionType.STATIC.constraint, textConstraint);
        general(this, data.general(), access, font);
        addChild(new DividerWidget(this.width, 2, 0xFFFFFFFF), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        levels = new ConfigServerLevelsSectionWidget(data.levels());
        addChild((AbstractWidget) levels, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
        addChild(new DividerWidget(this.width, 2, 0xFFFFFFFF), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        xpGains = new ConfigServerXpSectionWidget(data.xpGains());
        addChild((AbstractWidget) xpGains, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
        party = new ConfigServerPartySectionWidget(data.party());
        addChild((AbstractWidget) party, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
        scaling = new ConfigServerScalingSectionWidget(data.mobScaling());
        addChild((AbstractWidget) scaling, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
        vein = new ConfigServerVeinSectionWidget(data.veinMiner());
        addChild((AbstractWidget) vein, PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
        addChild(new DividerWidget(this.width, 2, 0xFFFFFFFF), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        setHeight(levels.getHeight() + xpGains.getHeight() + party.getHeight() + scaling.getHeight() + vein.getHeight() + 42);
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    public boolean applyFilter(Filter filter) {
        levels.visible = !levels.applyFilter(filter);
        xpGains.visible = !xpGains.applyFilter(filter);
        party.visible = !party.applyFilter(filter);
        scaling.visible = !scaling.applyFilter(filter);
        vein.visible = !vein.applyFilter(filter);
        boolean filtered = (!levels.visible && ! xpGains.visible && !party.visible && !scaling.visible && !vein.visible)
                || !filter.getTextFilter().isEmpty()
                || !filter.matchesObject(OBJECT.NONE);
        setHeight(filtered ? 0 : levels.getHeight() + xpGains.getHeight() + party.getHeight() + scaling.getHeight() + vein.getHeight() + 42);
        return filtered;
    }

    private static void general(ResponsiveLayout layout, ServerData.General data, RegistryAccess access, Font font) {
        var blockRef = access.lookupOrThrow(Registries.BLOCK).get(ResourceKey.create(Registries.BLOCK, data.salvageBlock()));
        blockRef.ifPresent(ref -> {
            Component block = ref.value().asItem().getDefaultInstance().getDisplayName();
            MutableComponent text = LangProvider.SALVAGE_TUTORIAL_HEADER.asComponent().append(": ").append(block);
            layout.addString(text, PositionType.STATIC.constraint, textConstraint);
        });
        layout.addString(LangProvider.GLOSSARY_CONFIG_SERVER_GENERAL_TREASURE.asComponent(data.treasureEnabled()), PositionType.STATIC.constraint, textConstraint);
    }
}
