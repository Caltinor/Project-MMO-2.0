package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.gui.ScrollPanel;

import java.util.List;

public class ScreenSkills extends Screen
{
    protected final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation bar = new ResourceLocation( Reference.MOD_ID, "textures/gui/screenboxy.png" );

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int boxPosX;
    private int boxPosY;
    private MyScrollPanel myList;

    public ScreenSkills(ITextComponent titleIn)
    {
        super(titleIn);
    }

    @Override
    protected void init()
    {
        super.init();

        children.add( myList = new MyScrollPanel(minecraft, boxPosX - 24, boxPosY - 40, 20, 12));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft.world != null)
        {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        else
            this.renderDirtBackground(p_renderBackground_1_);

        boxHeight = 250;
        boxWidth = 250;

        boxPosX = (int) ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        boxPosY = (int) ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );
        Minecraft.getInstance().getTextureManager().bindTexture( bar );

        this.blit( boxPosX, boxPosY, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

}
