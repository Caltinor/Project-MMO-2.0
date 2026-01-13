package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PosNegEffectSectionWidget extends ReactiveWidget {
    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();

    private final Map<String, Long> reqs;
    private final Map<ResourceLocation, Integer> pos;
    private final Map<ResourceLocation, Integer> neg;
    public PosNegEffectSectionWidget(LocationData data, boolean isBiome) {
        super(0, 0, 0, 0);
        this.reqs = data.travelReq();
        this.pos = data.getPositiveEffect();
        this.neg = data.getNegativeEffect();
        if (!pos.isEmpty() || !reqs.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            if (!reqs.isEmpty()) {
                setSkills(reqs, LangProvider.GLOSSARY_HEADER_TRAVEL_REQ.asComponent(), font).forEach(this::addChild);
                if (isBiome && !neg.isEmpty()) {
                    setEffects(neg, LangProvider.BIOME_EFFECT_NEG.asComponent(), font).forEach(this::addChild);
                }
            }
            if (!pos.isEmpty()) {
                setEffects(pos, LangProvider.LOCATION_EFFECT_POS.asComponent(), font).forEach(this::addChild);
            }
            addChild(new DividerWidget(100, 1, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        }
        setHeight((getChildren().size() * 12) + 2);
    }

    private static Positioner<?> build(Component text, Font font) {
        return new Positioner.Widget(new StringWidget(text, font).alignLeft(), PositionType.STATIC.constraint, textConstraint);
    }

    private static List<Positioner<?>> setSkills(Map<String, Long> map, Component header, Font font) {
        List<Positioner<?>> skillWidgets = new ArrayList<>(List.of(build(header, font)));
        MutableComponent prefix = Component.literal(map.values().stream().filter(s -> s > 0).count() > 1 ? "   " : "");
        map.forEach((skill, value) -> {
            if (value > 0)
                skillWidgets.add(build(prefix.copy().append(LangProvider.skill(skill)).append(": ").append(value.toString()), font));
        });
        return skillWidgets.size() > 1 ? skillWidgets : new ArrayList<>();
    }

    private static List<Positioner<?>> setEffects(Map<ResourceLocation, Integer> map, Component header, Font font) {
        List<Positioner<?>> skillWidgets = new ArrayList<>(List.of(build(header, font)));
        MutableComponent prefix = Component.literal(map.values().stream().filter(s -> s > 0).count() > 1 ? "   " : "");
        var reg = Minecraft.getInstance().player.registryAccess().registryOrThrow(Registries.MOB_EFFECT);
        map.forEach((skill, value) -> {
            if (value > 0) {
                var effect = reg.get(skill);
                if (effect == null) return;
                skillWidgets.add(build(prefix.copy().append(effect.getDisplayName()).append(": ").append(String.valueOf(value + 1)), font));
            }
        });
        return skillWidgets.size() > 1 ? skillWidgets : new ArrayList<>();
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = (reqs.isEmpty() && pos.isEmpty())
                || !filter.matchesSelection(SELECTION.REQS)
                || !filter.matchesEnum(ReqType.TRAVEL)
                || !filter.matchesSkill(reqs.keySet());
        this.setHeight(filtered ? 0 : (getChildren().size() * 12) + 2);
        return filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
