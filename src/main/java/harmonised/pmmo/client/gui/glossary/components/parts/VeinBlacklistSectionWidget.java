package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
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
        if (!data.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            var reg = Minecraft.getInstance().player.registryAccess().registryOrThrow(Registries.BLOCK);
            addChild(build(LangProvider.VEIN_BLACKLIST_HEADER.asComponent(), font));
            for (ResourceLocation id : data) {
                Block block = reg.get(id);
                addChild(build(block != null ? block.asItem().getDefaultInstance().getDisplayName() : Component.literal(id.toString()), font));
            }
        }
        setHeight((getChildren().size() * 12) + 2);
    }

    private static Positioner<?> build(Component text, Font font) {
        return new Positioner.Widget(new StringWidget(text, font).alignLeft(), PositionType.STATIC.constraint, textConstraint);
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = data.isEmpty() || data.stream().noneMatch(rl -> filter.matchesTextFilter(rl.toString())) || !filter.matchesSelection(SELECTION.VEIN);
        this.setHeight(filtered ? 0 : (getChildren().size() * 12) + 2);
        return filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
