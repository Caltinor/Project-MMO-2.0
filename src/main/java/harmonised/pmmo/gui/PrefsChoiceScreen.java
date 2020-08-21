package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class PrefsChoiceScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    MainWindow sr = minecraft.getWindow();
    FontRenderer font = minecraft.fontRenderer;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<TileButton> tileButtons;

    public PrefsChoiceScreen( ITextComponent titleIn )
    {
        super(titleIn);
    }

    @Override
    protected void init()
    {
        tileButtons = new ArrayList<>();

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new MainScreen( Minecraft.getInstance().player.getUniqueID(), new TranslationTextComponent( "pmmo.potato" ) ) );
        });

        TileButton settingsButton = new TileButton( (int) ( x + 24 + 36 * 1.5 ), (int) ( y + 24 + 36 * 2.5 ), 3, 7, "pmmo.settings", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new PrefsScreen( new TranslationTextComponent( ((TileButton) button).transKey ), JType.SETTINGS ) );
        });

        TileButton guiSettingsButton = new TileButton( (int) ( x + 24 + 36 * 3.5 ), (int) ( y + 24 + 36 * 2.5 ), 3, 7, "pmmo.guiSettings", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new PrefsScreen( new TranslationTextComponent( ((TileButton) button).transKey ), JType.GUI_SETTINGS ) );
        });

        addButton( exitButton );
        tileButtons.add( settingsButton );
        tileButtons.add( guiSettingsButton );

        for( TileButton button : tileButtons )
        {
            addButton( button );
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );
        super.render( stack, mouseX, mouseY, partialTicks );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

//        fillGradient( stack, x + 20, y + 52, x + 232, y + 164, 0x22444444, 0x33222222);

        for( TileButton button : tileButtons )
        {
            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
                renderTooltip( stack, new TranslationTextComponent( button.transKey ), mouseX, mouseY );
        }
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this, stack ));
        }
        else
            this.renderBackground( stack, p_renderBackground_1_ );


        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );
        RenderSystem.disableBlend();
        this.drawTexture( stack,  x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if( button == 1 )
        {
            exitButton.onPress();
            return true;
        }
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
