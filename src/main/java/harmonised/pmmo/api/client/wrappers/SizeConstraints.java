package harmonised.pmmo.api.client.wrappers;

import harmonised.pmmo.api.client.types.SizeType;
import net.minecraft.util.Mth;

public record SizeConstraints(
        SizeType minWidth,
        SizeType maxWidth,
        SizeType minHeight,
        SizeType maxHeight
) {
    public static final SizeConstraints DEFAULT = new SizeConstraints(
            new SizeType(SizeType.Type.PERCENT, 0.0),
            new SizeType(SizeType.Type.PERCENT, 1.0),
            new SizeType(SizeType.Type.PERCENT, 0.0),
            new SizeType(SizeType.Type.PERCENT, 1.0)
    );

    public static Builder builder() {return new Builder();}

    public void apply(Positioner<?> poser, int parentWidth, int parentHeight) {
        poser.setWidth(Mth.clamp(parentWidth, minWidth.get(parentWidth, poser.get().getWidth()), maxWidth.get(parentWidth, poser.get().getWidth())));
        poser.setHeight(Mth.clamp(parentHeight, minHeight.get(parentHeight, poser.get().getHeight()), maxHeight.get(parentHeight, poser.get().getHeight())));
    }
    
    public static class Builder {
        SizeType minWidth = new SizeType(SizeType.Type.PERCENT, 0.0);
        SizeType maxWidth = new SizeType(SizeType.Type.PERCENT, 1.0);
        SizeType minHeight = new SizeType(SizeType.Type.PERCENT, 0.0);
        SizeType maxHeight = new SizeType(SizeType.Type.PERCENT, 1.0);
        private Builder() {}
        
        public Builder minWidthAbsolute(double value) {
            minWidth = SizeType.absolute(value);
            return this;
        }
        public Builder minWidthPercent(double value) {
            minWidth = SizeType.percent(value);
            return this;
        }
        public Builder maxWidthAbsolute(double value) {
            maxWidth = SizeType.absolute(value);
            return this;
        }
        public Builder maxWidthPercent(double value) {
            maxWidth = SizeType.percent(value);
            return this;
        }
        public Builder absoulteWidth(double value) {
            maxWidth = minWidth = SizeType.absolute(value);
            return this;
        }
        public Builder internalWidth() {
            maxWidth = minWidth = SizeType.INTERNAL;
            return this;
        }
        public Builder minHeightAbsolute(double value) {
            minHeight = SizeType.absolute(value);
            return this;
        }
        public Builder minHeightPercent(double value) {
            minHeight = SizeType.percent(value);
            return this;
        }
        public Builder maxHeightAbsolute(double value) {
            maxHeight = SizeType.absolute(value);
            return this;
        }
        public Builder maxHeightPercent(double value) {
            maxHeight = SizeType.percent(value);
            return this;
        }
        public Builder absoluteHeight(double value) {
            maxHeight = minHeight = SizeType.absolute(value);
            return this;
        }
        public Builder internalHeight() {
            maxHeight = minHeight = SizeType.INTERNAL;
            return this;
        }
        public SizeConstraints build() {return new SizeConstraints(minWidth, maxWidth, minHeight, maxHeight);}
    }
}
