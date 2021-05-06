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
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class InfoScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final ResourceLocation logo = XP.getResLoc( Reference.MOD_ID, "textures/gui/logo.png" );
    private static TileButton exitButton;

    Minecraft mc = Minecraft.getInstance();
    MainWindow sr = mc.getMainWindow();
    FontRenderer font = mc.fontRenderer;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<ListButtonBig> tileButtons;
    private UUID uuid;

    public InfoScreen( UUID uuid, ITextComponent titleIn )
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

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, new TranslationTextComponent( "pmmo.potato" ) ) );
        });

        ListButtonBig discordButton = new ListButtonBig( 0, 0, 1, 11, "Discord", new TranslationTextComponent( "pmmo.discord" ).getString(), "", button ->
        {
            promptOpenLink( "https://discord.gg/5NVNkNB" );
        });

        ListButtonBig featuresButton = new ListButtonBig( 0, 0, 1, 9, "Features", new TranslationTextComponent( "pmmo.features" ).getString(), "", button ->
        {
            promptOpenLink( "https://harmonised7.github.io/minecraft/pmmo/Features.html" );
        });

        ListButtonBig customizeButton = new ListButtonBig( 0, 0, 1, 10, "Customize", new TranslationTextComponent( "pmmo.customize" ).getString(), "", button ->
        {
            promptOpenLink( "https://docs.google.com/document/d/1OaNxk1I3PTBfdGV5PlHqUpaENTkr49Tg26YhK3N5zTQ" );
        });

        ListButtonBig patreonButton = new ListButtonBig( 0, 0, 1, 14, "Patreon", new TranslationTextComponent( "pmmo.patreon" ).getString(), "", button ->
        {
            promptOpenLink( "https://patreon.com/harmonised" );
        });

        ListButtonBig donateButton = new ListButtonBig( 0, 0, 1, 13, "Donate", new TranslationTextComponent( "pmmo.donate" ).getString(), "", button ->
        {
            promptOpenLink( "https://ko-fi.com/harmonised" );
        });

        ListButtonBig curseButton = new ListButtonBig( 0, 0, 1, 12, "CurseForge", new TranslationTextComponent( "pmmo.curseforge" ).getString(), "", button ->
        {
            promptOpenLink( "https://curseforge.com/minecraft/mc-mods/project-mmo" );
        });


        addButton(exitButton);

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
            int buttonX = -(button.getWidth()+2)/2 + sr.getScaledWidth()/2;
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
            addButton( button );
        }
    }

    public void promptOpenLink( String link )
    {
        mc.displayGuiScreen(new ConfirmOpenLinkScreen( (open) ->
        {
            if (open)
                Util.getOSType().openURI( link );

            this.minecraft.displayGuiScreen(this);
        }, link, true) );
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );
        super.render( stack, mouseX, mouseY, partialTicks );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        for( ListButtonBig button : tileButtons )
        {
            if( mouseX > button.x + 3 && mouseY > button.y && mouseX < button.x + 60 && mouseY < button.y + 64 )
            {
                renderTooltip( stack, new StringTextComponent( button.playerName ), mouseX, mouseY );
                break;
            }
        }

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString( stack, font, title.getString(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( stack, font, title.getString(), sr.getScaledWidth() / 2, y - 5, 0xffffff );
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
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
        Minecraft.getInstance().getTextureManager().bindTexture( box );
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