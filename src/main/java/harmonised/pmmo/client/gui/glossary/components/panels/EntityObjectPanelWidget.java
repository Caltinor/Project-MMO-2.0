package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.EntityWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ReqSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.XpSectionWidget;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.LogicalSide;

public class EntityObjectPanelWidget extends ObjectPanelWidget {
    private final String id;
    private final String name;
    private final XpSectionWidget xpSection;
    private final ReqSectionWidget reqSection;

    public EntityObjectPanelWidget(int color, int width, Entity entity) {
        super(color, width, Core.get(LogicalSide.CLIENT));
        ResourceLocation rl = RegistryUtil.getId(Minecraft.getInstance().player.registryAccess(), entity);
        this.id = rl.toString();
        this.name = entity.getDisplayName().getString();
        addChild(new EntityWidget(entity), PositionConstraints.grid(0, 0), SizeConstraints.builder()
                .absoluteHeight(18).absoulteWidth(18).build());
        addChild(new StringWidget(entity.getDisplayName(), Minecraft.getInstance().font).alignLeft(), PositionConstraints.grid(0,1),
                SizeConstraints.builder().absoluteHeight(12).build());
        this.xpSection = XpSectionWidget.create(entity);
        addChild((AbstractWidget) xpSection, PositionConstraints.grid(1,1), SizeConstraints.builder().internalHeight().build());
        this.reqSection = ReqSectionWidget.create(entity);
        addChild((AbstractWidget) reqSection, PositionConstraints.grid(2,1), SizeConstraints.builder().internalHeight().build());
        addChild(new DividerWidget(200, 2, 0xFF000000), PositionConstraints.grid(4,1), SizeConstraints.builder()
                .absoluteHeight(2).build());
        this.setHeight(18 + xpSection.getHeight() + reqSection.getHeight() + 2);

    }

    @Override
    public boolean applyFilter(Filter filter) {
        xpSection.visible = !xpSection.applyFilter(filter);
        reqSection.visible = !reqSection.applyFilter(filter);
        this.setHeight(18 + xpSection.getHeight() + reqSection.getHeight() + 2);
        return (!xpSection.visible && !reqSection.visible)
                ||!filter.matchesObject(OBJECT.ENTITY)
                || (!filter.matchesTextFilter(id)
                && !filter.matchesTextFilter(name));
    }
}
