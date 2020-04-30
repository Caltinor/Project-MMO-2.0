package harmonised.pmmo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.gui.ScrollPanel;

public class MyScrollPanel extends ScrollPanel
{
    public MyScrollPanel(Minecraft client, int width, int height, int top, int left)
    {
        super(client, width, height, top, left);
    }

    @Override
    protected int getContentHeight() {
        return 0;
    }

    @Override
    protected void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY) {

    }
}
