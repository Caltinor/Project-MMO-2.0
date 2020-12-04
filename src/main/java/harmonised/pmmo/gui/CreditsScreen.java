package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.*;

public class CreditsScreen extends GuiScreen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final ResourceLocation logo = XP.getResLoc( Reference.MOD_ID, "textures/gui/logo.png" );
    private static boolean firstTime = true;
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getMinecraft();
    ScaledResolution sr = new ScaledResolution( minecraft );
    FontRenderer font = minecraft.fontRenderer;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private int scrollX, scrollY;
    private JType jType;
    private UUID uuid;
    private ArrayList<ListButtonBig> listButtons;
    private CreditsScrollPanel scrollPanel;

    public CreditsScreen( UUID uuid, ITextComponent titleIn, JType jType )
    {
        super(titleIn);
        this.uuid = uuid;
        this.jType = jType;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        listButtons = new ArrayList<>();

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );
        scrollX = x + 16;
        scrollY = y + 10;

        if( firstTime )
        {
            CreditorScreen.initCreditors();
            firstTime = false;
        }

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new MainScreen( uuid, new TextComponentTranslation( "pmmo.stats" ) ) );
        });

        PlayerConnectedHandler.lapisPatreons.forEach( a ->
        {
            listButtons.add( new ListButtonBig( 0, 0, 1, 2, "", CreditorScreen.uuidName.get( a.toString() ), new TextComponentTranslation( "pmmo.lapisPatreon" ).setStyle( XP.textStyle.get( "blue" ) ).getUnformattedText(), button ->
            {
                Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
            }));
        });

        PlayerConnectedHandler.dandelionPatreons.forEach( a ->
        {
            listButtons.add( new ListButtonBig( 0, 0, 1, 3, "", CreditorScreen.uuidName.get( a.toString() ), new TextComponentTranslation( "pmmo.dandelionPatreon" ).setStyle( XP.textStyle.get( "yellow" ) ).getUnformattedText(), button ->
            {
                Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
            }));
        });

        PlayerConnectedHandler.ironPatreons.forEach( a ->
        {
            listButtons.add( new ListButtonBig( 0, 0, 1, 4, "", CreditorScreen.uuidName.get( a.toString() ), new TextComponentTranslation( "pmmo.ironPatreon" ).setStyle( XP.textStyle.get( "grey" ) ).getUnformattedText(), button ->
            {
                Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
            }));
        });

        //TRANSLATOR

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "ko_kr", "BusanDaek#3970", new TextComponentTranslation( "pmmo.translated", "Korean" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "fr_fr", "deezer911#5693", new TextComponentTranslation( "pmmo.helpedTranslating", "French" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "fr_fr", "didis54#5815", new TextComponentTranslation( "pmmo.helpedTranslating", "French" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "pt_br", "TorukM4kt00#0246", new TextComponentTranslation( "pmmo.translated", "Portuguese - Brazil" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "ru_ru", "starche#7569", new TextComponentTranslation( "pmmo.translated", "Portuguese - Brazil" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "nl_nl", "Dawnless#1153", new TextComponentTranslation( "pmmo.translated", "Dutch" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "zh_tw", "Lyla#2639", new TextComponentTranslation( "pmmo.translated", "Chinese Traditional" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "zh_cn", "Lyla#2639", new TextComponentTranslation( "pmmo.translated", "Chinese Simplified" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "de_de", "Matterfall#1952", new TranslationTextComponent( "pmmo.translated", "Chinese Simplified" ).getString(), button ->
        {
            Minecraft.getInstance().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "es_ar", "N1co#9248", new TranslationTextComponent( "pmmo.translated", "Chinese Simplified" ).getString(), button ->
        {
            Minecraft.getInstance().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        //MODPACK

        listButtons.add( new ListButtonBig( 0, 0, 1, 5, "", "Tyrius#0842", new TextComponentTranslation( "pmmo.creatorOfModpack", "The Cosmic Tree" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 5, "", "didis54#5815", new TextComponentTranslation( "pmmo.creatorOfModpack", "Anarkhe Revolution" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 5, "", "neothiamin#1798", new TextComponentTranslation( "pmmo.creatorOfModpack", "Skillful Survival" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 5, "", "Darth Revan#7341", new TextComponentTranslation( "pmmo.creatorOfModpack", "Zombie Textiles" ).getUnformattedText(), button ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        addButton(exitButton);

//        listButtons.sort( Comparator.comparingInt( a -> ((ListButtonBig) a).elementTwo ) );

        scrollPanel = new CreditsScrollPanel( Minecraft.getMinecraft(), boxWidth - 40, boxHeight - 21, scrollY, scrollX, JType.CREDITS, listButtons );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.setTag( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
    }

    @Override
    public void render( int mouseX, int mouseY, double partialTicks)
    {
        renderBackground( 1 );
        super.render( mouseX, mouseY, partialTicks );
        scrollPanel.render( mouseX,mouseY,partialTicks );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        for( ListButtonBig button : listButtons )
        {
            if( mouseX > button.x + 3 && mouseY > button.y && mouseX < button.x + 60 && mouseY < button.y + 64 )
            {
                renderTooltip( new TextComponentString( button.playerName ).getUnformattedText(), mouseX, mouseY );
                break;
            }
        }

        if( font.getStringWidth( title.getUnformattedText() ) > 220 )
            drawCenteredString( font, title.getUnformattedText(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( font, title.getUnformattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
    }

    @Override
    public void renderBackground( int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient( 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this ));
        }
        else
            this.renderBackground( p_renderBackground_1_ );


        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getMinecraft().getTextureManager().bindTexture( box );
        GlStateManager.disableBlend();
        this.drawTexturedModalRect( x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        if( listButtons.size() >= 7 )
            scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
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

        for( ListButtonBig a : listButtons )
        {
            if( mouseX > a.x + 3 && mouseY > a.y && mouseX < a.x + 60 && mouseY < a.y + 64 )
                a.onPress();
        }

        scrollPanel.mouseClicked( mouseX, mouseY, button );
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased( mouseX, mouseY, button );
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

}
