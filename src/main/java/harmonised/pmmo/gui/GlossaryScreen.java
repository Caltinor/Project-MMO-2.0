package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GlossaryScreen extends GuiScreen
{
    private final List<GuiButton> buttons = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png");
    private static TileButton exitButton;

    Minecraft mc = Minecraft.getMinecraft();
    ScaledResolution sr = new ScaledResolution( mc );
    FontRenderer font = mc.fontRenderer;
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
        super();
        this.uuid = uuid;
        this.loadDefaultButtons = loadDefaultButtons;
    }

    public static void initButtons()
    {
        defaultTileButtons = new ArrayList<>();

        TileButton wearButton = new TileButton( 1337, 0, 0, 3, 9, "pmmo.wearTitle", JType.REQ_WEAR, GlossaryScreen::onGlossaryButtonPress );
        TileButton toolButton = new TileButton( 1337,  0, 0, 3, 10, "pmmo.toolTitle", JType.REQ_TOOL, GlossaryScreen::onGlossaryButtonPress );
        TileButton weaponButton = new TileButton( 1337,  0, 0, 3, 11, "pmmo.weaponTitle", JType.REQ_WEAPON, GlossaryScreen::onGlossaryButtonPress );
        TileButton useButton = new TileButton( 1337,  0, 0, 3, 12, "pmmo.useTitle", JType.REQ_USE, GlossaryScreen::onGlossaryButtonPress );
        TileButton placeButton = new TileButton( 1337,  0, 0, 3, 13, "pmmo.placeTitle", JType.REQ_PLACE, GlossaryScreen::onGlossaryButtonPress );
        TileButton breakButton = new TileButton( 1337,  0, 0, 3, 14, "pmmo.breakTitle", JType.REQ_BREAK, GlossaryScreen::onGlossaryButtonPress );
        TileButton craftButton = new TileButton( 1337,  0, 0, 3, 29, "pmmo.craftTitle", JType.REQ_CRAFT, GlossaryScreen::onGlossaryButtonPress );
        TileButton biomeButton = new TileButton( 1337,  0, 0, 3, 8, "pmmo.biomeTitle", JType.REQ_BIOME, GlossaryScreen::onGlossaryButtonPress );
        TileButton oreButton = new TileButton( 1337,  0, 0, 3, 15, "pmmo.oreTitle", JType.INFO_ORE, GlossaryScreen::onGlossaryButtonPress );
        TileButton logButton = new TileButton( 1337,  0, 0, 3, 16, "pmmo.logTitle", JType.INFO_LOG, GlossaryScreen::onGlossaryButtonPress );
        TileButton plantButton = new TileButton( 1337,  0, 0, 3, 17, "pmmo.plantTitle", JType.INFO_PLANT, GlossaryScreen::onGlossaryButtonPress );
        TileButton smeltButton = new TileButton( 1337,  0, 0, 3, 30, "pmmo.smeltTitle", JType.INFO_SMELT, GlossaryScreen::onGlossaryButtonPress );
        TileButton cookButton = new TileButton( 1337,  0, 0, 3, 32, "pmmo.cookTitle", JType.INFO_COOK, GlossaryScreen::onGlossaryButtonPress );
        TileButton brewButton = new TileButton( 1337,  0, 0, 3, 36, "pmmo.brewTitle", JType.INFO_BREW, GlossaryScreen::onGlossaryButtonPress );
        TileButton heldXpButton = new TileButton( 1337,  0, 0, 3, 19, "pmmo.heldTitle", JType.XP_BONUS_HELD, GlossaryScreen::onGlossaryButtonPress );
        TileButton wornXpButton = new TileButton( 1337,  0, 0, 3, 18, "pmmo.wornTitle", JType.XP_BONUS_WORN, GlossaryScreen::onGlossaryButtonPress );
        TileButton breedXpButton = new TileButton( 1337,  0, 0, 3, 20, "pmmo.breedXpTitle", JType.XP_VALUE_BREED, GlossaryScreen::onGlossaryButtonPress );
        TileButton tameXpButton = new TileButton( 1337,  0, 0, 3, 21, "pmmo.tameXpTitle", JType.XP_VALUE_TAME, GlossaryScreen::onGlossaryButtonPress );
        TileButton craftXpButton = new TileButton( 1337,  0, 0, 3, 22, "pmmo.craftXpTitle", JType.XP_VALUE_CRAFT, GlossaryScreen::onGlossaryButtonPress );
        TileButton breakXpButton = new TileButton( 1337,  0, 0, 3, 23, "pmmo.breakXpTitle",JType.XP_VALUE_BREAK, GlossaryScreen::onGlossaryButtonPress );
        TileButton smeltXpButton = new TileButton( 1337,  0, 0, 3, 31, "pmmo.smeltXpTitle",JType.XP_VALUE_SMELT, GlossaryScreen::onGlossaryButtonPress );
        TileButton cookXpButton = new TileButton( 1337,  0, 0, 3, 33, "pmmo.cookXpTitle",JType.XP_VALUE_COOK, GlossaryScreen::onGlossaryButtonPress );
        TileButton brewXpButton = new TileButton( 1337,  0, 0, 3, 37, "pmmo.brewXpTitle",JType.XP_VALUE_BREW, GlossaryScreen::onGlossaryButtonPress );
        TileButton growXpButton = new TileButton( 1337,  0, 0, 3, 35, "pmmo.growXpTitle",JType.XP_VALUE_GROW, GlossaryScreen::onGlossaryButtonPress );
        TileButton dimensionButton = new TileButton( 1337,  0, 0, 3, 8, "pmmo.dimensionTitle",JType.DIMENSION, GlossaryScreen::onGlossaryButtonPress );
        TileButton fishPoolButton = new TileButton( 1337,  0, 0, 3, 24, "pmmo.fishPoolTitle",JType.FISH_POOL, GlossaryScreen::onGlossaryButtonPress );
        TileButton mobButton = new TileButton( 1337,  0, 0, 3, 26, "pmmo.mobTitle" ,JType.REQ_KILL, GlossaryScreen::onGlossaryButtonPress );
        TileButton fishEnchantButton = new TileButton( 1337,  0, 0, 3, 25, "pmmo.fishEnchantTitle", JType.FISH_ENCHANT_POOL, GlossaryScreen::onGlossaryButtonPress );
        TileButton salvageToButton = new TileButton( 1337,  0, 0, 3, 27, "pmmo.salvagesToTitle", JType.SALVAGE, GlossaryScreen::onGlossaryButtonPress );
        TileButton salvageFromButton = new TileButton( 1337,  0, 0, 3, 28, "pmmo.salvagesFromTitle", JType.SALVAGE_FROM, GlossaryScreen::onGlossaryButtonPress );
        TileButton treasureToButton = new TileButton( 1337,  0, 0, 3, 38, "pmmo.treasureToTitle", JType.TREASURE, GlossaryScreen::onGlossaryButtonPress );
        TileButton treasureFromButton = new TileButton( 1337,  0, 0, 3, 39, "pmmo.treasureFromTitle", JType.TREASURE_FROM, GlossaryScreen::onGlossaryButtonPress );

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
    public void initGui()
    {
        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        creativeText = new TextComponentTranslation( "pmmo.creativeWarning" ).getUnformattedText();

        exitButton = new TileButton( 1337, x + boxWidth - 24, y - 8, 7, 0, "", JType.NONE, button ->
        {
            history = new ArrayList<>();
            Minecraft.getMinecraft().displayGuiScreen( new MainScreen( uuid, new TextComponentString( "pmmo.potato" ) ) );
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
            Minecraft.getMinecraft().player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.8F + rand.nextFloat() * 0.4F, 0.9F + rand.nextFloat() * 0.15F );
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
                    Minecraft.getMinecraft().player.playSound( SoundEvents.ENTITY_ENDERDRAGON_DEATH, 5F + rand.nextFloat() * 0.4F, -5F - rand.nextFloat() * 0.15F );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground( 1 );
        super.drawScreen(mouseX, mouseY, partialTicks);

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        GlStateManager.disableBlend();
        GlStateManager.pushAttrib();
        for( TileButton button : currentTileButtons )
        {
            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
                drawHoveringText( new TextComponentTranslation( button.transKey ).getFormattedText(), mouseX, mouseY );
        }
        GlStateManager.popAttrib();

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
    public void drawBackground(int p_renderBackground_1_)
    {
        if (this.mc != null)
        {
            this.drawGradientRect(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getMinecraft().getTextureManager().bindTexture( box );

        this.drawTexturedModalRect( x, y, 0, 0,  boxWidth, boxHeight );
    }

//    @Override
//    public boolean mouseScrolled(int mouseX, int mouseY, double scroll)
//    {
//        return super.mouseScrolled(mouseX, mouseY, scroll);
//    }

    @Override
    public void mouseClicked( int mouseX, int mouseY, int mouseButton ) throws IOException
    {
        if( mouseButton == 1 )
            exitButton.onPress();
        else
            super.mouseClicked( mouseX, mouseY, mouseButton );
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove( int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick )
{
        super.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
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
        {
            Item item = XP.getItem( regKey );
            GlossaryScreen.transKey = new TextComponentTranslation( item.getItemStackDisplayName( new ItemStack( item ) ) ).getUnformattedText();
        }
    }
}