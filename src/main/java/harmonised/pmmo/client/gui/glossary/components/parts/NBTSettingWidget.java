package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NBTSettingWidget extends ReactiveWidget {
    private boolean expanded = false;
    private final List<Positioner<?>> results = new ArrayList<>();
    private final List<Positioner<?>> nbtLogic = new ArrayList<>();
    private static final Component SUFFIX = Component.literal(" [NBT] ").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BLUE);

    public NBTSettingWidget(List<LogicEntry> setting, Map<String, ? extends Number> outputs, MutableComponent header) {
        super(0, 0, 0, 12);
        header.append(SUFFIX.copy());
        if (outputs.isEmpty()) {
            results.add(fromString(header, PositionType.STATIC.constraint, this.width));
        }
        else if (outputs.size() == 1) {
            Map.Entry<String, ?> entry = outputs.entrySet().iterator().next();
            Component skill = LangProvider.skill(entry.getKey()).append(": ").append(String.valueOf(entry.getValue()));
            results.add(fromString(header.append(skill), PositionType.STATIC.constraint, this.width));
        }
        else {
            results.add(fromString(header, PositionType.STATIC.constraint, this.width));
            for (Map.Entry<String, ?> entry : outputs.entrySet()) {
                Component skill = LangProvider.skill(entry.getKey()).append(": ").append(String.valueOf(entry.getValue()));
                results.add(fromString(skill, PositionConstraints.offset(10, 0), this.width));
            }
        }

        for (int i = 0; i < setting.size(); i++) {
            LogicEntry logic = setting.get(i);
            nbtLogic.add(fromString(LangProvider.GLOSSARY_NBT_ENTRY_HEADER.asComponent(i), PositionType.STATIC.constraint, this.width));
            nbtLogic.add(fromString(logic.behavior().translation.asComponent(), PositionConstraints.offset(10, 0), this.width));
            nbtLogic.add(fromString(logic.addCases()
                ? LangProvider.GLOSSARY_NBT_CASE_ADD.asComponent()
                : LangProvider.GLOSSARY_NBT_CASE_NOADD.asComponent(),
                PositionConstraints.offset(10, 0), this.width));
            for (LogicEntry.Case caso : logic.cases()) {
                if (caso.paths().size() == 1)
                    nbtLogic.add(fromString(LangProvider.GLOSSARY_NBT_CASE_PATH.asComponent(caso.paths().getFirst()), PositionConstraints.offset(20, 0), this.width));
                else {
                    nbtLogic.add(fromString(LangProvider.GLOSSARY_NBT_CASE_PATH.asComponent(), PositionConstraints.offset(20, 0), this.width));
                    caso.paths().forEach(path -> nbtLogic.add(fromString(Component.literal(path), PositionConstraints.offset(30, 0), this.width)));
                }
                for (LogicEntry.Criteria criteria : caso.criteria()) {
                    List<String> comparators = criteria.comparators().orElse(new ArrayList<>());
                    Component compComp = comparators.size() > 1
                            ? LangProvider.GLOSSARY_NBT_ANY_OF.asComponent(MsLoggy.listToString(comparators))
                            : Component.literal(MsLoggy.listToString(comparators));
                    Component values = criteria.skillMap().entrySet().stream()
                            .map(entry -> LangProvider.skill(entry.getKey()).append(": ").append(String.valueOf(entry.getValue())).append(" "))
                            .reduce(MutableComponent::append).orElse(Component.literal(""));
                    nbtLogic.add(fromString(criteria.operator().translation.asComponent(values, compComp), PositionConstraints.offset(30, 0), this.width));
                }
            }
        }

        results.forEach(this::addChild);
        nbtLogic.forEach(this::addChild);

        results.forEach(poser -> {if (poser.get() instanceof AbstractWidget widget) widget.visible = !expanded;});
        nbtLogic.forEach(poser -> {if (poser.get() instanceof AbstractWidget widget) widget.visible = expanded;});
    }

    @Override
    public void resize() {
        setHeight(visibleChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
    }

    private static Positioner<?> fromString(Component text, PositionConstraints constraints, int width) {
        return new Positioner.Widget(new StringWidget(width, 9, text, Minecraft.getInstance().font), constraints, textConstraint);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mbe, boolean isDoubleClick) {
        expanded = !expanded;
        results.forEach(poser -> {if (poser.get() instanceof AbstractWidget widget) widget.visible = !expanded;});
        nbtLogic.forEach(poser -> {if (poser.get() instanceof AbstractWidget widget) widget.visible = expanded;});
        return super.mouseClicked(mbe, isDoubleClick);
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
