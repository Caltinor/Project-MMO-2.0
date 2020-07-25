package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class CreditorScreen extends Screen
{
    public static final HashMap<String, String> uuidName = new HashMap<>();
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    public static final Map<String, List<String>> creditorsInfo = new HashMap<>();
    public static Map<String, Integer> colors = new HashMap<>();
    private static TileButton exitButton;

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
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
        super( new TranslationTextComponent( transKey ) );
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

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new CreditsScreen( Minecraft.getInstance().player.getUniqueID(), new TranslationTextComponent( "pmmo.credits" ), JType.CREDITS ) );
        });

        addButton(exitButton);

//        for( TileButton button : tileButtons )
//        {
//            addButton( button );
//        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );
        super.render(mouseX, mouseY, partialTicks);

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        color = 0xffffff;

        if( colors.containsKey( playerName ) )
            color = colors.get( playerName );

//        if( font.getStringWidth( title.getString() ) > 220 )
//            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, color );
//        else
//            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, color );

        List<String> list = creditorsInfo.get( playerName );

        for( int i = 0; i < list.size(); i++ )
        {
            if( list.get(i).contains( "§l" ) )
                drawCenteredString(font, list.get(i), sr.getScaledWidth() / 2, ( sr.getScaledHeight() / 2 - ( list.size() * 20 ) / 2 ) + i * 20, color );
            else
                drawCenteredString(font, "§l" + list.get(i), sr.getScaledWidth() / 2, ( sr.getScaledHeight() / 2 - ( list.size() * 20 ) / 2 ) + i * 20, color );
        }

//        fillGradient(x + 20, y + 52, x + 232, y + 164, 0x22444444, 0x33222222);

//        for( TileButton button : tileButtons )
//        {
//            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
//                renderTooltip( new TranslationTextComponent( button.transKey ).getFormattedText(), mouseX, mouseY );
//        }

        RenderSystem.enableBlend();
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        else
            this.renderDirtBackground(p_renderBackground_1_);


        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );
        RenderSystem.disableBlend();
        this.blit( x, y, 0, 0,  boxWidth, boxHeight );
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
//        uuidName.put( "3066eaa7-6387-489d-b04b-cce7b505ee87", "neothiamin#1798" );
//        uuidName.put( "1951c4ee-52e1-421c-927b-43fb941add98", "BusanDaek#3970" );
//        uuidName.put( "d3167127-daa9-485b-ab14-c842c888e087", "deezer911#5693" );
//        uuidName.put( "bfacfe26-94d7-4c6a-a337-fee6aad555bb", "TorukM4kt00#0246" );

        List<String> list;

        /////////LAPIS//////////////
        PlayerConnectedHandler.lapisPatreons.forEach( a ->
        {
            colors.put( uuidName.get( a.toString() ), 0x5555ff );
            creditorsInfo.put( uuidName.get( a.toString() ), new ArrayList<>());
        });
        //LUCIFER
        list = creditorsInfo.get( "Lucifer#0666" );
        list.add( "Lucifer#0666" );
        list.add( "First Lapis Tier Patreon" );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "28/04/2020" ).getString() );

        /////////DANDELION//////////
        //TYRIUS
        PlayerConnectedHandler.dandelionPatreons.forEach( a ->
        {
            colors.put( uuidName.get( a.toString() ), 0xffff33 );
            creditorsInfo.put( uuidName.get( a.toString() ), new ArrayList<>());
        });
        list = creditorsInfo.get( "Tyrius#0842" );
        list.add( "Tyrius#0842" );
        list.add( "First Dandelion Tier Patreon" );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "19/03/2020" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.creatorOfModpack", "The Cosmic Tree" ).getString() );

        /////////IRON///////////////
        //DIDIS54
        PlayerConnectedHandler.ironPatreons.forEach( a ->
        {
            colors.put( uuidName.get( a.toString() ), 0xeeeeee );
            creditorsInfo.put( uuidName.get( a.toString() ), new ArrayList<>());
        } );
        list = creditorsInfo.get( "didis54#5815" );
        list.add( "didis54#5815" );
        list.add( "First Iron Tier Patreon" );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "11/04/2020" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.creatorOfModpack", "Anarkhe Revolution" ).getString() );

        //NEOTHIAMIN
        list = new ArrayList<>();
        list.add( "neothiamin#1798" );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "17/04/2020" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.creatorOfModpack", "Skillful Survival" ).getString() );
        creditorsInfo.put( "neothiamin#1798", list );
        //DARTH_REVAN#7341
        list = new ArrayList<>();
        list.add( "Darth Revan#7341" );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "17/04/2020" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.creatorOfModpack", "Zombie Textiles" ).getString() );
        creditorsInfo.put( "Darth Revan#7341", list );

        /////////TRANSLATOR/////////
        //BUSANDAEK
        list = new ArrayList<>();
        list.add( "BusanDaek#3970" );
        list.add( new TranslationTextComponent( "pmmo.translated", "Korean" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "31/03/2020" ).getString() );
        creditorsInfo.put( "BusanDaek#3970", list );
        //MAREOFTHESTARS
        list = new ArrayList<>();
        list.add( "deezer911#5693" );
        list.add( new TranslationTextComponent( "pmmo.translated", "French" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "11/03/2020" ).getString() );
        creditorsInfo.put( "deezer911#5693", list );
        //TORUKM4KT00#0246
        list = new ArrayList<>();
        list.add( "TorukM4kt00#0246" );
        list.add( new TranslationTextComponent( "pmmo.translated", "Portuguese - Brazil" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "13/05/2020" ).getString() );
        creditorsInfo.put( "TorukM4kt00#0246", list );
        //STARCHE#0246
        list = new ArrayList<>();
        list.add( "starche#7569" );
        list.add( new TranslationTextComponent( "pmmo.translated", "Russian" ).getString() );
        list.add( new TranslationTextComponent( "pmmo.discordMemberSince", "24/07/2020" ).getString() );
        creditorsInfo.put( "starche#7569", list );

        /////////COLOR//////////////
//        colors.put( "pmmo.lapisPatreon", 0x5555ff );
//        colors.put( "pmmo.dandelionPatreon", 0xffff33 );
//        colors.put( "pmmo.ironPatreon", 0xeeeeee );
    }
}