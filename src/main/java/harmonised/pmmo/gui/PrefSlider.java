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
    public final int sliderWidth = 150, height = 16;
    private final int textFieldWidth = 36;

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

        slider = new Slider( 0, 0, sliderWidth, height, prefix, suffix, minVal, maxVal, curVal, showDec, showStr, button ->
        {
        });

        textField = new TextFieldWidget(font, 0, 0, textFieldWidth, height, "" );
        textField.setMaxStringLength( 5 );
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

    public int getWidth()
    {
        return sliderWidth + height + textFieldWidth;
    }

    public int getHeight()
    {
        return height + 11;
    }

    public int getX()
    {
        return slider.x;
    }

    public int getY()
    {
        return slider.y;
    }

    public void setX( int x )
    {
        slider.x = x;
        textField.x = x + sliderWidth;
        button.x = x + sliderWidth + textFieldWidth;
    }

    public void setY( int y )
    {
        slider.y = y;
        button.y = y;
        textField.y = y;
    }

    public void mouseClicked( double mouseX, double mouseY, int button )
    {
        this.slider.mouseClicked( mouseX, mouseY, button );
        this.button.mouseClicked( mouseX, mouseY, button );
    }

    public void mouseReleased( double mouseX, double mouseY, int button )
    {
        this.slider.mouseReleased( mouseX, mouseY, button );
        this.button.mouseReleased( mouseX, mouseY, button );
    }

    public void mouseDragged( double mouseX, double mouseY, int button, double deltaX, double deltaY )
    {
        this.slider.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        this.button.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
    }
}
