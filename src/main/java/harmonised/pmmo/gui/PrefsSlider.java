package harmonised.pmmo.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Consumer;

import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraftforge.fmlclient.gui.widget.Slider;

public class PrefsSlider extends Slider
{
    private Consumer<PrefsSlider> guiResponder;
    private final boolean isSwitch;
    private static double lastValue;
    public String preference;

    public PrefsSlider(int xPos, int yPos, int width, int height, String preference, Component prefix, Component suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, boolean isSwitch, OnPress handler )
    {
        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler);
        this.preference = preference;
        this.isSwitch = isSwitch;
    }

    @Override
    public void updateSlider()
    {
        if( isSwitch )
        {
            this.sliderValue = this.sliderValue < 0.5 ? 0 : 1;
            if(drawString)
                setMessage( new TextComponent( this.sliderValue == 1 ? "On" : "Off" ));
        }
        else
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
                setMessage( new TextComponent( dispString.getString() + val + suffix.getString() ));
        }

        if (parent != null)
        {
            parent.onChangeSliderValue( this );
        }

        if (this.guiResponder != null)
            this.guiResponder.accept( this );
    }

    public void setResponder( Consumer<PrefsSlider> rssponderIn )
    {
        this.guiResponder = rssponderIn;
    }
}
