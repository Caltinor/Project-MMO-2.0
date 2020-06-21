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
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static final Map<String, List<String>> creditorsInfo = new HashMap<>();
    private static Map<String, Integer> colors = new HashMap<>();
    private static boolean firstTime = true;
    private static TileButton exitButton;

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<TileButton> tileButtons;
    private UUID uuid;
    private int lastScroll;
    private int color;
    private String transKey;

    public CreditorScreen( UUID uuid, String transKey, int lastScroll )
    {
        super( new TranslationTextComponent( transKey ) );
        this.uuid = uuid;
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
            Minecraft.getInstance().displayGuiScreen( new CreditsScreen( uuid, new TranslationTextComponent( "pmmo.credits" ), lastScroll ) );
        });

        addButton(exitButton);

//        for( TileButton button : tileButtons )
//        {
//            addButton( button );
//        }

        if( firstTime )
        {
            initCreditors();
            firstTime = false;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );
        super.render(mouseX, mouseY, partialTicks);

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        color = 0xffffff;

        if( colors.containsKey( transKey ) )
            color = colors.get( transKey );

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, color );
        else
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, color );

        List<String> list = creditorsInfo.get( uuid.toString() );

        for( int i = 0; i < list.size(); i++ )
        {
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

    private static void initCreditors()
    {
        List<String> list;

        /////////LAPIS//////////////
        PlayerConnectedHandler.lapisPatreons.forEach( a -> creditorsInfo.put(a.toString(), new ArrayList<>()) );
        //LUCIFER
        list = creditorsInfo.get( "e4c7e475-c1ff-4f94-956c-ac5be02ce04a" );
        list.add( "Lucifer#0666" );
        list.add( "First Lapis Tier Patreon" );
        list.add( "Discord Member Since 28/04/2020" );

        /////////DANDELION//////////
        //TYRIUS
        PlayerConnectedHandler.dandelionPatreons.forEach( a -> creditorsInfo.put(a.toString(), new ArrayList<>()) );
        list = creditorsInfo.get( "8eb0578d-c113-49d3-abf6-a6d36f6d1116" );
        list.add( "Tyrius#0842" );
        list.add( "First Dandelion Tier Patreon" );
        list.add( "Discord Member Since 19/03/2020" );

        /////////IRON///////////////
        //DIDIS54
        PlayerConnectedHandler.ironPatreons.forEach( a -> creditorsInfo.put(a.toString(), new ArrayList<>()) );
        list = creditorsInfo.get( "2ea5efa1-756b-4c9e-9605-7f53830d6cfa" );
        list.add( "didis54#5815" );
        list.add( "First Iron Tier Patreon" );
        list.add( "Discord Member Since 11/04/2020" );

        /////////TRANSLATOR/////////
        //BUSANDAEK
        list = new ArrayList<>();
        list.add( "BusanDaek#3970" );
        list.add( "Translated Korean" );
        list.add( "Discord Member Since 31/03/2020" );
        creditorsInfo.put("1951c4ee-52e1-421c-927b-43fb941add98", list );
        //MAREOFTHESTARS
        list = new ArrayList<>();
        list.add( "deezer911#5693" );
        list.add( "Translated French" );
        list.add( "Discord Member Since 11/03/2020" );
        creditorsInfo.put("d3167127-daa9-485b-ab14-c842c888e087", list );
        //TORUKM4KT00#0246
        list = new ArrayList<>();
        list.add( "TorukM4kt00#0246" );
        list.add( "Translated Portuguese - Brazil" );
        list.add( "Discord Member Since 13/05/2020" );
        creditorsInfo.put("bfacfe26-94d7-4c6a-a337-fee6aad555bb", list );

        /////////COLOR//////////////
        colors.put("pmmo.lapisPatreon", 0x5555ff );
        colors.put("pmmo.dandelionPatreon", 0xffff33 );
        colors.put("pmmo.ironPatreon", 0xeeeeee );
    }
}