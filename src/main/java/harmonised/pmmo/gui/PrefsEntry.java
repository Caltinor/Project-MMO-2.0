package harmonised.pmmo.gui;

import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;

public class PrefsEntry
{
    public static FontRenderer font = Minecraft.getMinecraft().fontRenderer;
    public PrefsSlider slider;
    public ResetButton button;
//    public TextFieldWidget textField;
    public String preference, prefix, suffix;
    public double defaultVal;
    public final int sliderWidth = 186, height = 16;
    private final int textFieldWidth = 36;
    public final boolean isSwitch, removeIfMax;

    public PrefsEntry(String preference, String prefix, String suffix, double minVal, double maxVal, double curVal, double defaultVal, boolean showDec, boolean showStr, boolean removeIfMax, boolean isSwitch )
    {
        this.preference = preference;
        this.prefix = prefix;
        this.suffix = suffix;
        this.isSwitch = isSwitch;
        this.removeIfMax = removeIfMax;

        if( defaultVal > maxVal )
            defaultVal = maxVal;
        if( defaultVal < minVal )
            defaultVal = minVal;
        if( curVal > maxVal )
            curVal = maxVal;
        if( curVal < minVal )
            curVal = minVal;

        this.defaultVal = defaultVal;

        slider = new PrefsSlider( 1337, 0, 0, sliderWidth, height, preference, prefix, suffix, minVal, maxVal, curVal, showDec, showStr, isSwitch );
//        if( !isSwitch )
//        {
//            textField = new WidgetText( font, 0, 0, textFieldWidth, height, "" );
//            textField.setMaxStringLength( 5 );
//            textField.setText( slider.getMessage() );
//        }
        button = new ResetButton(0, 0, height + (isSwitch ? textFieldWidth : 0), height, height, "R", button ->
        {
            resetValue();
        });
        //COUT
    }

    public void resetValue()
    {
        slider.setValue( defaultVal );
        slider.updateSlider();
//        if( isSwitch )
//            slider.setMessage( slider.getValue() == 1 ? "On" : "Off" );
//        else
//            textField.setText( slider.getMessage() );
        //COUT
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
//        if( isSwitch )
            button.x = x + sliderWidth;
//        else
//        {
//            button.x = x + sliderWidth + textFieldWidth;
//            textField.x = x + sliderWidth;
            //COUT
//        }
    }

    public void setY( int y )
    {
        slider.y = y;
        button.y = y;
//        if( !isSwitch )
//            textField.y = y;
        //COUT
    }

    public void mouseClicked( int mouseX, int mouseY )
    {
        this.slider.mousePressed( Minecraft.getMinecraft(), mouseX, mouseY );
        this.button.mousePressed( Minecraft.getMinecraft(), mouseX, mouseY );
    }

    public void mouseReleased( int mouseX, int mouseY )
    {
        this.slider.mouseReleased( mouseX, mouseY );
        this.button.mouseReleased( mouseX, mouseY );
    }

    public void mouseClickMove( int mouseX, int mouseY )
    {
        this.slider.mouseDragged( Minecraft.getMinecraft(), mouseX, mouseY );
        this.button.mouseDragged( Minecraft.getMinecraft(), mouseX, mouseY );
    }
}
