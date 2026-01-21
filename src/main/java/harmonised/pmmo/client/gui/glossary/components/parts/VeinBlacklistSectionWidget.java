package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Consumer;

public class VeinBlacklistSectionWidget extends ReactiveWidget {
    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();

    private final List<ResourceLocation> data;
    public VeinBlacklistSectionWidget(List<ResourceLocation> data) {
        super(0, 0, 0, 0);
        this.data = data;
        if (!data.isEmpty() && Config.server().veinMiner().enabled()) {
            var reg = Minecraft.getInstance().player.registryAccess().lookupOrThrow(Registries.BLOCK);
            addString(LangProvider.VEIN_BLACKLIST_HEADER.asComponent(), PositionType.STATIC.constraint, textConstraint);
            for (ResourceLocation id : data) {
                Block block = reg.get(id).get().value();
                addString(block != null ? block.asItem().getDefaultInstance().getDisplayName() : Component.literal(id.toString()), PositionConstraints.offset(10, 0), textConstraint);
            }
        }
        setHeight((getChildren().size() * 12) + 2);
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = data.isEmpty()
                || filter.getEnumGroup() != null
                || data.stream().noneMatch(rl -> filter.matchesTextFilter(rl.toString()))
                || !filter.matchesSelection(SELECTION.VEIN);
        this.setHeight(filtered ? 0 : (getChildren().size() * 12) + 2);
        return filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
