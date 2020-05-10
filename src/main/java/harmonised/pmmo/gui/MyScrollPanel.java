package harmonised.pmmo.gui;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;

import java.util.Map;

public class MyScrollPanel extends ScrollPanel
{
    MainWindow sr = Minecraft.getInstance().getMainWindow();
    PlayerEntity player;
    String type;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private Map<String, Map<String, Object>> theMap;

    public MyScrollPanel( Minecraft client, int width, int height, int top, int left, String type, PlayerEntity player )
    {
        super(client, width, height, top, left);
        this.player = player;
        this.type = type;
    }

    @Override
    protected int getContentHeight()
    {
        return 5000;
    }

    @Override
    protected void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
    {
        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );

        drawCenteredString(Minecraft.getInstance().fontRenderer, player.getDisplayName().getString() + " " + type,x + boxWidth / 2, relativeY, 50000 );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }
}
