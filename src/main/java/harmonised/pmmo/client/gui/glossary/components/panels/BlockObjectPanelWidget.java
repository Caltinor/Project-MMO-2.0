package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ItemStackWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ReqSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.VeinSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.XpSectionWidget;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.LogicalSide;

public class BlockObjectPanelWidget extends ObjectPanelWidget {
    private final String id;
    private final String name;
    private final XpSectionWidget xpSection;
    private final ReqSectionWidget reqSection;
    private final VeinSectionWidget veinSection;

    public BlockObjectPanelWidget(int color, int width, Block block) {this(color, width, block, null);}
    public BlockObjectPanelWidget(int color, int width, Block block, BlockEntity be) {
        super(color, width, Core.get(LogicalSide.CLIENT));
        Identifier rl = RegistryUtil.getId(block);
        this.id = rl.toString();
        this.name = block.getName().getString();
        addChild(new ItemStackWidget(block.asItem().getDefaultInstance()), PositionConstraints.grid(0, 0), SizeConstraints.builder()
                .absoluteHeight(18).absoulteWidth(18).build());
        addString(block.getName(), PositionConstraints.grid(0,1), textConstraint);
        this.xpSection = XpSectionWidget.create(block, be);
        addChild((AbstractWidget) xpSection, PositionConstraints.grid(1,1), SizeConstraints.builder().internalHeight().build());
        this.reqSection = ReqSectionWidget.create(block, be);
        addChild((AbstractWidget) reqSection, PositionConstraints.grid(2,1), SizeConstraints.builder().internalHeight().build());
        this.veinSection = new VeinSectionWidget(core.getLoader().BLOCK_LOADER.getData(rl).veinData(), false);
        addChild((AbstractWidget) veinSection, PositionConstraints.grid(3, 1), SizeConstraints.builder().internalHeight().build());
        addChild(new DividerWidget(200, 2, 0xFF000000), PositionConstraints.grid(4,1), SizeConstraints.builder()
                .absoluteHeight(2).build());
        this.setHeight(18 + xpSection.getHeight() + reqSection.getHeight() + veinSection.getHeight() + 2);

    }

    @Override
    public boolean applyFilter(Filter filter) {
        xpSection.visible = !xpSection.applyFilter(filter);
        reqSection.visible = !reqSection.applyFilter(filter);
        veinSection.visible = !veinSection.applyFilter(filter);
        this.setHeight(18 + xpSection.getHeight() + reqSection.getHeight() + veinSection.getHeight() + 2);
        return (!xpSection.visible && !reqSection.visible && !veinSection.visible)
                ||!filter.matchesObject(OBJECT.BLOCKS)
                || (!filter.matchesTextFilter(id)
                && !filter.matchesTextFilter(name));
    }
}
