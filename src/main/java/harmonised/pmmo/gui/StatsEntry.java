package harmonised.pmmo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import java.util.List;


public class StatsEntry
{
    public static FontRenderer font = Minecraft.getMinecraft().fontRenderer;
    public ITextComponent title;
    public List<ITextComponent> text;
    public int x, y;

    public StatsEntry( int x, int y, ITextComponent title, List<ITextComponent> text )
    {
        this.x = x;
        this.y = y;
        this.title = title;
        this.text = text;
    }

    public int getHeight()
    {
        return text.size() * 11 + 15;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public void setX( int x )
    {
        this.x = x;
    }

    public void setY( int y )
    {
        this.y = y;
    }
}