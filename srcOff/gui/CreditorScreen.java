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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.*;

public class CreditorScreen extends GuiScreen
{
    public static final HashMap<String, String> uuidName = new HashMap<>();
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    public static final Map<String, List<String>> creditorsInfo = new HashMap<>();
    public static Map<String, Integer> colors = new HashMap<>();
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getMinecraft();
    ScaledResolution sr = new ScaledResolution( mc );
    FontRenderer font = minecraft.fontRenderer;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<TileButton> tileButtons;
    //    private UUID uuid;
    public String playerName;
    private int lastScroll;
    private int color;
    private String transKey;

    public CreditorScreen( String playerName, String transKey, int lastScroll )
    {
        super( new TextComponentTranslation( transKey ) );
//        this.uuid = uuid;
        this.playerName = playerName;
        this.lastScroll = lastScroll;
        this.transKey = transKey;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        tileButtons = new ArrayList<>();

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new CreditsScreen( Minecraft.getMinecraft().player.getUniqueID(), new TextComponentTranslation( "pmmo.credits" ), JType.CREDITS ) );
        });

        addButton(exitButton);

//        for( TileButton button : tileButtons )
//        {
//            addButton( button );
//        }
    }

    @Override
    public void render( int mouseX, int mouseY, double partialTicks)
    {
        renderBackground( 1 );
        super.render( mouseX, mouseY, partialTicks );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        color = 0xffffff;

        if( colors.containsKey( playerName ) )
            color = colors.get( playerName );

        drawCenteredString( font, "§l" + playerName, sr.getScaledWidth() / 2, y - ( font.getStringWidth( playerName ) > 220 ? 10 : 5), color );

        List<String> list = creditorsInfo.get( playerName );

        if( list == null )
            drawCenteredString( font, "§lError! Please Report me! \"" + playerName + "\"", sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, color );
        else
        {
            for( int i = 0; i < list.size(); i++ )
            {
                drawCenteredString( font, ( list.get(i).contains( "§l" ) ? "" : "§l" ) + list.get(i), sr.getScaledWidth() / 2, ( sr.getScaledHeight() / 2 - ( list.size() * 20 ) / 2 ) + i * 20, color );
            }
        }

        GlStateManager.enableBlend();
    }

    @Override
    public void renderBackground( int p_renderBackground_1_ )
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
        this.blit( x, y, 0, 0, boxWidth, boxHeight );
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

    public static void initCreditors()
    {
        uuidName.put( "e4c7e475-c1ff-4f94-956c-ac5be02ce04a", "Lucifer#0666" );
        uuidName.put( "8eb0578d-c113-49d3-abf6-a6d36f6d1116", "Tyrius#0842" );
        uuidName.put( "2ea5efa1-756b-4c9e-9605-7f53830d6cfa", "didis54#5815" );
        uuidName.put( "0bc51f06-9906-41ea-9fb4-7e9be169c980", "stressindicator#8819" );
        uuidName.put( "5bfdb948-7b66-476a-aefe-d45e4778fb2d", "Daddy_P1G#0432" );
        uuidName.put( "554b53b8-d0fa-409e-ab87-2a34bf83e506", "joerkig#1337" );
        uuidName.put( "21bb554a-f339-48ef-80f7-9a5083172892", "Judicius#1036" );
        List<String> list;

        /////////LAPIS//////////////
        PlayerConnectedHandler.lapisPatreons.forEach( a ->
        {
            colors.put( uuidName.get( a.toString() ), 0x5555ff );
            creditorsInfo.put( uuidName.get( a.toString() ), new ArrayList<>());
        });
        //LUCIFER
        list = creditorsInfo.get( "Lucifer#0666" );
        list.add( "First Lapis Tier Patreon" );
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "28/04/2020" ).getUnformattedText() );

        /////////DANDELION//////////
        //TYRIUS
        PlayerConnectedHandler.dandelionPatreons.forEach( a ->
        {
            colors.put( uuidName.get( a.toString() ), 0xffff33 );
            creditorsInfo.put( uuidName.get( a.toString() ), new ArrayList<>());
        });
        list = creditorsInfo.get( "Tyrius#0842" );
        list.add( "First Dandelion Tier Patreon" );
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "19/03/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.creatorOfModpack", "The Cosmic Tree" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.helpedFillingInModValues", "Botania" ).getUnformattedText() );

        list = creditorsInfo.get( "joerkig#1337" );
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "3/11/2020" ).getUnformattedText() );

        list = creditorsInfo.get( "Judicius#1036" );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "22/11/2020" ).getString() );

        /////////IRON///////////////
        PlayerConnectedHandler.ironPatreons.forEach( a ->
        {
            colors.put( uuidName.get( a.toString() ), 0xeeeeee );
            creditorsInfo.put( uuidName.get( a.toString() ), new ArrayList<>());
        } );
        //DIDIS54
        list = creditorsInfo.get( "didis54#5815" );
        list.add( "First Iron Tier Patreon" );
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "11/04/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.creatorOfModpack", "Anarkhe Revolution" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.helpedTranslating", "French" ).getUnformattedText() );

        //STRESSINDICATOR
        list = creditorsInfo.get( "stressindicator#8819" );
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "17/08/2020" ).getUnformattedText() );

        //DADDY_P1G
        list = creditorsInfo.get( "Daddy_P1G#0432" );
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "29/06/2020" ).getUnformattedText() );

        //NEOTHIAMIN
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "17/04/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.creatorOfModpack", "Skillful Survival" ).getUnformattedText() );
        creditorsInfo.put( "neothiamin#1798", list );
        //DARTH_REVAN#7341
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "17/04/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.creatorOfModpack", "Zombie Textiles" ).getUnformattedText() );
        creditorsInfo.put( "Darth Revan#7341", list );

        /////////TRANSLATOR/////////
        //BusanDaek#3970
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "31/03/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.translated", "Korean" ).getUnformattedText() );
        creditorsInfo.put( "BusanDaek#3970", list );
        //deezer911#5693
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "11/03/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.helpedTranslating", "French" ).getUnformattedText() );
        creditorsInfo.put( "deezer911#5693", list );
        //TorukM4kt00#0246
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "13/05/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.translated", "Portuguese - Brazil" ).getUnformattedText() );
        creditorsInfo.put( "TorukM4kt00#0246", list );
        //Dawnless#1153
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "22/08/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.translated", "Dutch - Netherlands" ).getUnformattedText() );
        creditorsInfo.put( "Dawnless#1153", list );
        //TorukM4kt00#0246
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "13/05/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.translated", "Portuguese - Brazil" ).getUnformattedText() );
        creditorsInfo.put( "TorukM4kt00#0246", list );
        //starche#7569
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "24/07/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.translated", "Russian" ).getUnformattedText() );
        creditorsInfo.put( "starche#7569", list );
        //Lyla#2639
        list = new ArrayList<>();
        list.add( new TextComponentTranslation( "pmmo.discordMemberSince", "28/10/2020" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.translated", "Chinese Traditional" ).getUnformattedText() );
        list.add( new TextComponentTranslation( "pmmo.translated", "Chinese Simplified" ).getUnformattedText() );
        creditorsInfo.put( "Lyla#2639", list );
        //Matterfall#1952
        list = new ArrayList<>();
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "22/11/2020" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.translated", "German" ).getString() );
        creditorsInfo.put( "Matterfall#1952", list );
        //N1co#9248
        list = new ArrayList<>();
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "25/11/2020" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.translated", "Spanish" ).getString() );
        creditorsInfo.put( "N1co#9248", list );
    }
}