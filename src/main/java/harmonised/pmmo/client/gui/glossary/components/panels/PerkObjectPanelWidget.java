package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ItemStackWidget;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.registry.PerkRegistry;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PerkObjectPanelWidget extends ObjectPanelWidget {
    private final EventType type;

    public PerkObjectPanelWidget(int color, int width, EventType event, List<CompoundTag> configurations) {
        super(color, width, Core.get(LogicalSide.CLIENT));
        this.setPadding(2, 0, 0, 0);
        this.type = event;
        Player player = Minecraft.getInstance().player;
        PerkRegistry reg = Core.get(LogicalSide.CLIENT).getPerkRegistry();
        if (!configurations.isEmpty()) {
            addString(LangProvider.PERK_GLOSSARY_EVENT_HEADER.asComponent(event.getName())
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(ChatFormatting.BOLD)
                            .withStyle(ChatFormatting.AQUA),
                    PositionType.STATIC.constraint, textConstraint);
            configurations.forEach(tag -> {
                ResourceLocation perkID = Reference.of(tag.getString("perk"));
                CompoundTag settings = new CompoundTag().merge(reg.getDefaults(perkID)).merge(tag.copy());
                addChild((AbstractWidget) reg.getRenderer(perkID).getWidget(this.width, player, settings), PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build());
            });
            addChild(new DividerWidget(427, 2, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder()
                    .absoluteHeight(2).build());
            this.setHeight(visibleChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
        }
    }

    @Override
    public List<Positioner<?>>visibleChildren() {
        return getChildren().stream().filter(poser -> !(poser.get() instanceof AbstractWidget widget) || widget.visible).toList();
    }

    public List<PanelWidget> getPerks() {
        return getChildren().stream()
                .filter(poser -> poser.get() instanceof PanelWidget)
                .map(poser -> (PanelWidget)poser.get()).toList();
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    public boolean applyFilter(Filter filter) {
        for (PanelWidget perk : getPerks()) {
            perk.visible = !perk.applyFilter(filter);
        }
        if (getPerks().stream().noneMatch(panel -> panel.visible)) {
            this.setHeight(0);
            return true;
        }
        this.setHeight(visibleChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
        return !filter.matchesObject(OBJECT.PERKS)
                || !filter.matchesSelection(SELECTION.PERKS)
                || !filter.matchesEnum(type);
    }
}
