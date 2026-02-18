package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public class VeinSectionWidget extends ReactiveWidget {
    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();

    private final VeinData data;
    public VeinSectionWidget(VeinData data, boolean isItem) {
        super(0, 0, 0, 0);
        this.data = data;
        if (!data.isEmpty() && Config.server().veinMiner().enabled()) {
            Font font = Minecraft.getInstance().font;
            addString(LangProvider.VEIN_HEADER.asComponent().withStyle(ChatFormatting.YELLOW), PositionType.STATIC.constraint, textConstraint);
            if (isItem) {
                addString(LangProvider.VEIN_RATE.asComponent(DP.dpSoft(data.chargeRate.orElse(0d) * 2d)), PositionType.STATIC.constraint, textConstraint);
                addString(LangProvider.VEIN_CAP.asComponent(data.chargeCap.orElse(0)), PositionType.STATIC.constraint, textConstraint);
            } else {
                addString(LangProvider.VEIN_CONSUME.asComponent(data.consumeAmount.orElse(0)), PositionType.STATIC.constraint, textConstraint);
            }
            addChild(new DividerWidget(100, 1, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        }
        setHeight((getChildren().size() * 12) + 2);
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = data.isEmpty()
                || !filter.getSkill().isEmpty()
                || filter.getEnumGroup() != null
                || !filter.matchesSelection(SELECTION.VEIN);
        this.setHeight(filtered ? 0 : (getChildren().size() * 12) + 2);
        return filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
