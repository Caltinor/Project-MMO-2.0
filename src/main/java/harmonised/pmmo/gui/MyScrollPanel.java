package harmonised.pmmo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.gui.ScrollPanel;

import java.util.Map;

public class MyScrollPanel extends ScrollPanel
{
    private Map<String, Map<String, Object>> theMap;

    public MyScrollPanel( Minecraft client, int width, int height, int top, int left, Map<String, Map<String, Object>> theMap )
    {
        super(client, width, height, top, left);
        this.theMap = theMap;
    }

    @Override
    protected int getContentHeight()
    {
        return 5000;
    }

    @Override
    protected void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
    {

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }
}
