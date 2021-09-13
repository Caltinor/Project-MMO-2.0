package harmonised.pmmo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.BaseComponent;

import java.util.List;

public class StatsEntry
{
    public static Font font = Minecraft.getInstance().font;
    public BaseComponent title;
    public List<MutableComponent> text;
    public int x, y;

    public StatsEntry( int x, int y, BaseComponent title, List<MutableComponent> text )
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