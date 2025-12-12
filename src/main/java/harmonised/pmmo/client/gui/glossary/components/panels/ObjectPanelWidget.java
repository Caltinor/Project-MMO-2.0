package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.core.Core;

public class ObjectPanelWidget extends PanelWidget{
    protected final Core core;

    public ObjectPanelWidget(int color, int width, Core core) {
        super(color, width);
        this.core = core;
    }

    protected static final SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();

    @Override public DisplayType getDisplayType() {return DisplayType.GRID;}
}
