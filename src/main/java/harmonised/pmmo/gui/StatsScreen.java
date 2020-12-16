package harmonised.pmmo.gui;

import harmonised.pmmo.baubles.BaublesHandler;
import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.*;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

public class StatsScreen extends GuiScreen
{
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static TileButton exitButton;

    Minecraft mc = Minecraft.getMinecraft();
    ScaledResolution sr = new ScaledResolution( mc );
    FontRenderer font = mc.fontRenderer;
    private ITextComponent title;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y;
    private StatsScrollPanel scrollPanel;
    private List<StatsEntry> statsEntries;
    private UUID uuid;
    private JType jType = JType.STATS;

    public StatsScreen( UUID uuid, ITextComponent titleIn )
    {
        super();
        this.title = titleIn;
        this.uuid = uuid;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    public static void addXpMapEntryAsText( List<ITextComponent> text, Map<String, Double> xpBoosts )
    {
        Skill skill;
        String skillName;
        Style color;
        for( Map.Entry<String, Double> entry : xpBoosts.entrySet() )
        {
            skill = Skill.getSkill( entry.getKey() );
            color = XP.getSkillStyle( skill );
            skillName = new TextComponentTranslation( "pmmo." + skill.toString() ).getUnformattedText();
            text.add( new TextComponentString( ( entry.getValue() < 0 ? " " : " +" ) + new TextComponentTranslation( "pmmo.levelDisplayPercentage", DP.dpSoft( entry.getValue() ), skillName ).getUnformattedText() ).setStyle( color ) );
        }
    }

    @Override
    public void initGui()
    {
        sr = new ScaledResolution( mc );
        statsEntries = new ArrayList<>();
        ArrayList<ITextComponent> text;
        Map<String, Double> map;

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(1337, x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new MainScreen( uuid, new TextComponentTranslation( "pmmo.skills" ) ) );
        });

        EntityPlayer player = Minecraft.getMinecraft().player;
        ITextComponent entryTitle;

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.damage" );
        text.add( new TextComponentTranslation( "pmmo.damageBonusMelee", Skill.COMBAT.getLevel( player ) / FConfig.levelsPerDamageMelee ).setStyle( XP.skillStyle.get( Skill.COMBAT ) ) );
        text.add( new TextComponentTranslation( "pmmo.damageBonusArchery", Skill.ARCHERY.getLevel( player ) / FConfig.levelsPerDamageArchery ).setStyle( XP.skillStyle.get( Skill.ARCHERY ) ) );
        text.add( new TextComponentTranslation( "pmmo.damageBonusMagic", Skill.MAGIC.getLevel( player ) / FConfig.levelsPerDamageMagic ).setStyle( XP.skillStyle.get( Skill.MAGIC ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.speed" );
        text.add( new TextComponentTranslation( "pmmo.sprintSpeedBonus", DP.dpSoft( AttributeHandler.getSpeedBoostMultiplier( Skill.AGILITY.getLevel( player ) ) * 100D ) ).setStyle( XP.skillStyle.get( Skill.AGILITY ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.jump" );
        text.add( new TextComponentTranslation( "pmmo.jumpBonusSprint", DP.dpSoft( JumpHandler.getSprintJumpBoost( player ) / 0.14D ) ).setStyle( XP.skillStyle.get( Skill.AGILITY ) ) );
        text.add( new TextComponentTranslation( "pmmo.jumpBonusCrouch", DP.dpSoft( JumpHandler.getCrouchJumpBoost( player ) / 0.14D ) ).setStyle( XP.skillStyle.get( Skill.AGILITY ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.fallSaveChance" );
        text.add( new TextComponentTranslation( "pmmo.fallSaveChancePercentage", DP.dpSoft( DamageHandler.getFallSaveChance( player ) ) ).setStyle( XP.skillStyle.get( Skill.AGILITY ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.endurance" );
        text.add( new TextComponentTranslation( "pmmo.damageReductionPercentage", DP.dpSoft( DamageHandler.getEnduranceMultiplier( player ) ) ).setStyle( XP.skillStyle.get( Skill.ENDURANCE ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.hearts" );
        text.add( new TextComponentTranslation( "pmmo.heartBonus", AttributeHandler.getHeartBoost( player ) ).setStyle( XP.skillStyle.get( Skill.ENDURANCE ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.reach" );
        text.add( new TextComponentTranslation( "pmmo.reachBonus", DP.dpSoft( AttributeHandler.getReachBoost( player ) ) ).setStyle( XP.skillStyle.get( Skill.BUILDING ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

//        text = new ArrayList<>();
//        entryTitle = new TextComponentTranslation( "pmmo.respiration" );
//        text.add( new TextComponentTranslation( "pmmo.respirationBonus", getRespirationBonus( player ) ).setStyle( XP.skillStyle.get( Skill.SWIMMING ) ) );
//        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.underwaterNightVision" );
        text.add( new TextComponentTranslation( Skill.SWIMMING.getLevelDecimal( player ) >= FConfig.getConfig( "nightvisionUnlockLevel" ) ? "pmmo.unlocked" : "pmmo.locked" ).setStyle( XP.skillStyle.get( Skill.SWIMMING ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.rareFishPool" );
        text.add( new TextComponentTranslation( "pmmo.fishPoolChance", DP.dpSoft( FishedHandler.getFishPoolChance( player ) ) ).setStyle( XP.skillStyle.get( Skill.FISHING ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        ItemStack itemStack;
        text = new ArrayList<>();
        entryTitle = new TextComponentTranslation( "pmmo.xpBonuses" );
        InventoryPlayer inv = player.inventory;

        //Helm
        itemStack = inv.getStackInSlot( 39 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TextComponentTranslation( itemStack.getDisplayName() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        //Chest
        itemStack = inv.getStackInSlot( 38 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TextComponentTranslation( itemStack.getDisplayName() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        //Legs
        itemStack = inv.getStackInSlot( 37 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TextComponentTranslation( itemStack.getDisplayName() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        //Boots
        itemStack = inv.getStackInSlot( 36 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TextComponentTranslation( itemStack.getDisplayName() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        itemStack = player.getHeldItemOffhand();
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TextComponentTranslation( itemStack.getDisplayName() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        itemStack = player.getHeldItemMainhand();
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, true );
            if( map.size() > 0 )
            {
                text.add( new TextComponentTranslation( itemStack.getDisplayName() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        if( BaublesHandler.isLoaded() )
        {
            for( ItemStack stack : BaublesHandler.getBaublesItems( player ) )
            {
                if( !stack.isEmpty() )
                    addXpMapEntryAsText( text, XP.getStackXpBoosts( stack, true ) );
            }
        }

        map = XP.getDimensionBoosts( "" + player.world.getWorldType().getId() );
        if( map.size() > 0 )
        {
            text.add( new TextComponentTranslation( "pmmo.dimension" ) );
            addXpMapEntryAsText( text, map );    //Dimension
        }
        map = XP.getBiomeBoosts( player );
        if( map.size() > 0 )
        {
            text.add( new TextComponentTranslation( "pmmo.biome" ) );
            addXpMapEntryAsText( text, map );    //Biome
        }
        for( Map.Entry<String, Map<Skill, Double>> outterEntry : FConfig.getXpBoostsMap( player ).entrySet() )
        {
            text.add( new TextComponentTranslation( outterEntry.getKey() ) );
            addXpMapEntryAsText( text, NBTHelper.MapSkillKeyToString( outterEntry.getValue() ) );    //Biome
        }
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        scrollPanel = new StatsScrollPanel( Minecraft.getMinecraft(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        addButton(exitButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground( 1 );

        if( font.getStringWidth( title.getFormattedText() ) > 220 )
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.drawScreen( mouseX, mouseY, partialTicks );
        super.drawScreen( mouseX, mouseY, partialTicks );

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll() );
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
//    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
//    {
//        scrollPanel.mouseScrolled(mouseX, mouseY, scroll);
//        return super.mouseScrolled(mouseX, mouseY, scroll);
//    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        scrollPanel.scroll( Mouse.getEventDWheel() );
    }

    @Override
    public void mouseClicked( int mouseX, int mouseY, int button) throws IOException
    {
        if( button == 1 )
        {
            exitButton.onPress();
            return;
        }

        scrollPanel.mouseClicked(mouseX, mouseY, button);
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased( int mouseX, int mouseY, int button)
    {
        scrollPanel.mouseReleased(mouseX, mouseY, button);
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove( int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick )
    {
        scrollPanel.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
        super.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
    }

}
