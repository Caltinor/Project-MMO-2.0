package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.*;

public class MainScreen extends GuiScreen
{
    private final List<GuiButton> buttons = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final ResourceLocation logo = XP.getResLoc( Reference.MOD_ID, "textures/gui/logo.png" );
    private static TileButton exitButton;
    public static Map<JType, Integer> scrollAmounts = new HashMap<>();

    ScaledResolution sr = new ScaledResolution( Minecraft.getMinecraft() );
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<TileButton> tileButtons;
    private UUID uuid;

    public MainScreen( UUID uuid, ITextComponent titleIn )
    {
        super();
        this.uuid = uuid;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    public void initGui()
    {
        tileButtons = new ArrayList<>();

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton( 1337, x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getMinecraft().player.closeScreen();
        });

        TileButton glossaryButton = new TileButton( 1337, 0, y + 24 + 36 * 4, 3, 5, "pmmo.glossary", JType.NONE, (button) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new GlossaryScreen( uuid, new TextComponentTranslation( ((TileButton) button).transKey ), true ) );
        });

        TileButton creditsButton = new TileButton( 1337,  0, y + 24 + 36 * 4, 3, 4, "pmmo.credits", JType.NONE, (button) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditsScreen( uuid, new TextComponentTranslation( ((TileButton) button).transKey ), JType.CREDITS ) );
        });

        TileButton prefsButton = new TileButton( 1337,  0, y + 24 + 36 * 4, 3, 7, "pmmo.preferences", JType.NONE, (button) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new PrefsChoiceScreen( new TextComponentTranslation( ((TileButton) button).transKey ) ) );
        });

        TileButton skillsButton = new TileButton( 1337,  0, y + 24 + 36 * 4, 3, 6, "pmmo.skills", JType.NONE, (button) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new ListScreen( uuid,  new TextComponentTranslation( ((TileButton) button).transKey ), "", JType.SKILLS, Minecraft.getMinecraft().player ) );
        });



        addButton(exitButton);
        tileButtons.add(glossaryButton);
        tileButtons.add(creditsButton);
//        tileButtons.add(prefsButton);
        tileButtons.add(skillsButton);
        //COUT

        for( int i = 0; i < tileButtons.size(); i++ )
        {
            TileButton button = tileButtons.get( i );
            button.x = sr.getScaledWidth()/2 + i*( button.getButtonWidth()+2 ) - tileButtons.size()*(button.getButtonWidth()+2)/2;
            addButton( button );
        }
    }

    @Override
    public void drawScreen( int mouseX, int mouseY, float partialTicks )
    {
        drawBackground( 1 );
        super.drawScreen( mouseX, mouseY, partialTicks );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        drawGradientRect(x + 20, y + 52, x + 232, y + 164, 0x22444444, 0x33222222);

        GlStateManager.pushAttrib();
        for( TileButton button : tileButtons )
        {
            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
                drawHoveringText( new TextComponentTranslation( button.transKey ).getFormattedText(), mouseX, mouseY );
        }
        GlStateManager.popAttrib();
        GlStateManager.enableBlend();

        Minecraft.getMinecraft().getTextureManager().bindTexture( logo );
        this.drawTexturedModalRect( sr.getScaledWidth() / 2 - 100, sr.getScaledHeight() / 2 - 80, 0, 0,  200, 60 );
    }

    @Override
    public void drawBackground(int p_drawBackground_1_)
    {
        if ( this.mc != null )
        {
            this.drawGradientRect(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        else
            this.drawBackground(p_drawBackground_1_);


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
    public void mouseClicked( int mouseX, int mouseY, int button) throws IOException
    {
        if( button == 1 )
            exitButton.onPress();
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased( int mouseX, int mouseY, int button)
    {
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove( int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick )
    {
        super.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
    }
}
