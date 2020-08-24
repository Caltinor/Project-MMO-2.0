package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GlossaryScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png");
    private static TileButton exitButton;

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private ListScrollPanel myList;
    private List<TileButton> tileButtons;
    private String creativeText;
    private UUID uuid;
    public static List<Character> history = new ArrayList<>();
    private static String[] weaster = { "1523", "3251", "911" };
    private static Random rand = new Random();

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

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", "", button ->
        {
            history = new ArrayList<>();
            Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, new TranslationTextComponent( "pmmo.potato" ) ) );
        });
        TileButton wearButton = new TileButton(0, 0, 3, 9, "pmmo.wearTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_WEAR, Minecraft.getInstance().player) );
        });
        TileButton toolButton = new TileButton( 0, 0, 3, 10, "pmmo.toolTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_TOOL, Minecraft.getInstance().player ) );
        });
        TileButton weaponButton = new TileButton( 0, 0, 3, 11, "pmmo.weaponTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_WEAPON, Minecraft.getInstance().player ) );
        });
        TileButton useButton = new TileButton( 0, 0, 3, 12, "pmmo.useTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_USE, Minecraft.getInstance().player ) );
        });
        TileButton placeButton = new TileButton( 0, 0, 3, 13, "pmmo.placeTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_PLACE, Minecraft.getInstance().player ) );
        });
        TileButton breakButton = new TileButton( 0, 0, 3, 14, "pmmo.breakTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_BREAK, Minecraft.getInstance().player ) );
        });
        TileButton craftButton = new TileButton( 0, 0, 3, 29, "pmmo.craftTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_CRAFT, Minecraft.getInstance().player ) );
        });
        TileButton biomeButton = new TileButton( 0, 0, 3, 8, "pmmo.biomeTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_BIOME, Minecraft.getInstance().player ) );
        });
        TileButton oreButton = new TileButton( 0, 0, 3, 15, "pmmo.oreTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.INFO_ORE, Minecraft.getInstance().player ) );
        });
        TileButton logButton = new TileButton( 0, 0, 3, 16, "pmmo.logTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.INFO_LOG, Minecraft.getInstance().player ) );
        });
        TileButton plantButton = new TileButton( 0, 0, 3, 17, "pmmo.plantTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.INFO_PLANT, Minecraft.getInstance().player ) );
        });
        TileButton smeltButton = new TileButton( 0, 0, 3, 30, "pmmo.smeltTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.INFO_SMELT, Minecraft.getInstance().player ) );
        });
        TileButton cookButton = new TileButton( 0, 0, 3, 32, "pmmo.cookTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.INFO_COOK, Minecraft.getInstance().player ) );
        });
        TileButton heldXpButton = new TileButton( 0, 0, 3, 19, "pmmo.heldTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.XP_BONUS_HELD, Minecraft.getInstance().player ) );
        });
        TileButton wornXpButton = new TileButton( 0, 0, 3, 18, "pmmo.wornTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.REQ_WEAR, Minecraft.getInstance().player ) );
        });
        TileButton breedXpButton = new TileButton( 0, 0, 3, 20, "pmmo.breedXpTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.XP_VALUE_BREED, Minecraft.getInstance().player ) );
        });
        TileButton tameXpButton = new TileButton( 0, 0, 3, 21, "pmmo.tameXpTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.XP_VALUE_TAME, Minecraft.getInstance().player ) );
        });
        TileButton craftXpButton = new TileButton( 0, 0, 3, 22, "pmmo.craftXpTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.XP_VALUE_CRAFT, Minecraft.getInstance().player ) );
        });
        TileButton breakXpButton = new TileButton( 0, 0, 3, 23, "pmmo.breakXpTitle","", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.XP_VALUE_BREAK, Minecraft.getInstance().player ) );
        });
        TileButton smeltXpButton = new TileButton( 0, 0, 3, 31, "pmmo.smeltXpTitle","", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.XP_VALUE_SMELT, Minecraft.getInstance().player ) );
        });
        TileButton cookXpButton = new TileButton( 0, 0, 3, 33, "pmmo.cookXpTitle","", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.XP_VALUE_COOK, Minecraft.getInstance().player ) );
        });
        TileButton dimensionButton = new TileButton( 0, 0, 3, 8, "pmmo.dimensionTitle","", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.DIMENSION, Minecraft.getInstance().player ) );
        });
        TileButton fishPoolButton = new TileButton( 0, 0, 3, 24, "pmmo.fishPoolTitle","", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.FISH_POOL, Minecraft.getInstance().player ) );
        });
        TileButton mobButton = new TileButton( 0, 0, 3, 26, "pmmo.mobTitle" ,"", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.XP_VALUE_KILL, Minecraft.getInstance().player ) );
        });
        TileButton fishEnchantButton = new TileButton( 0, 0, 3, 25, "pmmo.fishEnchantTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.FISH_ENCHANT_POOL, Minecraft.getInstance().player ) );
        });
        TileButton salvageToButton = new TileButton( 0, 0, 3, 27, "pmmo.salvagesToTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.SALVAGE_TO, Minecraft.getInstance().player ) );
        });
        TileButton salvageFromButton = new TileButton( 0, 0, 3, 28, "pmmo.salvagesFromTitle", "", button ->
        {
            updateHistory( ( (TileButton) button ).index );
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.SALVAGE_FROM, Minecraft.getInstance().player ) );
        });

        addButton(exitButton);

        tileButtons.add(wearButton);
        tileButtons.add( toolButton );
        tileButtons.add( weaponButton );
        tileButtons.add( useButton );
        tileButtons.add( placeButton );
        tileButtons.add( breakButton );
        tileButtons.add( craftButton );
        tileButtons.add( oreButton );
        tileButtons.add( logButton );
        tileButtons.add( plantButton );
        tileButtons.add( smeltButton );
        tileButtons.add( cookButton );
        tileButtons.add( heldXpButton );
        tileButtons.add( wornXpButton );
        tileButtons.add( breedXpButton );
        tileButtons.add( tameXpButton );
        tileButtons.add( craftXpButton );
        tileButtons.add( breakXpButton );
        tileButtons.add( smeltXpButton );
        tileButtons.add( cookXpButton );
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
            button.index = row * 6 + col;
            button.x = x + 22 + col * 36;
            button.y = y + 22 + row * 36;
            addButton( button );
            if( ++col > 5 )
            {
                col = 0;
                row++;
            }
        }
    }
    
    public void updateHistory( int index )
    {
        if( index >= 10 )
        {
            history = new ArrayList<>();
            return;
        }

        boolean combo = false;

        for( char c : Integer.toString( index + 1 ).toCharArray() )
        {
            if( !combo )
                combo = updateHistory( c );
        }

        if( combo )
            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.8F + rand.nextFloat() * 0.4F, 0.9F + rand.nextFloat() * 0.15F );
    }

    public boolean updateHistory( char index )
    {
        history.add( index );
        int historyLength = history.size();
        int passLength, startPos, pos;
        boolean passed;

//        System.out.println( history );

        for( String pass : weaster )
        {
            passLength = pass.length();

            if( historyLength >= passLength )
            {
                passed = true;
                pos = 0;
                startPos = historyLength - passLength;

                for( char c : pass.toCharArray() )
                {
                    if( history.get( startPos + pos ) != c )
                        passed = false;
                    pos++;
                }

                if( passed )
                {
                    Minecraft.getInstance().player.playSound(SoundEvents.ENTITY_PHANTOM_DEATH, SoundCategory.AMBIENT, 5F + rand.nextFloat() * 0.4F, -5F - rand.nextFloat() * 0.15F );
                    return true;
                }
            }
        }
        return false;
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