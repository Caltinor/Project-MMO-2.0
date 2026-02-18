package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.parts.BonusSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ItemStackWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ReqSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.SalvageSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.VeinSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.XpSectionWidget;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;

public class ItemObjectPanelWidget extends ObjectPanelWidget {
    private final String id;
    private final String name;
    private final XpSectionWidget xpSection;
    private final ReqSectionWidget reqSection;
    private final BonusSectionWidget bonusSection;
    private final SalvageSectionWidget salvageSection;
    private final VeinSectionWidget veinSection;

    public ItemObjectPanelWidget(int color, int width, ItemStack stack) {
        super(color, width, Core.get(LogicalSide.CLIENT));
        Identifier rl = RegistryUtil.getId(Minecraft.getInstance().level.registryAccess() ,stack);
        this.id = rl.toString();
        this.name = stack.getDisplayName().getString();
        addChild(new ItemStackWidget(stack), PositionConstraints.grid(0, 0), SizeConstraints.builder()
                .absoluteHeight(18).absoulteWidth(18).build());
        addString(Component.literal("")
                        .append(stack.getDisplayName())
                        .append(stack.getItem() instanceof BlockItem ? LangProvider.GLOSSARY_HEADER_AS_ITEM.asComponent() : Component.literal("")),
                PositionConstraints.grid(0,1),
                SizeConstraints.builder().absoluteHeight(12).build());
        this.xpSection = XpSectionWidget.create(stack);
        addChild((AbstractWidget) xpSection, PositionConstraints.grid(1,1), SizeConstraints.builder().internalHeight().build());
        this.reqSection = ReqSectionWidget.create(stack);
        addChild((AbstractWidget) reqSection, PositionConstraints.grid(2,1), SizeConstraints.builder().internalHeight().build());
        this.bonusSection = BonusSectionWidget.create(stack);
        addChild((AbstractWidget) bonusSection, PositionConstraints.grid(3, 1), SizeConstraints.builder().internalHeight().build());
        this.salvageSection = new SalvageSectionWidget(stack);
        addChild((AbstractWidget) salvageSection, PositionConstraints.grid(4, 1), SizeConstraints.builder().internalHeight().build());
        this.veinSection = new VeinSectionWidget(core.getLoader().ITEM_LOADER.getData(rl).veinData(), true);
        addChild((AbstractWidget) veinSection, PositionConstraints.grid(5, 1), SizeConstraints.builder().internalHeight().build());
        addChild(new DividerWidget(200, 2, 0xFF000000), PositionConstraints.grid(6,1), SizeConstraints.builder()
                .absoluteHeight(2).build());
        this.setHeight(18 + xpSection.getHeight() + reqSection.getHeight() + bonusSection.getHeight() + salvageSection.getHeight() + veinSection.getHeight() + 2);

    }

    @Override
    public boolean applyFilter(Filter filter) {
        xpSection.visible = !xpSection.applyFilter(filter);
        reqSection.visible = !reqSection.applyFilter(filter);
        bonusSection.visible = !bonusSection.applyFilter(filter);
        salvageSection.visible = !salvageSection.applyFilter(filter);
        veinSection.visible = !veinSection.applyFilter(filter);
        this.setHeight(18 + xpSection.getHeight() + reqSection.getHeight() + bonusSection.getHeight() + salvageSection.getHeight() + veinSection.getHeight() + 2);
        return (!xpSection.visible && !reqSection.visible && !bonusSection.visible && !salvageSection.visible && !veinSection.visible)
                ||!filter.matchesObject(OBJECT.ITEMS)
                || (!filter.matchesTextFilter(id)
                && !filter.matchesTextFilter(name));
    }
}
