package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;

import java.util.List;

public class ScreenSkills extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation bar = new ResourceLocation( Reference.MOD_ID, "textures/gui/screenboxy.png" );

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int boxPosX;
    private int boxPosY;
    private MyScrollPanel myList;
    private Button button;

    public ScreenSkills(ITextComponent titleIn)
    {
        super(titleIn);
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
//        super.init();

//        children.add( myList = new MyScrollPanel(minecraft, boxPosX - 24, boxPosY - 40, 20, 12));

        button = new Button( (sr.getScaledWidth() / 2), (sr.getScaledHeight() / 2), 0, 0, I18n.format("pmmo.text.button"), (something) ->
        {
            System.out.println( "click" );
        });
        buttons.add( button );
        children.add( button );
//        addButton( button );
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );

        super.render(mouseX, mouseY, partialTicks);

//        this.renderTooltip( new ItemStack(Items.DIAMOND_AXE ), mouseX, mouseY );
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        else
            this.renderDirtBackground(p_renderBackground_1_);

        boxHeight = 256;
        boxWidth = 256;

        boxPosX = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        boxPosY = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );
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
