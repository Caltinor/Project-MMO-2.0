package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class SalvageEntryWidget extends ReactiveWidget {
    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();
    Set<String> skills = new HashSet<>();

    public SalvageEntryWidget(ItemStack salvage, CodecTypes.SalvageData data, Font font) {
        super(0, 0, 0, 0);
        addChild(new Positioner.Widget(new ItemStackWidget(salvage), PositionConstraints.grid(0, 0), SizeConstraints.builder().absoluteHeight(18).absoulteWidth(18).build()));
        addChild(new Positioner.Widget(new StringWidget(Component.literal(data.salvageMax()+"x ").append(salvage.getDisplayName()), font).alignLeft(), PositionConstraints.grid(0, 1), textConstraint));
        addChild(new Positioner.Widget(new StringWidget(LangProvider.SALVAGE_CHANCE.asComponent(DP.dpSoft(data.baseChance()*100)+"%", DP.dpSoft(data.maxChance()*100)+"%"), font).alignLeft(), PositionConstraints.grid(1,1), textConstraint));
        var chanceSkills = inlineSkills(data.chancePerLevel(), 2, LangProvider.SALVAGE_CHANCE_MOD.asComponent(), font);
        chanceSkills.forEach(this::addChild);
        int lastRow = 2 + chanceSkills.size();
        var reqSkills = inlineSkills(data.levelReq(), lastRow, LangProvider.SALVAGE_LEVEL_REQ.asComponent(), font);
        reqSkills.forEach(this::addChild);
        lastRow += reqSkills.size();
        var xpSkills = inlineSkills(data.xpAward(), lastRow, LangProvider.SALVAGE_XP_AWARD.asComponent(), font);
        xpSkills.forEach(this::addChild);
        setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(18));
        skills.addAll(data.xpAward().keySet());
        skills.addAll(data.levelReq().keySet());
        skills.addAll(data.chancePerLevel().keySet());
    }

    @Override public DisplayType getDisplayType() {return DisplayType.GRID;}

    private int calculateHeight() {
        return getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0) + 2;
    }

    private static List<Positioner<?>> inlineSkills(Map<String, ? extends Number> skills, int startRow, Component header, Font font) {
        List<Positioner<?>> outLines = new ArrayList<>();
        return switch (skills.size()) {
            case 0 -> outLines;
            case 1 -> {
                String skill = skills.keySet().iterator().next();
                StringWidget headWidget = new StringWidget(header.copy()
                        .append(LangProvider.skill(skill))
                        .append(": ")
                        .append(parse(skills.get(skill))), font).alignLeft();
                outLines.add(new Positioner.Widget(headWidget, PositionConstraints.grid(startRow, 1), textConstraint));
                yield outLines;
            }
            default -> {
                outLines.add(new Positioner.Widget(new StringWidget(header, font).alignLeft(), PositionConstraints.grid(startRow, 1), textConstraint));
                Iterator<String> keys = skills.keySet().iterator();
                for (int row = 0; row < skills.size(); row++) {
                    String skill = keys.next();
                    outLines.add(new Positioner.Widget(
                            new StringWidget(Component.literal("   ").append(LangProvider.skill(skill)).append(": ").append(parse(skills.get(skill))), font).alignLeft(),
                            PositionConstraints.grid(row + startRow + 1, 1),
                            textConstraint));
                }
                yield outLines;
            }
        };
    }

    private static String parse(Number value) {
        if (value instanceof Double dub)
            return DP.dpSoft(dub*100)+"%";
        else return value.toString();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = !filter.matchesSkill(skills);
        this.setHeight(filtered ? 0 : calculateHeight());
        return filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
