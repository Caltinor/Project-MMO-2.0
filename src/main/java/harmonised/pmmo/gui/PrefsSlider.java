package harmonised.pmmo.gui;

import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.function.Consumer;

public class PrefsSlider extends Slider
{
    private Consumer<PrefsSlider> guiResponder;
    public String preference;

    public PrefsSlider(int xPos, int yPos, int width, int height, String preference, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler)
    {
        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler);
        this.preference = preference;
    }

    @Override
    public void updateSlider()
    {
        if (this.sliderValue < 0.0F)
            this.sliderValue = 0.0F;

        if (this.sliderValue > 1.0F)
            this.sliderValue = 1.0F;

        String val;

        if (showDecimal)
        {
            val = Double.toString(sliderValue * (maxValue - minValue) + minValue);

            if (val.substring(val.indexOf(".") + 1).length() > precision)
            {
                val = val.substring(0, val.indexOf(".") + precision + 1);

                if (val.endsWith("."))
                {
                    val = val.substring(0, val.indexOf(".") + precision);
                }
            }
            else
            {
                while (val.substring(val.indexOf(".") + 1).length() < precision)
                {
                    val = val + "0";
                }
            }
        }
        else
        {
            val = Integer.toString((int)Math.round(sliderValue * (maxValue - minValue) + minValue));
        }

        if(drawString)
        {
            setMessage(dispString + val + suffix);
        }

        if (parent != null)
        {
            parent.onChangeSliderValue(this);
        }

        if (this.guiResponder != null)
            this.guiResponder.accept( this );
    }

    public void setResponder( Consumer<PrefsSlider> rssponderIn )
    {
        this.guiResponder = rssponderIn;
    }
}
