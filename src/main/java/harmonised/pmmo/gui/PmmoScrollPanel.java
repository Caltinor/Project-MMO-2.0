//package harmonised.pmmo.gui;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiScreen;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.client.resources.I18n;
//import net.minecraftforge.fml.client.GuiModList;
//import org.lwjgl.opengl.GL11;
//
//public class PmmoScrollPanel extends GuiModList
//{
//    public Minecraft mc = Minecraft.getMinecraft();
//    public final int top, left, bottom, right, barLeft, border = 4, barWidth = 6;
//
//    public PmmoScrollPanel( GuiScreen mainMenu, int width, int height, int top, int left)
//    {
//        super( mainMenu );
//        this.width = width;
//        this.height = height;
//        this.top = top;
//        this.left = left;
//        this.bottom = height + this.top;
//        this.right = width + this.left;
//        this.barLeft = this.left + this.width - barWidth;
//    }
//
//    public int getContentHeight()
//    {
//        int height = 48;
//
//        for( int i = 0; i < buttons.size(); i += 3 )
//        {
//            height += 92;
//        }
//
//        return height;
//    }
//
//    @Override
//    public void drawScreen(int mouseX, int mouseY, float partialTicks)
//    {
//        Tessellator tess = Tessellator.getInstance();
//        BufferBuilder worldr = tess.getBuffer();
//
//        double scale = mc.getMainWindow().getGuiScaleFactor();
//        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        GL11.glScissor((int)(left  * scale), (int)(client.getMainWindow().getFramebufferHeight() - (bottom * scale)),
//                (int)(width * scale), (int)(height * scale));
//
//        int baseY = this.top + border - (int)this.scrollDistance;
//        this.drawPanel(right, baseY, tess, mouseX, mouseY);
//
//        RenderSystem.disableDepthTest();
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
//            RenderSystem.disableTexture();
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
//        RenderSystem.enableTexture();
//        RenderSystem.shadeModel(GL11.GL_FLAT);
//        RenderSystem.enableAlphaTest();
//        RenderSystem.disableBlend();
//        GL11.glDisable(GL11.GL_SCISSOR_TEST);
//    }
//}
