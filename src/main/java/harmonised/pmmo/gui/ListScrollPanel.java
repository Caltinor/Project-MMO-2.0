package harmonised.pmmo.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
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
            height += a.getHeight() + 4;
        }

        return height;
    }

    @Override
    protected void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY)
    {
        accumulativeHeight = 0;
        for( int i = 0; i < buttons.size(); i++ )
        {
            ListButton button = buttons.get( i );
            button.x = this.right - button.getWidth() - 8;
            button.y = relativeY + accumulativeHeight;

            if( button.y + button.getHeight() + 2 > this.top && button.y - 2 < this.bottom )
            {
                if( button.unlocked )
                    fillGradient(this.left + 4, button.y - 2, this.right - 2, button.y + button.getHeight() + 2, 0x22444444, 0x33222222);
                else
                    fillGradient(this.left + 4, button.y - 2, this.right - 2, button.y + button.getHeight() + 2, 0xaa444444, 0xaa222222);

                button.render( mouseX, mouseY, 0 );

                int color = button.unlocked ? 0x54fc54 : 0xfc5454;

                if( jType.equals( JType.SKILLS ) )
                {
                    color = Skill.getSkillColor( button.regKey );
                    if( color == 0xffffff )
                        color = 0x54fc54;
                }

                if( jType.equals( JType.SKILLS ) || jType.equals( JType.HISCORE ) )
                    drawString( Minecraft.getInstance().fontRenderer, (i+1) + ".", this.left + 178, button.y + 2, color );

                drawString( Minecraft.getInstance().fontRenderer, button.title, this.left + 6, button.y + 2, color );

                int j = 0;
                for( String line : button.text )
                {
                    drawString( Minecraft.getInstance().fontRenderer, line, this.left + 6, button.y + 11 + (j++ * 9), 0xffffff );
                }
            }
            accumulativeHeight += button.getHeight() + 4;
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
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldr = tess.getBuffer();

        double scale = client.getMainWindow().getGuiScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left  * scale), (int)(client.getMainWindow().getFramebufferHeight() - (bottom * scale)),
                (int)(width * scale), (int)(height * scale));

        int baseY = this.top + border - (int)this.scrollDistance;
        this.drawPanel(right, baseY, tess, mouseX, mouseY);

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
