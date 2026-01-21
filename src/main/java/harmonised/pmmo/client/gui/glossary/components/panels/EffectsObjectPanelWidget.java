package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.MobEffectWidget;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EffectsObjectPanelWidget extends ObjectPanelWidget {
    private final String id;
    private final String name;
    private final List<String> skills;

    public EffectsObjectPanelWidget(int color, int width, MobEffect effect) {
        super(color, width, Core.get(LogicalSide.CLIENT));
        var reg = Minecraft.getInstance().player.registryAccess().lookupOrThrow(Registries.MOB_EFFECT);
        ResourceLocation rl = reg.getKey(effect);
        Optional<Holder.Reference<MobEffect>> holder = reg.get(rl);
        EnhancementsData data = core.getLoader().EFFECT_LOADER.getData(rl);
        this.id = rl.toString();
        this.name = this.id;
        this.skills = data.skillArray().values().stream().map(Map::keySet).flatMap(Set::stream).toList();
        if (holder.isPresent()) {
            addChild(new MobEffectWidget(reg.get(rl).get()), PositionConstraints.grid(0, 0), SizeConstraints.builder()
                    .absoluteHeight(18).absoulteWidth(18).build());
            addChild(new StringWidget(effect.getDisplayName(), Minecraft.getInstance().font), PositionConstraints.grid(0, 1),
                    SizeConstraints.builder().absoluteHeight(12).build());
            int row = 1;
            for (AbstractWidget widget : build(data, Minecraft.getInstance().font)) {
                this.addChild(widget, PositionConstraints.grid(row, 1), textConstraint);
                row++;
            }
            addChild(new DividerWidget(200, 2, 0xFF000000), PositionConstraints.grid(row, 1), SizeConstraints.builder()
                    .absoluteHeight(2).build());
            this.setHeight((getChildren().size() * 12) - 16);
        }
    }

    private List<AbstractWidget> build(EnhancementsData data, Font font) {
        List<AbstractWidget> entries = new ArrayList<>();
        if (!data.skillArray().isEmpty())
            entries.add(new StringWidget(LangProvider.GLOSSARY_HEADER_EFFECT_XP.asComponent(), font));
        data.skillArray().forEach((level, map) -> {
            if (map.isEmpty()) return;
            MutableComponent text = levelAsComponent(level);
            if (map.size() > 1) {
                entries.add(new StringWidget(text, font));
                map.forEach((skill, xp) -> entries.add(new StringWidget(Component.literal("      ").append(LangProvider.skill(skill)).append(" ").append(xp.toString()), font)));
            }
            else {
                map.forEach((key, xp) -> text.append(LangProvider.skill(key)).append(" ").append(xp.toString()));
                entries.add(new StringWidget(text, font));
            }
        });
        return entries;
    }

    private MutableComponent levelAsComponent(int level) {
        return Component.literal(switch (level) {
            case 0 -> "   I:    ";
            case 1 -> "   II:   ";
            case 2 -> "   III:  ";
            case 3 -> "   IV:   ";
            case 4 -> "   V:    ";
            case 5 -> "   VI:   ";
            case 6 -> "   VII:  ";
            case 7 -> "   VIII: ";
            case 8 -> "   IX:   ";
            case 9 -> "   X:    ";
            default -> "   " + level + ": ";
        });
    }

    @Override
    public boolean applyFilter(Filter filter) {
        this.setHeight((getChildren().size() * 12) - 16);
        return !filter.matchesSkill(skills)
                || !filter.matchesObject(OBJECT.EFFECTS)
                || !filter.matchesSelection(SELECTION.XP)
                || !filter.matchesEnum(EventType.EFFECT)
                || (!filter.matchesTextFilter(id)
                && !filter.matchesTextFilter(name));
    }
}
