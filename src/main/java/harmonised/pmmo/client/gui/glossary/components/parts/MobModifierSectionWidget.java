package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.GuiEnumGroup;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.MobModifier;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MobModifierSectionWidget extends ReactiveWidget {
    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();

    private final List<MobModifier> globals;
    private final Map<Identifier, List<MobModifier>> mobModifiers;
    private final List<GuiEnumGroup> types = new ArrayList<>();
    public MobModifierSectionWidget(LocationData data) {
        super(0, 0, 0, 0);
        this.globals = data.globalModifiers();
        this.mobModifiers = data.mobModifiers();
        this.types.addAll(data.bonusMap().keySet());
        Font font = Minecraft.getInstance().font;
        if (!Config.server().mobScaling().enabled()) {
            this.visible = false;
            return;
        }
        if (!globals.isEmpty()) {
            addChild(build(LangProvider.GLOSSARY_HEADER_GLOBAL_MOB_MODIFIERS.asComponent(), font, this.width));
            var registry = Minecraft.getInstance().player.registryAccess().lookupOrThrow(Registries.ATTRIBUTE);
            globals.forEach(m -> addChild(build(Component.literal("   ").append(m.component(registry)), font, this.width)));
        }
        if (!mobModifiers.isEmpty()) {
            var access = Minecraft.getInstance().player.registryAccess();
            var entities = access.lookupOrThrow(Registries.ENTITY_TYPE);
            var attributes = access.lookupOrThrow(Registries.ATTRIBUTE);
            addChild(build(LangProvider.MOB_MODIFIER_HEADER.asComponent(), font, this.width));
            mobModifiers.forEach((mobID, modifierList) -> {
                Entity entity = entities.get(mobID).get().value().create(Minecraft.getInstance().level, EntitySpawnReason.COMMAND);
                if (entity instanceof LivingEntity living) {
                    addChild(build(living.getDisplayName(), font, this.width));
                    modifierList.forEach(m -> addChild(build(Component.literal("   ").append(m.component(attributes)), font, this.width)));
                }
            });
        }
        setHeight((getChildren().size() * 12) + 2);
    }

    private static Positioner<?> build(Component text, Font font, int width) {
        return new Positioner.Widget(new StringWidget(width, 9, text, font), PositionType.STATIC.constraint, textConstraint);
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = (globals.isEmpty() && mobModifiers.isEmpty())
                || !filter.matchesEnum(types)
//                || mobModifiers.keySet().stream().noneMatch(rl -> filter.matchesTextFilter(rl.toString()))
                || !filter.matchesSelection(SELECTION.MOB_SCALING);
        this.setHeight(filtered ? 0 : (getChildren().size() * 12) + 2);
        return filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
