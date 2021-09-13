package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;

public class CreditsScreen extends Screen
{
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final ResourceLocation logo = XP.getResLoc( Reference.MOD_ID, "textures/gui/logo.png" );
    private static boolean firstTime = true;
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    Window sr = minecraft.getWindow();
    Font font = minecraft.font;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private int scrollX, scrollY;
    private JType jType;
    private UUID uuid;
    private ArrayList<ListButtonBig> listButtons;
    private CreditsScrollPanel scrollPanel;

    public CreditsScreen( UUID uuid, Component titleIn, JType jType )
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

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );
        scrollX = x + 16;
        scrollY = y + 10;

        if( firstTime )
        {
            CreditorScreen.initCreditors();
            firstTime = false;
        }

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().setScreen( new MainScreen( uuid, new TranslatableComponent( "pmmo.skills" ) ) );
        });

        PlayerConnectedHandler.lapisPatreons.forEach( a ->
        {
            listButtons.add( new ListButtonBig( 0, 0, 1, 2, "", CreditorScreen.uuidName.get( a.toString() ), new TranslatableComponent( "pmmo.lapisPatreon" ).setStyle( XP.textStyle.get( "blue" ) ).getString(), button ->
            {
                Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
            }));
        });

        PlayerConnectedHandler.dandelionPatreons.forEach( a ->
        {
            listButtons.add( new ListButtonBig( 0, 0, 1, 3, "", CreditorScreen.uuidName.get( a.toString() ), new TranslatableComponent( "pmmo.dandelionPatreon" ).setStyle( XP.textStyle.get( "yellow" ) ).getString(), button ->
            {
                Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
            }));
        });

        PlayerConnectedHandler.ironPatreons.forEach( a ->
        {
            listButtons.add( new ListButtonBig( 0, 0, 1, 4, "", CreditorScreen.uuidName.get( a.toString() ), new TranslatableComponent( "pmmo.ironPatreon" ).setStyle( XP.textStyle.get( "grey" ) ).getString(), button ->
            {
                Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
            }));
        });

        //TRANSLATION

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "ko_kr", "BusanDaek#3970", new TranslatableComponent( "pmmo.translated", "Korean" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "fr_fr", "deezer911#5693", new TranslatableComponent( "pmmo.helpedTranslating", "French" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "fr_fr", "didis54#5815", new TranslatableComponent( "pmmo.helpedTranslating", "French" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "pt_br", "TorukM4kt00#0246", new TranslatableComponent( "pmmo.translated", "Portuguese - Brazil" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "ru_ru", "starche#7569", new TranslatableComponent( "pmmo.translated", "Portuguese - Brazil" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "nl_nl", "Dawnless#1153", new TranslatableComponent( "pmmo.translated", "Dutch" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "zh_tw", "Lyla#2639", new TranslatableComponent( "pmmo.translated", "Chinese Traditional" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "zh_cn", "Lyla#2639", new TranslatableComponent( "pmmo.translated", "Chinese Simplified" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "de_de", "Matterfall#1952", new TranslatableComponent( "pmmo.translated", "German" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 6, "es_ar", "N1co#9248", new TranslatableComponent( "pmmo.translated", "Spanish" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        //MODPACK

        listButtons.add( new ListButtonBig( 0, 0, 1, 5, "", "Tyrius#0842", new TranslatableComponent( "pmmo.creatorOfModpack", "The Cosmic Tree" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 5, "", "didis54#5815", new TranslatableComponent( "pmmo.creatorOfModpack", "Anarkhe Revolution" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 5, "", "neothiamin#1798", new TranslatableComponent( "pmmo.creatorOfModpack", "Skillful Survival" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        listButtons.add( new ListButtonBig( 0, 0, 1, 5, "", "Darth Revan#7341", new TranslatableComponent( "pmmo.creatorOfModpack", "Zombie Textiles" ).getString(), button ->
        {
            Minecraft.getInstance().setScreen( new CreditorScreen( ((ListButtonBig) button).playerName, "a", scrollPanel.getScroll() ) );
        }));

        addRenderableWidget(exitButton);

//        listButtons.sort( Comparator.comparingInt( a -> ((ListButtonBig) a).elementTwo ) );

        scrollPanel = new CreditsScrollPanel( Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, scrollY, scrollX, JType.CREDITS, listButtons );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );
        super.render( stack, mouseX, mouseY, partialTicks );
        scrollPanel.render( stack,  mouseX,mouseY,partialTicks );

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );

        for( ListButtonBig button : listButtons )
        {
            if( mouseX > button.x + 3 && mouseY > button.y && mouseX < button.x + 60 && mouseY < button.y + 64 )
            {
                renderTooltip( stack, new TextComponent( button.playerName ), mouseX, mouseY );
                break;
            }
        }

        if( font.width( title.getString() ) > 220 )
            drawCenteredString( stack, font, title.getString(), sr.getGuiScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( stack, font, title.getString(), sr.getGuiScaledWidth() / 2, y - 5, 0xffffff );

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
    }

    @Override
    public void renderBackground( PoseStack stack, int p_renderBackground_1_)
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
        Minecraft.getInstance().getTextureManager().bindForSetup( box );
        RenderSystem.disableBlend();
        this.blit( stack,  x, y, 0, 0,  boxWidth, boxHeight );
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
