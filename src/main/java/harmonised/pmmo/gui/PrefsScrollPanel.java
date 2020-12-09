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

public class PrefsScrollPanel extends GuiScreen
{
    public Minecraft mc;
    public EntityPlayer player;
    public JType jType;
    ScaledResolution sr;
    public final int top, left, bottom, right, barLeft, border = 4, barWidth = 6;
    public int scrollDistance = 0, accumulativeHeight = 0;
    private final List<PrefsEntry> prefsEntries;
    private PrefsEntry prefEntry;
    public boolean scrolling = false;
    private int deltaY = 0, lastMouseY = 0;

    public PrefsScrollPanel( Minecraft client, int width, int height, int top, int left, JType jType, EntityPlayer player, List<PrefsEntry> prefsEntries )
    {
        super();
        this.player = player;
        this.jType = jType;
        this.prefsEntries = prefsEntries;

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

        for( int i = 0; i < prefsEntries.size(); i++ )
        {
            height += prefsEntries.get(i).getHeight() + 2;
        }

        return height;
    }

    protected void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
    {
        for( int i = 0; i < prefsEntries.size(); i++ )
        {
            prefEntry = prefsEntries.get( i );
            prefEntry.setX( this.left + 6 );
            prefEntry.setY( 7 + relativeY + ( prefEntry.getHeight() + 2) * i );
//            slider = prefEntry.slider;

//            if( prefEntry.y + prefEntry.getHeight() > this.top && prefEntry.y - prefEntry.getHeight() < this.bottom )
//            {
//                if( prefEntry.isSwitch )
//                {
//                    if( slider.getValue() == 1 )
//                        fillGradient(this.left + 4, prefEntry.y - 11, this.right - 2, prefEntry.y + slider.getHeight() + 2, 0x22444444, 0x33222222);
//                    else
//                        fillGradient(this.left + 4, prefEntry.y - 11, this.right - 2, prefEntry.y + slider.getHeight() + 2, 0xaa444444, 0xaa222222);
//                }
//                else
//                {
//                    if( (double) prefEntry.defaultVal == (double) slider.getValue() )
//                        fillGradient(this.left + 4, prefEntry.y - 11, this.right - 2, prefEntry.y + slider.getHeight() + 2, 0xaa444444, 0xaa222222);
//                    else
//                        fillGradient(this.left + 4, prefEntry.y - 11, this.right - 2, prefEntry.y + slider.getHeight() + 2, 0x22444444, 0x33222222);
//                }

//                drawCenteredString( font, prefEntry.preference, prefEntry.x + slider.getWidth() / 2, prefEntry.y - 9, 0xffffff );
                drawCenteredString( fontRenderer, prefEntry.preference, prefEntry.button.x, prefEntry.button.y, 0xffffff );
//                slider.render(mouseX, mouseY, 0);
                prefEntry.button.drawButton( mc, mouseX, mouseY, 0);
//                if( !prefEntry.isSwitch )
//                    prefEntry.textField.render(mouseX, mouseY, 0);
                //COUT
//            }
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
//package harmonised.pmmo.gui;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import org.lwjgl.opengl.GL11;
//
//import java.util.ArrayList;
//
//public class PrefsScrollPanel extends PmmoScrollPanel
//{
//    ScaledResolution sr = new ScaledResolution( Minecraft.getMinecraft() );
//    private final int boxWidth = 256;
//    private final int boxHeight = 256;
//    private final ArrayList<PrefsEntry> prefsEntries;
//    private PrefsEntry prefEntry;
//    private FontRenderer font = Minecraft.getMinecraft().fontRenderer;
//    private PrefsSlider slider;
//
//    private final Minecraft client;
//    private final int width, height, top, bottom, right, left, barLeft, border = 4, barWidth = 6;
//
//    public PrefsScrollPanel(Minecraft client, int width, int height, int top, int left, ArrayList<PrefsEntry> prefsEntries )
//    {
//        super(client, width, height, top, left);
//        this.prefsEntries = prefsEntries;
//
//        this.client = client;
//        this.width = width;
//        this.height = height;
//        this.top = top;
//        this.left = left;
//        this.bottom = height + this.top;
//        this.right = width + this.left;
//        this.barLeft = this.left + this.width - barWidth;
//    }
//
//    @Override
//    protected int getContentHeight()
//    {
//        int height = -4;
//
//        for( PrefsEntry a : prefsEntries )
//        {
//            height += a.getHeight() + 2;
//        }
//
//        return height;
//    }
//
//    @Override
//    protected void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
//    {
//    }
//
////    @Override
////    public boolean mouseScrolled(int mouseX, int mouseY, double scroll)
////    {
////        return super.mouseScrolled(mouseX, mouseY, scroll);
////    }
//
//    public int getScroll()
//    {
//        return (int) this.scrollDistance;
//    }
//
//    public void setScroll( int scroll )
//    {
//        this.scrollDistance = scroll;
//    }
//
//    public int getTop()
//    {
//        return this.top;
//    }
//
//    public int getBottom()
//    {
//        return this.bottom;
//    }
//
//    public int getRelativeY()
//    {
//        return this.top + this.border - (int) this.scrollDistance;
//    }
//
//    @Override
//    public void drawScreen(int mouseX, int mouseY, float partialTicks)
//    {
////        this.drawBackground();
//
////        if (Minecraft.getMinecraft().world != null)
////        {
////            this.drawGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), -1072689136, -804253680);
////            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(super ) );
////        }
//
//        Tessellator tess = Tessellator.getMinecraft();
//        BufferBuilder worldr = tess.getBuffer();
//
//        double scale = client.getMainWindow().getGuiScaleFactor();
//        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        GL11.glScissor((int)(left  * scale), (int)(client.getMainWindow().getFramebufferHeight() - (bottom * scale)),
//                (int)(width * scale), (int)(height * scale));
//
////        if (this.client.world != null)
////        {
////            this.drawGradientRect(this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
////        }
////        else // Draw dark dirt background
////        {
////            GlStateManager.disableLighting();
////            GlStateManager.disableFog();
////            this.client.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
////            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
////            final double texScale = 32.0F;
////            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
////            worldr.pos(this.left,  this.bottom, 0.0D).tex(this.left  / texScale, (this.bottom + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
////            worldr.pos(this.right, this.bottom, 0.0D).tex(this.right / texScale, (this.bottom + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
////            worldr.pos(this.right, this.top,    0.0D).tex(this.right / texScale, (this.top    + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
////            worldr.pos(this.left,  this.top,    0.0D).tex(this.left  / texScale, (this.top    + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
////            tess.draw();
////        }
//
//        int baseY = this.top + border - (int)this.scrollDistance;
//        this.drawPanel(right, baseY, tess, mouseX, mouseY);
//
//        GlStateManager.disableDepthTest();
//
//        int extraHeight = (this.getContentHeight() + border) - height;
//        if (extraHeight > 0)
//        {
//            int barHeight = getBarHeight();
//
//            int barTop = (int)this.scrollDistance * (height - barHeight) / extraHeight + this.top;
//            if (barTop < this.top)
//            {
//                barTop = this.top;
//            }
//
//            GlStateManager.disableTexture();
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(barLeft,            this.bottom, 0.0D).tex(0.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//            worldr.pos(barLeft + barWidth, this.bottom, 0.0D).tex(1.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//            worldr.pos(barLeft + barWidth, this.top,    0.0D).tex(1.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//            worldr.pos(barLeft,            this.top,    0.0D).tex(0.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//            tess.draw();
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(barLeft,            barTop + barHeight, 0.0D).tex(0.0F, 1.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            worldr.pos(barLeft + barWidth, barTop + barHeight, 0.0D).tex(1.0F, 1.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            worldr.pos(barLeft + barWidth, barTop,             0.0D).tex(1.0F, 0.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            worldr.pos(barLeft,            barTop,             0.0D).tex(0.0F, 0.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            tess.draw();
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(barLeft,                barTop + barHeight - 1, 0.0D).tex(0.0F, 1.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            worldr.pos(barLeft + barWidth - 1, barTop + barHeight - 1, 0.0D).tex(1.0F, 1.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            worldr.pos(barLeft + barWidth - 1, barTop,                 0.0D).tex(1.0F, 0.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            worldr.pos(barLeft,                barTop,                 0.0D).tex(0.0F, 0.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            tess.draw();
//        }
//
//        GlStateManager.enableTexture();
//        GlStateManager.shadeModel(GL11.GL_FLAT);
//        GlStateManager.enableAlphaTest();
//        GlStateManager.disableBlend();
//        GL11.glDisable(GL11.GL_SCISSOR_TEST);
//    }
//
//    private int getBarHeight()
//    {
//        int barHeight = (height * height) / this.getContentHeight();
//
//        if (barHeight < 32) barHeight = 32;
//
//        if (barHeight > height - border*2)
//            barHeight = height - border*2;
//
//        return barHeight;
//    }
//}