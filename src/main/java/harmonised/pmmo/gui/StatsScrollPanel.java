package harmonised.pmmo.gui;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class StatsScrollPanel extends ScrollPanel
{
    Window sr;
    private final int boxWidth = 256;
    private final int boxHeight = 256;
    private final List<StatsEntry> statsEntries;
    private StatsEntry statsEntry;
    private Font font;

    private final Minecraft client;
    private final int width, height, top, bottom, right, left, barLeft, border = 4, barWidth = 6;

    public StatsScrollPanel(PoseStack stack, Minecraft client, int width, int height, int top, int left, List<StatsEntry> statsEntries)
    {
        super(client, width, height, top, left);
        this.statsEntries = statsEntries;
        this.sr = Minecraft.getInstance().getWindow();
        this.font = Minecraft.getInstance().font;

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
        int height = 16;

        for(StatsEntry a : statsEntries)
        {
            height += a.getHeight() + 3;
        }

        return height;
    }

    @Override
    protected void drawPanel(PoseStack stack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY)
    {
        int accumulativeHeight = 0;
        for(int i = 0; i < statsEntries.size(); i++)
        {
            statsEntry = statsEntries.get(i);
            statsEntry.setX(this.left + 6);
            statsEntry.setY(relativeY + accumulativeHeight);

            TextColor color = statsEntry.title.getStyle().getColor();
            int hexColor = color == null ? 0xffffff : color.getValue();

            fillGradient(stack, this.left + 4, statsEntry.getY() - 2, this.right - 2, statsEntry.getY() + statsEntry.getHeight() + 2, 0x22444444, 0x33222222);
            drawCenteredString(stack, font, statsEntry.title.getString(), sr.getGuiScaledWidth()/2, statsEntry.getY(), hexColor);
            for(int j = 0; j < statsEntry.text.size(); j++)
            {
                MutableComponent line = statsEntry.text.get(j);
                color = line.getStyle().getColor();
                hexColor = color == null ? 0xffffff : color.getValue();
                drawString(stack, font, line.getString(), statsEntry.getX(), 2 + statsEntry.getY() + (j+1)*11, hexColor);
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

    public void setScroll(int scroll)
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
    public void render(PoseStack stack,  int mouseX, int mouseY, float partialTicks)
    {
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder worldr = tess.getBuilder();

        double scale = client.getWindow().getGuiScale();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left  * scale), (int)(client.getWindow().getHeight() - (bottom * scale)),
                (int)(width * scale), (int)(height * scale));

        int baseY = this.top + border - (int)this.scrollDistance;
        this.drawPanel(stack, right, baseY, tess, mouseX, mouseY);

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
            worldr.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            worldr.vertex(barLeft,            this.bottom, 0.0D).uv(0.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.vertex(barLeft + barWidth, this.bottom, 0.0D).uv(1.0F, 1.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.vertex(barLeft + barWidth, this.top,    0.0D).uv(1.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.vertex(barLeft,            this.top,    0.0D).uv(0.0F, 0.0F).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.end();
            worldr.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            worldr.vertex(barLeft,            barTop + barHeight, 0.0D).uv(0.0F, 1.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.vertex(barLeft + barWidth, barTop + barHeight, 0.0D).uv(1.0F, 1.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.vertex(barLeft + barWidth, barTop,             0.0D).uv(1.0F, 0.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.vertex(barLeft,            barTop,             0.0D).uv(0.0F, 0.0F).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            tess.end();
            worldr.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            worldr.vertex(barLeft,                barTop + barHeight - 1, 0.0D).uv(0.0F, 1.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.vertex(barLeft + barWidth - 1, barTop + barHeight - 1, 0.0D).uv(1.0F, 1.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.vertex(barLeft + barWidth - 1, barTop,                 0.0D).uv(1.0F, 0.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.vertex(barLeft,                barTop,                 0.0D).uv(0.0F, 0.0F).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            tess.end();
        }

        RenderSystem.enableTexture();
        //COUT
//        RenderSystem.shadeModel(GL_FLAT);
        PmmoScreen.enableAlpha(1);
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

    @Override
    public NarrationPriority narrationPriority()
    {
        return null;
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_)
    {

    }
}