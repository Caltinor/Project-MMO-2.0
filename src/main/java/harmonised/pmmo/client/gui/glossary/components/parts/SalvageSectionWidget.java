package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.Positioner;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SalvageSectionWidget extends ReactiveWidget {
    private static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();

    Map<ResourceLocation, CodecTypes.SalvageData> salvage = new HashMap<>();
    public SalvageSectionWidget(ItemStack stack) {
        super(0, 0, 0, 0);
        RegistryAccess access = Minecraft.getInstance().player.registryAccess();
        Registry<Item> registry = access.registryOrThrow(Registries.ITEM);
        ResourceLocation id = RegistryUtil.getId(access, stack);
        Font font = Minecraft.getInstance().font;
        Core core = Core.get(LogicalSide.CLIENT);
        this.salvage = core.getLoader().ITEM_LOADER.getData(id).salvage();
        List<Positioner<?>> contentWidgets = new ArrayList<>();
        this.salvage.forEach((sid, data) -> contentWidgets.add(new Positioner.Widget(new SalvageEntryWidget(registry.get(sid).getDefaultInstance(), data, font), PositionType.STATIC.constraint, SizeConstraints.builder().internalHeight().build())));

        if (!salvage.isEmpty()) {
            addChild(new Positioner.Widget(new StringWidget(LangProvider.SALVAGE_HEADER.asComponent(), font).alignLeft(), PositionType.STATIC.constraint, textConstraint));
            contentWidgets.forEach(this::addChild);
            addChild(new DividerWidget(100, 1, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder().absoluteHeight(2).build());
        }
        setHeight(calculateHeight());
    }

    @Override public DisplayType getDisplayType() {return DisplayType.BLOCK;}

    private int calculateHeight() {
        return getChildren().stream().map(poser -> (poser.get() instanceof AbstractWidget widget && !widget.visible) ? 0 : poser.get().getHeight()).reduce(Integer::sum).orElse(0) + 2;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean childrenHidden = true;
        for (SalvageEntryWidget entry : this.getChildren().stream()
                .filter(poser -> poser.get() instanceof SalvageEntryWidget)
               .map(poser -> (SalvageEntryWidget)poser.get()).toList()) {
            boolean visible = !entry.applyFilter(filter);
            if (visible) childrenHidden = false;
            entry.visible = visible;
        }
        boolean filtered = salvage.isEmpty()
                || childrenHidden
                || !filter.matchesSelection(SELECTION.SALVAGE);
        this.setHeight(filtered ? 0 : calculateHeight());
        return filtered;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(this);
    }
}
