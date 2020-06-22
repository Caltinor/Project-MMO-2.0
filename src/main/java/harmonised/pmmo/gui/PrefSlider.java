package harmonised.pmmo.gui;

import harmonised.pmmo.util.DP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class PrefSlider
{
    public static FontRenderer font = Minecraft.getInstance().fontRenderer;
    public Slider slider;
    public Button button;
    public TextFieldWidget textField;
    public String preference, prefix, suffix;
    public double defaultVal;
    public int width = 150, height = 16;

    public PrefSlider( String preference, String prefix, String suffix, double minVal, double maxVal, double curVal, double defaultVal, boolean showDec, boolean showStr )
    {
        this.preference = preference;
        this.prefix = prefix;
        this.suffix = suffix;

        if( defaultVal > maxVal )
            defaultVal = maxVal;
        if( defaultVal < minVal )
            defaultVal = minVal;
        if( curVal > maxVal )
            curVal = maxVal;
        if( curVal < minVal )
            curVal = minVal;

        this.defaultVal = defaultVal;

        slider = new Slider( 0, 0, width, height, prefix, suffix, minVal, maxVal, curVal, showDec, showStr, button ->
        {
        });

        textField = new TextFieldWidget(font, 0, 0, 32, height, "" );
        textField.setMaxStringLength( 4 );
        textField.setText( DP.dpSoft( curVal ) );

        button = new Button(0, 0, height, height, "R", button ->
        {
            resetValue();
        });
    }

    public void resetValue()
    {
        slider.setValue( defaultVal );
        textField.setText(DP.dpSoft( defaultVal ) );
    }

    public void updateX( int x )
    {
        slider.x = x;
        textField.x = x + width;
        button.x = x + width + textField.getWidth();
    }

    public void updateY( int y )
    {
        slider.y = y;
        button.y = y;
        textField.y = y;
    }
}
