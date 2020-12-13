package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.*;

public class PrefsChoiceScreen extends GuiScreen
{
    private final List<GuiButton> buttons = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static TileButton exitButton;

    ScaledResolution sr = new ScaledResolution( Minecraft.getMinecraft() );
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<TileButton> tileButtons;

    public PrefsChoiceScreen( ITextComponent titleIn )
    {
        super();
    }

    @Override
    public void initGui()
    {
        sr = new ScaledResolution( mc );
        tileButtons = new ArrayList<>();

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton( 1337, x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new MainScreen( Minecraft.getMinecraft().player.getUniqueID(), new TextComponentString( "pmmo.potato" ) ) );
        });

        TileButton settingsButton = new TileButton( 1337,  (int) ( x + 24 + 36 * 1.5 ), (int) ( y + 24 + 36 * 2.5 ), 3, 7, "pmmo.settings", JType.SETTINGS, (button) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new PrefsScreen( new TextComponentTranslation( ((TileButton) button).transKey ), JType.SETTINGS ) );
        });

        TileButton guiSettingsButton = new TileButton( 1337,  (int) ( x + 24 + 36 * 3.5 ), (int) ( y + 24 + 36 * 2.5 ), 3, 7, "pmmo.guiSettings", JType.GUI_SETTINGS, (button) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new PrefsScreen( new TextComponentTranslation( ((TileButton) button).transKey ), JType.GUI_SETTINGS ) );
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground( 1 );
        super.drawScreen(mouseX, mouseY, partialTicks);

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

//        fillGradient(x + 20, y + 52, x + 232, y + 164, 0x22444444, 0x33222222);

        GlStateManager.pushAttrib();
        for( TileButton button : tileButtons )
        {
            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
                drawHoveringText( new TextComponentTranslation( button.transKey ).getFormattedText(), mouseX, mouseY );
        }
        GlStateManager.popAttrib();
    }

    @Override
    public void drawBackground(int p_renderBackground_1_)
    {
        if (this.mc != null)
        {
            this.drawGradientRect(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        else
            this.drawBackground(p_renderBackground_1_);


        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getMinecraft().getTextureManager().bindTexture( box );
        GlStateManager.disableBlend();
        this.drawTexturedModalRect( x, y, 0, 0,  boxWidth, boxHeight );
    }

//    @Override
//    public boolean mouseScrolled(int mouseX, int mouseY, double scroll)
//    {
//        return super.mouseScrolled(mouseX, mouseY, scroll);
//    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException
    {
        if( button == 1 )
            exitButton.onPress();
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    protected void mouseClickMove( int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick )
{
        super.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
    }

}
