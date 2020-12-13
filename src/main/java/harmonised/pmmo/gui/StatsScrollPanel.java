package harmonised.pmmo.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.gui.ScrollPanel;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class StatsScrollPanel extends ScrollPanel
{
    MainWindow sr;
    private final int boxWidth = 256;
    private final int boxHeight = 256;
    private final List<StatsEntry> statsEntries;
    private StatsEntry statsEntry;
    private FontRenderer font;

    private final Minecraft client;
    private final int width, height, top, bottom, right, left, barLeft, border = 4, barWidth = 6;

    public StatsScrollPanel( Minecraft client, int width, int height, int top, int left, List<StatsEntry> statsEntries )
    {
        super(client, width, height, top, left);
        this.statsEntries = statsEntries;
        this.sr = Minecraft.getInstance().getMainWindow();
        this.font = Minecraft.getInstance().fontRenderer;

        this.client = client;
        this.width = width;
        this.height = height;
        this.top = top;
        this.left = left;
        this.bottom = height + this.top;
        this.right = width + this.left;
        this.barLeft = this.left + this.width - barWidth;
    }

    @Override
    protected int getContentHeight()
    {
        int height = -4;

        for( StatsEntry a : statsEntries )
        {
            height += a.getHeight() + 2;
        }

        return height;
    }

    @Override
    protected void drawPanel( int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
    {
        int accumulativeHeight = 0;
        for( int i = 0; i < statsEntries.size(); i++ )
        {
            statsEntry = statsEntries.get( i );
            statsEntry.setX( this.left + 6 );
            statsEntry.setY( relativeY + accumulativeHeight );

            TextFormatting color = statsEntry.title.getStyle().getColor();
            int hexColor = color == null ? 0xffffff : ( color.getColor() == null ? 0xffffff : color.getColor() );

            fillGradient( this.left + 4, statsEntry.getY() - 2, this.right - 2, statsEntry.getY() + statsEntry.getHeight() + 2, 0x22444444, 0x33222222 );
            drawCenteredString( font, statsEntry.title.getString(), sr.getScaledWidth()/2, statsEntry.getY(), hexColor );
            for( int j = 0; j < statsEntry.text.size(); j++ )
            {
                TextComponent line = statsEntry.text.get( j );
                color = line.getStyle().getColor();
                hexColor = color == null ? 0xffffff : ( color.getColor() == null ? 0xffffff : color.getColor() );
                drawString( font, line.getString(), statsEntry.getX(), statsEntry.getY() + (j+1)*11, hexColor );
            }

            accumulativeHeight += statsEntry.getHeight() + 4;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    public int getScroll()
    {
        return (int) this.scrollDistance;
    }

    public void setScroll( int scroll )
    {
        this.scrollDistance = scroll;
    }

    public int getTop()
    {
        return this.top;
    }

    public int getBottom()
    {
        return this.bottom;
    }

    public int getRelativeY()
    {
        return this.top + this.border - (int) this.scrollDistance;
    }

    @Override
    public void render(  int mouseX, int mouseY, float partialTicks)
    {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldr = tess.getBuffer();

        double scale = client.getMainWindow().getGuiScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left  * scale), (int)(client.getMainWindow().getFramebufferHeight() - (bottom * scale)),
                (int)(width * scale), (int)(height * scale));

        int baseY = this.top + border - (int)this.scrollDistance;
        this.drawPanel( right, baseY, tess, mouseX, mouseY);

        RenderSystem.disableDepthTest();

        int extraHeight = (this.getContentHeight() + border) - height;
        if (extraHeight > 0)
        {
            int barHeight = getBarHeight();

            int barTop = (int)this.scrollDistance * (height - barHeight) / extraHeight + this.top;
            if (barTop < this.top)
            {
                barTop = this.top;
            }

            RenderSystem.disableTexture();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(barLeft,            this.bottom, 0.0D).tex(0.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth, this.bottom, 0.0D).tex(1.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth, this.top,    0.0D).tex(1.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(barLeft,            this.top,    0.0D).tex(0.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(barLeft,            barTop + barHeight, 0.0D).tex(0.0F, 1.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth, barTop + barHeight, 0.0D).tex(1.0F, 1.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth, barTop,             0.0D).tex(1.0F, 0.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(barLeft,            barTop,             0.0D).tex(0.0F, 0.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(barLeft,                barTop + barHeight - 1, 0.0D).tex(0.0F, 1.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth - 1, barTop + barHeight - 1, 0.0D).tex(1.0F, 1.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth - 1, barTop,                 0.0D).tex(1.0F, 0.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(barLeft,                barTop,                 0.0D).tex(0.0F, 0.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            tess.draw();
        }

        RenderSystem.enableTexture();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private int getBarHeight()
    {
        int barHeight = (height * height) / this.getContentHeight();

        if (barHeight < 32) barHeight = 32;

        if (barHeight > height - border*2)
            barHeight = height - border*2;

        return barHeight;
    }
}