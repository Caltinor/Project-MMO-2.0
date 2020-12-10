package harmonised.pmmo.gui;

import harmonised.pmmo.config.JType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.GuiModList;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListScrollPanel extends GuiScreen
{
    public Minecraft mc;
    public EntityPlayer player;
    public JType jType;
    ScaledResolution sr;
    public final int top, left, bottom, right, barLeft, border = 4, barWidth = 6;
    public int scrollDistance = 0, accumulativeHeight = 0;
    List<ListButton> buttons;
    public boolean scrolling = false;
    private int deltaY = 0, lastMouseY = 0;

    public ListScrollPanel( Minecraft client, int width, int height, int top, int left, JType jType, EntityPlayer player, ArrayList<ListButton> buttons )
    {
        super();
        this.player = player;
        this.jType = jType;
        this.buttons = buttons;

        this.mc = client;
        this.sr = new ScaledResolution( mc );
        this.width = width;
        this.height = height;
        this.top = top;
        this.left = left;
        this.bottom = height + this.top;
        this.right = width + this.left;
        this.barLeft = this.left + this.width - barWidth;
    }

    public int getScroll()
    {
        return (int) this.scrollDistance;
    }

    public void setScroll( int scroll )
    {
        this.scrollDistance = scroll;
    }

    public int getContentHeight()
    {
        int height = 48;

        for ( ListButton button : buttons )
        {
            height += button.getHeight() + 3;
        }

        return height;
    }

    protected void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
    {
        accumulativeHeight = 0;
        for( ListButton button : buttons )
        {
            button.x = this.right - button.width - 8;
            button.y = relativeY + accumulativeHeight;

            if( button.y + button.getHeight() + 2 > this.top && button.y - 2 < this.bottom )
            {
                if( button.unlocked )
                    drawGradientRect(this.left + 4, button.y - 2, this.right - 2, button.y + button.getHeight() + 2, 0x22444444, 0x33222222);
                else
                    drawGradientRect(this.left + 4, button.y - 2, this.right - 2, button.y + button.getHeight() + 2, 0xaa444444, 0xaa222222);

                button.drawButton( mc, mouseX, mouseY, 0 );

                drawString( mc.fontRenderer, button.title, this.left + 6, button.y + 2, button.unlocked ? 0x54fc54 : 0xfc5454 );

                int i = 0;
                for( String line : button.text )
                {
                    drawString( mc.fontRenderer, line, this.left + 6, button.y + 11 + (i++ * 9), 0xffffff );
                }
            }
            accumulativeHeight += button.getHeight() + 4;
        }
    }

    private int getBarHeight()
    {
        int barHeight = (height * height) / this.getContentHeight();

        if (barHeight < 32) barHeight = 32;

        if (barHeight > height - border*2)
            barHeight = height - border*2;

        return barHeight;
    }

    public int getTop()
    {
        return this.top;
    }

    public int getBottom()
    {
        return this.bottom;
    }

    @Override
    public void drawScreen( int mouseX, int mouseY, float partialTicks )
    {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldr = tess.getBuffer();


        double scale = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        GL11.glScissor((int)(left  * scale), (int)(client.getMainWindow().getFramebufferHeight() - (bottom * scale)),
        GL11.glScissor((int)(left  * scale), (int)(mc.displayHeight - (bottom * scale)),
                (int)(width * scale), (int)(height * scale));

        int baseY = this.top + border - (int)this.scrollDistance;
        this.drawPanel(right, baseY, tess, mouseX, mouseY);

        GlStateManager.disableDepth();

        int extraHeight = (this.getContentHeight() + border) - height;
        if (extraHeight > 0)
        {
            int barHeight = getBarHeight();

            int barTop = (int)this.scrollDistance * (height - barHeight) / extraHeight + this.top;
            if (barTop < this.top)
            {
                barTop = this.top;
            }

            GlStateManager.disableTexture2D();
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

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

//    @Override
//    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
//    {
//        if (scroll != 0)
//        {
//            this.scrollDistance += -scroll * getScrollAmount();
//            applyScrollLimits();
//            return true;
//        }
//        return false;
//    }

    protected boolean clickPanel(double mouseX, double mouseY, int button) { return false; }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, button);

        this.scrolling = button == 0 && mouseX >= barLeft && mouseX < barLeft + barWidth;
        if ( !this.scrolling )
        {
            int mouseListY = ((int)mouseY) - this.top - this.getContentHeight() + (int)this.scrollDistance - border;
            if (mouseX >= left && mouseX <= right && mouseListY < 0)
            {
                this.clickPanel(mouseX - left, mouseY - this.top + (int)this.scrollDistance - border, button);
            }
        }
    }

    @Override
    public void mouseReleased( int mouseX, int mouseY, int button )
    {
        super.mouseReleased( mouseX, mouseY, button );
        this.scrolling = false;
    }

    private int getMaxScroll()
    {
        return this.getContentHeight() - (this.height - this.border);
    }

    public void scroll( int amount )
    {
        setScroll( getScroll() - amount );
        applyScrollLimits();
    }

    private void applyScrollLimits()
    {
        int max = getMaxScroll();

        if (max < 0)
        {
            max /= 2;
        }

        this.scrollDistance = Math.min( max, Math.max( 0, this.scrollDistance ) );
    }


    @Override
    public void mouseClickMove( int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick )
    {
        if (this.scrolling)
        {
            deltaY = mouseY - lastMouseY;
            int maxScroll = height - getBarHeight();
            double moved = deltaY / (double) maxScroll;
            this.scrollDistance += getMaxScroll() * moved;
            applyScrollLimits();
            lastMouseY = mouseY;
        }
    }
}
