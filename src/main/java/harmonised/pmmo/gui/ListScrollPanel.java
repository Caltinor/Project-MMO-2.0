package harmonised.pmmo.gui;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ScrollPanel;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ListScrollPanel extends ScrollPanel
{
    Window sr = Minecraft.getInstance().getWindow();
    Player player;
    String regKey;
    JType jType;
    private final int boxWidth = 256;
    private final int boxHeight = 256;
    private ArrayList<ListButton> buttons;

    private final Minecraft client;
    private final int width, height, top, bottom, right, left, barLeft, border = 4, barWidth = 6;
    int accumulativeHeight;

    public ListScrollPanel(Minecraft client, int width, int height, int top, int left, JType jType, Player player, ArrayList<ListButton> buttons)
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

    public void setButtons(ArrayList<ListButton> buttons)
    {
        this.buttons = buttons;
    }

    @Override
    protected int getContentHeight()
    {
        int height = 0;

        for(ListButton a : buttons)
        {
            height += a.getHeight() + 4;
        }

        return height;
    }

    @Override
    protected void drawPanel(PoseStack stack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY)
    {
        accumulativeHeight = 0;
        for(int i = 0; i < buttons.size(); i++)
        {
            ListButton button = buttons.get(i);
            button.x = this.right - button.getWidth() - 8;
            button.y = relativeY + accumulativeHeight;

            if(button.y + button.getHeight() + 2 > this.top && button.y - 2 < this.bottom)
            {
                if(button.unlocked)
                    fillGradient(stack, this.left + 4, button.y - 2, this.right - 2, button.y + button.getHeight() + 2, 0x22444444, 0x33222222);
                else
                    fillGradient(stack, this.left + 4, button.y - 2, this.right - 2, button.y + button.getHeight() + 2, 0xaa444444, 0xaa222222);

                button.render(stack,  mouseX, mouseY, 0);

                int color = button.unlocked ? 0x54fc54 : 0xfc5454;

                if(jType.equals(JType.SKILLS))
                {
                    color = Skill.getSkillColor(button.regKey);
                    if(color == 0xffffff)
                        color = 0x54fc54;
                }

                if(jType.equals(JType.HISCORE))
                    drawString(stack, Minecraft.getInstance().font, (i+1) + ".", this.left + 178, button.y + 2, color);
                else if(jType.equals(JType.SKILLS) && i > 0)
                    drawString(stack, Minecraft.getInstance().font, (i) + ".", this.left + 178, button.y + 2, color);

                drawString(stack, Minecraft.getInstance().font, button.title, this.left + 6, button.y + 2, color);

                int j = 0;
                for(Component line : button.text)
                {
                    drawString(stack, Minecraft.getInstance().font, line.getString(), this.left + 6, button.y + 11 + (j++ * 9), line.getStyle().getColor() == null ? 0xffffff : line.getStyle().getColor().getValue());
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

    private int getMaxScroll()
    {
        return this.getContentHeight() - (this.height - this.border);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
//        this.drawBackground();

//        if (Minecraft.getInstance().world != null)
//        {
//            this.fillGradient(stack, 0, 0, sr.getScaledWidth(), sr.getScaledHeight(), -1072689136, -804253680);
//            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundDrawnEvent(super));
//        }

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder worldr = tess.getBuilder();

        double scale = client.getWindow().getGuiScale();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left  * scale), (int)(client.getWindow().getHeight() - (bottom * scale)),
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
//            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
//            final float texScale = 32.0F;
//            worldr.begin(VertexFormat.Mode.QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(this.left,  this.bottom, 0.0D).tex(this.left  / texScale, (this.bottom + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.right, this.bottom, 0.0D).tex(this.right / texScale, (this.bottom + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.right, this.top,    0.0D).tex(this.right / texScale, (this.top    + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            worldr.pos(this.left,  this.top,    0.0D).tex(this.left  / texScale, (this.top    + (int)this.scrollDistance) / texScale).color(0x20, 0x20, 0x20, 0xFF).endVertex();
//            tess.draw();
//        }

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
//        RenderSystem.shadeModel(GL11.GL_FLAT);
//        RenderSystem.enableAlphaTest();
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
    public NarrationPriority narrationPriority() {
        return null;
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }
}
