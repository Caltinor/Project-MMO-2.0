package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GlossaryScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png");

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private MyScrollPanel myList;
    private List<TileButton> tileButtons;
    private String creativeText;
    private UUID uuid;

    public GlossaryScreen( UUID uuid, ITextComponent titleIn )
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

        creativeText = new TranslationTextComponent( "pmmo.creativeWarning" ).getString();

        TileButton exitButton = new TileButton(x + boxWidth - 24, y - 8, 0, 7, "", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, new TranslationTextComponent( "pmmo.potato" ) ) );
        });
        TileButton wearButton = new TileButton(0, 0, 3, 9, "pmmo.wearTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen ( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "wear", Minecraft.getInstance().player) );
        });
        TileButton toolButton = new TileButton( 0, 0, 3, 10, "pmmo.toolTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "tool", Minecraft.getInstance().player ) );
        });
        TileButton weaponButton = new TileButton( 0, 0, 3, 11, "pmmo.weaponTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "weapon", Minecraft.getInstance().player ) );
        });
        TileButton useButton = new TileButton( 0, 0, 3, 12, "pmmo.useTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "use", Minecraft.getInstance().player ) );
        });
        TileButton placeButton = new TileButton( 0, 0, 3, 13, "pmmo.placeTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "place", Minecraft.getInstance().player ) );
        });
        TileButton breakButton = new TileButton( 0, 0, 3, 14, "pmmo.breakTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "break", Minecraft.getInstance().player ) );
        });
        TileButton biomeButton = new TileButton( 0, 0, 3, 8, "pmmo.biomeTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "biome", Minecraft.getInstance().player ) );
        });
        TileButton oreButton = new TileButton( 0, 0, 3, 15, "pmmo.oreTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "ore", Minecraft.getInstance().player ) );
        });
        TileButton logButton = new TileButton( 0, 0, 3, 16, "pmmo.logTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "log", Minecraft.getInstance().player ) );
        });
        TileButton plantButton = new TileButton( 0, 0, 3, 17, "pmmo.plantTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "plant", Minecraft.getInstance().player ) );
        });
        TileButton heldXpButton = new TileButton( 0, 0, 3, 19, "pmmo.heldTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "held", Minecraft.getInstance().player ) );
        });
        TileButton wornXpButton = new TileButton( 0, 0, 3, 18, "pmmo.wornTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "worn", Minecraft.getInstance().player ) );
        });
        TileButton breedXpButton = new TileButton( 0, 0, 3, 20, "pmmo.breedXpTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "breedXp", Minecraft.getInstance().player ) );
        });
        TileButton tameXpButton = new TileButton( 0, 0, 3, 21, "pmmo.tameXpTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "tameXp", Minecraft.getInstance().player ) );
        });
        TileButton craftXpButton = new TileButton( 0, 0, 3, 22, "pmmo.craftXpTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "craftXp", Minecraft.getInstance().player ) );
        });
        TileButton breakXpButton = new TileButton( 0, 0, 3, 23, "pmmo.breakXpTitle","", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "breakXp", Minecraft.getInstance().player ) );
        });
        TileButton dimensionButton = new TileButton( 0, 0, 3, 8, "pmmo.dimensionTitle","", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "dimension", Minecraft.getInstance().player ) );
        });
        TileButton fishPoolButton = new TileButton( 0, 0, 3, 24, "pmmo.fishPoolTitle","", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "fishPool", Minecraft.getInstance().player ) );
        });
        TileButton mobButton = new TileButton( 0, 0, 3, 26, "pmmo.mobTitle" ,"", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "kill", Minecraft.getInstance().player ) );
        });
        TileButton fishEnchantButton = new TileButton( 0, 0, 3, 25, "pmmo.fishEnchantTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "fishEnchantPool", Minecraft.getInstance().player ) );
        });
        TileButton salvageToButton = new TileButton( 0, 0, 3, 27, "pmmo.salvagesToTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "salvagesTo", Minecraft.getInstance().player ) );
        });
        TileButton salvageFromButton = new TileButton( 0, 0, 3, 28, "pmmo.salvagesFromTitle", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), "salvagesFrom", Minecraft.getInstance().player ) );
        });

        addButton(exitButton);

        tileButtons.add(wearButton);
        tileButtons.add( toolButton );
        tileButtons.add( weaponButton );
        tileButtons.add( useButton );
        tileButtons.add( placeButton );
        tileButtons.add( breakButton );
        tileButtons.add( oreButton );
        tileButtons.add( logButton );
        tileButtons.add( plantButton );
        tileButtons.add( heldXpButton );
        tileButtons.add( wornXpButton );
        tileButtons.add( breedXpButton );
        tileButtons.add( tameXpButton );
        tileButtons.add( craftXpButton );
        tileButtons.add( breakXpButton );
        tileButtons.add( dimensionButton );
        tileButtons.add( biomeButton );
        tileButtons.add( mobButton );
        tileButtons.add( fishPoolButton );
        tileButtons.add( fishEnchantButton );
        tileButtons.add( salvageToButton );
        tileButtons.add( salvageFromButton );

        int col = 0;
        int row = 0;

        for( TileButton button : tileButtons )
        {
            button.x = x + 24 + col * 36;
            button.y = y + 24 + row * 36;
            addButton( button );
            if( ++col > 5 )
            {
                col = 0;
                row++;
            }
        }

        int column = 1;
        int skillID;
//        for( int i = 0; i < 4; i++ )
//        {
//            for( int j = 1; j <= 6; j++ )
//            {
//                skillID = i * 6 + j;
//                if(Skill.getSkill( j ) != Skill.INVALID_SKILL )
//                {
//                    Button button = new TileButton( x + 24 + (j - 1) * 34, y + 16 + i * 34, 1, 0, I18n.format("" ), (button) ->
//                    {
//                        button.
//                    });
//
//                    tileButtons.add( button );
//                }
//
//                if( i % 6 == 0 )
//                    column++;
//            }
//        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );
        super.render(mouseX, mouseY, partialTicks);

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        for( TileButton button : tileButtons )
        {
            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
                renderTooltip( new TranslationTextComponent( button.transKey ).getFormattedText(), mouseX, mouseY );
        }

        if( Minecraft.getInstance().player.isCreative() )
        {
            if( font.getStringWidth( creativeText ) > 220 )
                drawCenteredString(Minecraft.getInstance().fontRenderer, creativeText,sr.getScaledWidth() / 2, y - 10, 0xffff00 );
            else
                drawCenteredString(Minecraft.getInstance().fontRenderer, creativeText,sr.getScaledWidth() / 2, y - 5, 0xffff00 );
        }
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

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