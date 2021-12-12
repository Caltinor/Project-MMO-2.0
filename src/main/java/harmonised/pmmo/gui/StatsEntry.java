package harmonised.pmmo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextComponent;

import java.util.List;

public class StatsEntry
{
    public static FontRenderer font = Minecraft.getInstance().fontRenderer;
    public TextComponent title;
    public List<IFormattableTextComponent> text;
    public int x, y;

    public StatsEntry(int x, int y, TextComponent title, List<IFormattableTextComponent> text)
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

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }
}