package harmonised.pmmo.api.client.wrappers;

import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.PositionType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;

public interface Positioner<T extends LayoutElement>{
    T get();
    PositionConstraints positioning();
    SizeConstraints constraints();
    void setWidth(int width);
    void setHeight(int height);

    record Widget(AbstractWidget widget, PositionConstraints positioning, SizeConstraints constraints) implements Positioner<AbstractWidget>{
        @Override public AbstractWidget get() {return widget();}
        @Override public void setWidth(int width) {widget.setWidth(width);}
        @Override public void setHeight(int height) {widget.setHeight(height);}
    }
    record Layout(ResponsiveLayout layout, PositionConstraints positioning, SizeConstraints constraints) implements Positioner<ResponsiveLayout> {
        @Override public ResponsiveLayout get() {return layout();}
        @Override public void setWidth(int width) {layout.setWidth(width);}
        @Override public void setHeight(int height) {layout.setHeight(height);}
    }
}
