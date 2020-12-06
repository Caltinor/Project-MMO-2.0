package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GlossaryScreen extends GuiScreen
{
    private final List<GuiButton> buttons = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png");
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getMinecraft();
    ScaledResolution sr = new ScaledResolution( mc );
    FontRenderer font = minecraft.fontRenderer;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    public static List<TileButton> defaultTileButtons = new ArrayList<>();
    public static List<TileButton> currentTileButtons = new ArrayList<>();
    private String creativeText;
    private UUID uuid;
    public static List<Character> history = new ArrayList<>();
    private static String[] weaster = { "1523", "3251", "911" };
    private static Random rand = new Random();
    private static String transKey;
    private boolean loadDefaultButtons;

    public GlossaryScreen( UUID uuid, ITextComponent titleIn, boolean loadDefaultButtons )
    {
        super(titleIn);
        this.uuid = uuid;
        this.loadDefaultButtons = loadDefaultButtons;
    }

    public static void initButtons()
    {
        defaultTileButtons = new ArrayList<>();

        TileButton wearButton = new TileButton(0, 0, 3, 9, "pmmo.wearTitle", JType.REQ_WEAR, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton toolButton = new TileButton( 0, 0, 3, 10, "pmmo.toolTitle", JType.REQ_TOOL, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton weaponButton = new TileButton( 0, 0, 3, 11, "pmmo.weaponTitle", JType.REQ_WEAPON, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton useButton = new TileButton( 0, 0, 3, 12, "pmmo.useTitle", JType.REQ_USE, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton placeButton = new TileButton( 0, 0, 3, 13, "pmmo.placeTitle", JType.REQ_PLACE, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton breakButton = new TileButton( 0, 0, 3, 14, "pmmo.breakTitle", JType.REQ_BREAK, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton craftButton = new TileButton( 0, 0, 3, 29, "pmmo.craftTitle", JType.REQ_CRAFT, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton biomeButton = new TileButton( 0, 0, 3, 8, "pmmo.biomeTitle", JType.REQ_BIOME, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton oreButton = new TileButton( 0, 0, 3, 15, "pmmo.oreTitle", JType.INFO_ORE, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton logButton = new TileButton( 0, 0, 3, 16, "pmmo.logTitle", JType.INFO_LOG, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton plantButton = new TileButton( 0, 0, 3, 17, "pmmo.plantTitle", JType.INFO_PLANT, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton smeltButton = new TileButton( 0, 0, 3, 30, "pmmo.smeltTitle", JType.INFO_SMELT, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton cookButton = new TileButton( 0, 0, 3, 32, "pmmo.cookTitle", JType.INFO_COOK, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton brewButton = new TileButton( 0, 0, 3, 36, "pmmo.brewTitle", JType.INFO_BREW, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton heldXpButton = new TileButton( 0, 0, 3, 19, "pmmo.heldTitle", JType.XP_BONUS_HELD, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton wornXpButton = new TileButton( 0, 0, 3, 18, "pmmo.wornTitle", JType.XP_BONUS_WORN, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton breedXpButton = new TileButton( 0, 0, 3, 20, "pmmo.breedXpTitle", JType.XP_VALUE_BREED, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton tameXpButton = new TileButton( 0, 0, 3, 21, "pmmo.tameXpTitle", JType.XP_VALUE_TAME, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton craftXpButton = new TileButton( 0, 0, 3, 22, "pmmo.craftXpTitle", JType.XP_VALUE_CRAFT, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton breakXpButton = new TileButton( 0, 0, 3, 23, "pmmo.breakXpTitle",JType.XP_VALUE_BREAK, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton smeltXpButton = new TileButton( 0, 0, 3, 31, "pmmo.smeltXpTitle",JType.XP_VALUE_SMELT, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton cookXpButton = new TileButton( 0, 0, 3, 33, "pmmo.cookXpTitle",JType.XP_VALUE_COOK, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton brewXpButton = new TileButton( 0, 0, 3, 37, "pmmo.brewXpTitle",JType.XP_VALUE_BREW, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton growXpButton = new TileButton( 0, 0, 3, 35, "pmmo.growXpTitle",JType.XP_VALUE_GROW, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton dimensionButton = new TileButton( 0, 0, 3, 8, "pmmo.dimensionTitle",JType.DIMENSION, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton fishPoolButton = new TileButton( 0, 0, 3, 24, "pmmo.fishPoolTitle",JType.FISH_POOL, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton mobButton = new TileButton( 0, 0, 3, 26, "pmmo.mobTitle" ,JType.REQ_KILL, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton fishEnchantButton = new TileButton( 0, 0, 3, 25, "pmmo.fishEnchantTitle", JType.FISH_ENCHANT_POOL, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton salvageToButton = new TileButton( 0, 0, 3, 27, "pmmo.salvagesToTitle", JType.SALVAGE, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton salvageFromButton = new TileButton( 0, 0, 3, 28, "pmmo.salvagesFromTitle", JType.SALVAGE_FROM, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton treasureToButton = new TileButton( 0, 0, 3, 38, "pmmo.treasureToTitle", JType.TREASURE, button -> onGlossaryButtonPress( (TileButton) button ) );
        TileButton treasureFromButton = new TileButton( 0, 0, 3, 39, "pmmo.treasureFromTitle", JType.TREASURE_FROM, button -> onGlossaryButtonPress( (TileButton) button ) );

        defaultTileButtons.add( wearButton );
        defaultTileButtons.add( toolButton );
        defaultTileButtons.add( weaponButton );
        defaultTileButtons.add( useButton );
        defaultTileButtons.add( placeButton );
        defaultTileButtons.add( breakButton );
        defaultTileButtons.add( craftButton );
        defaultTileButtons.add( oreButton );
        defaultTileButtons.add( logButton );
        defaultTileButtons.add( plantButton );
        defaultTileButtons.add( smeltButton );
        defaultTileButtons.add( cookButton );
        defaultTileButtons.add( brewButton );
        defaultTileButtons.add( heldXpButton );
        defaultTileButtons.add( wornXpButton );
        defaultTileButtons.add( breedXpButton );
        defaultTileButtons.add( tameXpButton );
        defaultTileButtons.add( craftXpButton );
        defaultTileButtons.add( breakXpButton );
        defaultTileButtons.add( smeltXpButton );
        defaultTileButtons.add( cookXpButton );
        defaultTileButtons.add( brewXpButton );
        defaultTileButtons.add( growXpButton );
        defaultTileButtons.add( dimensionButton );
        defaultTileButtons.add( biomeButton );
        defaultTileButtons.add( mobButton );
        defaultTileButtons.add( fishPoolButton );
        defaultTileButtons.add( fishEnchantButton );
        defaultTileButtons.add( salvageToButton );
        defaultTileButtons.add( salvageFromButton );
        defaultTileButtons.add( treasureToButton );
        defaultTileButtons.add( treasureFromButton );
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        creativeText = new TextComponentTranslation( "pmmo.creativeWarning" ).getUnformattedText();

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", JType.NONE, button ->
        {
            history = new ArrayList<>();
            Minecraft.getMinecraft().displayGuiScreen( new MainScreen( uuid, new TextComponentTranslation( "pmmo.potato" ) ) );
        });

        if( loadDefaultButtons )
            setButtonsToDefault();

        addButton(exitButton);

        int col = 0;
        int row = 0;

        for( TileButton button : currentTileButtons )
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

    public static void onGlossaryButtonPress( TileButton button )
    {
        updateHistory( button.index );
        Minecraft.getMinecraft().displayGuiScreen( new ListScreen( Minecraft.getMinecraft().player.getUniqueID(), new TextComponentTranslation( button.transKey ), "", button.jType, Minecraft.getMinecraft().player ) );
    }

    public static void updateHistory( int index )
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
            Minecraft.getMinecraft().player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.8F + rand.nextFloat() * 0.4F, 0.9F + rand.nextFloat() * 0.15F );
    }

    public static boolean updateHistory( char index )
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
                    Minecraft.getMinecraft().player.playSound(SoundEvents.ENTITY_PHANTOM_DEATH, SoundCategory.AMBIENT, 5F + rand.nextFloat() * 0.4F, -5F - rand.nextFloat() * 0.15F );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, double partialTicks)
    {
        renderBackground( 1 );
        super.render(mouseX, mouseY, partialTicks);

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        for( TileButton button : currentTileButtons )
        {
            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
                renderTooltip( new TextComponentTranslation( button.transKey ).getFormattedText(), mouseX, mouseY );
        }

        if( Minecraft.getMinecraft().player.isCreative() )
        {
            if( font.getStringWidth( creativeText ) > 220 )
            {
                drawCenteredString( Minecraft.getMinecraft().fontRenderer, transKey,sr.getScaledWidth() / 2, y - 18, 0xffffff );
                drawCenteredString( Minecraft.getMinecraft().fontRenderer, creativeText,sr.getScaledWidth() / 2, y - 10, 0xffff00 );
            }
            else
            {
                drawCenteredString( Minecraft.getMinecraft().fontRenderer, transKey,sr.getScaledWidth() / 2, y - 13, 0xffffff );
                drawCenteredString( Minecraft.getMinecraft().fontRenderer, creativeText,sr.getScaledWidth() / 2, y - 5, 0xffff00 );
            }
        }
        else
            drawCenteredString( Minecraft.getMinecraft().fontRenderer, transKey,sr.getScaledWidth() / 2, y - 5, 0xffffff );
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
        Minecraft.getMinecraft().getTextureManager().bindTexture( box );

        this.drawTexturedModalRect( x, y, 0, 0,  boxWidth, boxHeight );
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

    public static void setButtonsToDefault()
    {
        currentTileButtons = defaultTileButtons;
        GlossaryScreen.transKey = new TextComponentTranslation( "pmmo.glossary" ).getUnformattedText();
    }

    public static void setButtonsToKey( String regKey )
    {
        currentTileButtons = new ArrayList<>();

        for( TileButton button : defaultTileButtons )
        {
            if( ( JsonConfig.data.containsKey( button.jType ) && JsonConfig.data.get( button.jType ).containsKey( regKey ) ) || ( JsonConfig.data2.containsKey( button.jType ) && JsonConfig.data2.get( button.jType ).containsKey( regKey ) ) )
                currentTileButtons.add( button );
        }

        if( currentTileButtons.size() == 0 )
        {
            setButtonsToDefault();
            GlossaryScreen.transKey = new TextComponentTranslation( "pmmo.glossary" ).getUnformattedText();
        }
        else
            GlossaryScreen.transKey = new TextComponentTranslation( XP.getItem( regKey ).getTranslationKey() ).getUnformattedText();
    }
}