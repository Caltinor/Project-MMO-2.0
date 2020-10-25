package harmonised.pmmo.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ListScrollPanel extends ScrollPanel
{
    MainWindow sr = Minecraft.getInstance().getMainWindow();
    PlayerEntity player;
    String regKey;
    JType jType;
    private final int boxWidth = 256;
    private final int boxHeight = 256;
    private final ArrayList<ListButton> buttons;

    private final Minecraft client;
    private final int width, height, top, bottom, right, left, barLeft, border = 4, barWidth = 6;
    int accumulativeHeight;

    public ListScrollPanel(Minecraft client, int width, int height, int top, int left, JType jType, PlayerEntity player, ArrayList<ListButton> buttons )
    {
        super(client, width, height, top, left);
        this.player = player;
        this.jType = jType;
        this.buttons = buttons;

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
        int height = 0;

        for( ListButton a : buttons )
        {
            height += a.getHeightRealms() + 4;
        }

        return height;
    }

    @Override
    protected void drawPanel( MatrixStack stack, int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
    {
        accumulativeHeight = 0;
        for( ListButton button : buttons )
        {
            button.x = this.right - button.getWidth() - 8;
            button.y = relativeY + accumulativeHeight;

            if( button.y + button.getHeightRealms() + 2 > this.top && button.y - 2 < this.bottom )
            {
                if( button.unlocked )
                    fillGradient( stack, this.left + 4, button.y - 2, this.right - 2, button.y + button.getHeightRealms() + 2, 0x22444444, 0x33222222);
                else
                    fillGradient( stack, this.left + 4, button.y - 2, this.right - 2, button.y + button.getHeightRealms() + 2, 0xaa444444, 0xaa222222);

                button.render( stack,  mouseX, mouseY, 0 );

                drawString( stack, Minecraft.getInstance().fontRenderer, button.title, this.left + 6, button.y + 2, button.unlocked ? 0x54fc54 : 0xfc5454 );

                int i = 0;
                for( ITextComponent line : button.text )
                {
                    drawString( stack, Minecraft.getInstance().fontRenderer, line.getString(), this.left + 6, button.y + 11 + (i++ * 9), line.getStyle().getColor() == null ? 0xffffff : line.getStyle().getColor().getColor() );
                }
            }
            accumulativeHeight += button.getHeightRealms() + 4;
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

    private int getMaxScroll()
    {
        return this.getContentHeight() - (this.height - this.border);
    }

    @Override
    public void render( MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
//        this.drawBackground();

//        if (Minecraft.getInstance().world != null)
//        {
//            this.fillGradient( stack, 0, 0, sr.getScaledWidth(), sr.getScaledHeight(), -1072689136, -804253680);
//            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(super ) );
//        }

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldr = tess.getBuffer();

        double scale = client.getMainWindow().getGuiScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left  * scale), (int)(client.getMainWindow().getFramebufferHeight() - (bottom * scale)),
                (int)(width * scale), (int)(height * scale));

//        if (this.client.world != null)
//        {
//            this.drawGradientRect(this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
//        }
//        else // Draw dark dirt background
//        {
//            RenderSystem.disableLighting();
//            RenderSystem.disableFog();
//            this.client.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
//            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//            final float texScale = 32.0F;
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(this.left,  this.bottom, 0.0D).tex(this.left  / texScale, (this.bottom + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.right, this.bottom, 0.0D).tex(this.right / texScale, (this.bottom + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.right, this.top,    0.0D).tex(this.right / texScale, (this.top    + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.left,  this.top,    0.0D).tex(this.left  / texScale, (this.top    + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            tess.draw();
//        }

        int baseY = this.top + border - (int)this.scrollDistance;
        this.drawPanel( stack, right, baseY, tess, mouseX, mouseY);

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
