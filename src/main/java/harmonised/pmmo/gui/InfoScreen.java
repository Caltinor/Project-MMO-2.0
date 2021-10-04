package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;

public class InfoScreen extends Screen
{
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final ResourceLocation logo = XP.getResLoc( Reference.MOD_ID, "textures/gui/logo.png" );
    private static TileButton exitButton;

    Minecraft mc = Minecraft.getInstance();
    Window sr = mc.getWindow();
    Font font = mc.font;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<ListButtonBig> tileButtons;
    private UUID uuid;

    public InfoScreen( UUID uuid, Component titleIn )
    {
        super(titleIn);
        this.uuid = uuid;
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

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().setScreen( new MainScreen( uuid, new TranslatableComponent( "pmmo.potato" ) ) );
        });

        ListButtonBig discordButton = new ListButtonBig( 0, 0, 1, 9, "FAQ", new TranslatableComponent( "pmmo.faq" ).getString(), "", button ->
        {
            promptOpenLink( "https://docs.google.com/document/d/1njMEjYR9aPvwLsTsH2eALV6ci4lNEULEoYlm83n4iOc" );
        });

        ListButtonBig featuresButton = new ListButtonBig( 0, 0, 1, 9, "Features", new TranslatableComponent( "pmmo.features" ).getString(), "", button ->
        {
            promptOpenLink( "https://harmonised7.github.io/minecraft/pmmo/Features.html" );
        });

        ListButtonBig customizeButton = new ListButtonBig( 0, 0, 1, 10, "Customize", new TranslatableComponent( "pmmo.customize" ).getString(), "", button ->
        {
            promptOpenLink( "https://docs.google.com/document/d/1OaNxk1I3PTBfdGV5PlHqUpaENTkr49Tg26YhK3N5zTQ" );
        });

        ListButtonBig patreonButton = new ListButtonBig( 0, 0, 1, 14, "Patreon", new TranslatableComponent( "pmmo.patreon" ).getString(), "", button ->
        {
            promptOpenLink( "https://patreon.com/harmonised" );
        });

        ListButtonBig donateButton = new ListButtonBig( 0, 0, 1, 13, "Donate", new TranslatableComponent( "pmmo.donate" ).getString(), "", button ->
        {
            promptOpenLink( "https://ko-fi.com/harmonised" );
        });

        ListButtonBig curseButton = new ListButtonBig( 0, 0, 1, 12, "CurseForge", new TranslatableComponent( "pmmo.curseforge" ).getString(), "", button ->
        {
            promptOpenLink( "https://curseforge.com/minecraft/mc-mods/project-mmo" );
        });


        addWidget(exitButton);

        tileButtons.add( discordButton );
        tileButtons.add( featuresButton );
        tileButtons.add( customizeButton );
        tileButtons.add( patreonButton );
        tileButtons.add( donateButton );
        tileButtons.add( curseButton );

        for( int i = 0; i < tileButtons.size(); i++ )
        {
            ListButtonBig button = tileButtons.get( i );
            int mode = i % 3;
            int buttonX = -(button.getWidth()+2)/2 + sr.getGuiScaledWidth()/2;
            int buttonY = y + 20 + (i/3) * 100;
            switch( mode )
            {
                case 0:
                    buttonX -= 50;
                    break;

                case 1:
                    buttonX += 50;
                    break;

                case 2:
                    buttonY += 50;
                    break;
            }
            button.x = buttonX;
            button.y = buttonY;
            addWidget( button );
        }
    }

    public void promptOpenLink( String link )
    {
        mc.setScreen(new ConfirmLinkScreen( (open) ->
        {
            if (open)
                Util.getPlatform().openUri( link );

            this.minecraft.setScreen(this);
        }, link, true) );
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );
        super.render( stack, mouseX, mouseY, partialTicks );

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );

        for( ListButtonBig button : tileButtons )
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
    }

    @Override
    public void renderBackground( PoseStack stack, int p_renderBackground_1_)
    {
        if (this.mc != null)
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