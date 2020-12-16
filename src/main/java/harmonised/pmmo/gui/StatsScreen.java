package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.curios.Curios;
import harmonised.pmmo.events.DamageHandler;
import harmonised.pmmo.events.FishedHandler;
import harmonised.pmmo.events.JumpHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.items.IItemHandler;

import java.util.*;
import java.util.stream.Collectors;

public class StatsScreen extends Screen
{
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    MainWindow sr = minecraft.getMainWindow();
    FontRenderer font = minecraft.fontRenderer;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y;
    private StatsScrollPanel scrollPanel;
    private List<StatsEntry> statsEntries;
    private UUID uuid;
    private JType jType = JType.STATS;

    public StatsScreen( UUID uuid, ITextComponent titleIn )
    {
        super( titleIn );
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
            skillName = new TranslationTextComponent( "pmmo." + skill.toString() ).getString();
            text.add( new StringTextComponent( ( entry.getValue() < 0 ? " " : " +" ) + new TranslationTextComponent( "pmmo.levelDisplayPercentage", DP.dpSoft( entry.getValue() ), skillName ).getString() ).setStyle( color ) );
        }
    }

    @Override
    protected void init()
    {
        statsEntries = new ArrayList<>();
        ArrayList<ITextComponent> text;
        Map<String, Double> map;

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, new TranslationTextComponent( "pmmo.skills" ) ) );
        });
        
        PlayerEntity player = Minecraft.getInstance().player;
        TextComponent entryTitle;

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.damage" );
        text.add( new TranslationTextComponent( "pmmo.damageBonusMelee", Skill.COMBAT.getLevel( player ) / Config.forgeConfig.levelsPerDamageMelee.get() ).setStyle( XP.skillStyle.get( Skill.COMBAT ) ) );
        text.add( new TranslationTextComponent( "pmmo.damageBonusArchery", Skill.ARCHERY.getLevel( player ) / Config.forgeConfig.levelsPerDamageArchery.get() ).setStyle( XP.skillStyle.get( Skill.ARCHERY ) ) );
        text.add( new TranslationTextComponent( "pmmo.damageBonusMagic", Skill.MAGIC.getLevel( player ) / Config.forgeConfig.levelsPerDamageMagic.get() ).setStyle( XP.skillStyle.get( Skill.MAGIC ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.speed" );
        double baseSpeed = AttributeHandler.getBaseSpeed( player );
        double boostedSpeed = baseSpeed + AttributeHandler.getSpeedBoost( player );
        text.add( new TranslationTextComponent( "pmmo.sprintSpeedBonus", DP.dpSoft( boostedSpeed*100D / baseSpeed ) ).setStyle( XP.skillStyle.get( Skill.AGILITY ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.jump" );
        text.add( new TranslationTextComponent( "pmmo.jumpBonusSprint", DP.dpSoft( JumpHandler.getSprintJumpBoost( player ) / 0.14D ) ).setStyle( XP.skillStyle.get( Skill.AGILITY ) ) );
        text.add( new TranslationTextComponent( "pmmo.jumpBonusCrouch", DP.dpSoft( JumpHandler.getCrouchJumpBoost( player ) / 0.14D ) ).setStyle( XP.skillStyle.get( Skill.AGILITY ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.fallSaveChance" );
        text.add( new TranslationTextComponent( "pmmo.fallSaveChancePercentage", DP.dpSoft( DamageHandler.getFallSaveChance( player ) ) ).setStyle( XP.skillStyle.get( Skill.AGILITY ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.endurance" );
        text.add( new TranslationTextComponent( "pmmo.damageReductionPercentage", DP.dpSoft( DamageHandler.getEnduranceMultiplier( player ) ) ).setStyle( XP.skillStyle.get( Skill.ENDURANCE ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.hearts" );
        text.add( new TranslationTextComponent( "pmmo.heartBonus", AttributeHandler.getHeartBoost( player ) ).setStyle( XP.skillStyle.get( Skill.ENDURANCE ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.reach" );
        text.add( new TranslationTextComponent( "pmmo.reachBonus", DP.dpSoft( AttributeHandler.getReachBoost( player ) ) ).setStyle( XP.skillStyle.get( Skill.BUILDING ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

//        text = new ArrayList<>();
//        entryTitle = new TranslationTextComponent( "pmmo.respiration" );
//        text.add( new TranslationTextComponent( "pmmo.respirationBonus", getRespirationBonus( player ) ).setStyle( XP.skillStyle.get( Skill.SWIMMING ) ) );
//        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.underwaterNightVision" );
        text.add( new TranslationTextComponent( Skill.SWIMMING.getLevelDecimal( player ) >= Config.getConfig( "nightvisionUnlockLevel" ) ? "pmmo.unlocked" : "pmmo.locked" ).setStyle( XP.skillStyle.get( Skill.SWIMMING ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.rareFishPool" );
        text.add( new TranslationTextComponent( "pmmo.fishPoolChance", DP.dpSoft( FishedHandler.getFishPoolChance( player ) ) ).setStyle( XP.skillStyle.get( Skill.FISHING ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        ItemStack itemStack;
        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.xpBonuses" );
        PlayerInventory inv = player.inventory;

        //Helm
        itemStack = inv.getStackInSlot( 39 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TranslationTextComponent( itemStack.getTranslationKey() ) );
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
                text.add( new TranslationTextComponent( itemStack.getTranslationKey() ) );
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
                text.add( new TranslationTextComponent( itemStack.getTranslationKey() ) );
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
                text.add( new TranslationTextComponent( itemStack.getTranslationKey() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        itemStack = player.getHeldItemOffhand();
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TranslationTextComponent( itemStack.getTranslationKey() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        itemStack = player.getHeldItemMainhand();
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, true );
            if( map.size() > 0 )
            {
                text.add( new TranslationTextComponent( itemStack.getTranslationKey() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        if( Curios.isLoaded() )
        {

            Collection<IItemHandler> curiosItems = Curios.getCurios(player).collect(Collectors.toSet());

            for( IItemHandler value : curiosItems )
            {
                for (int i = 0; i < value.getSlots(); i++)
                {
                    addXpMapEntryAsText( text, XP.getStackXpBoosts( value.getStackInSlot(i), true ) );
                }
            };
        }

        map = XP.getDimensionBoosts( "" + player.world.getWorldType().getId() );
        if( map.size() > 0 )
        {
            text.add( new TranslationTextComponent( "pmmo.dimension" ) );
            addXpMapEntryAsText( text, map );    //Dimension
        }
        map = XP.getBiomeBoosts( player );
        if( map.size() > 0 )
        {
            text.add( new TranslationTextComponent( "pmmo.biome" ) );
            addXpMapEntryAsText( text, map );    //Biome
        }
        for( Map.Entry<String, Map<Skill, Double>> outterEntry : Config.getXpBoostsMap( player ).entrySet() )
        {
            text.add( new TranslationTextComponent( outterEntry.getKey() ) );
            addXpMapEntryAsText( text, NBTHelper.MapSkillKeyToString( outterEntry.getValue() ) );    //Biome
        }
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );        scrollPanel = new StatsScrollPanel( Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
        addButton(exitButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(  1 );

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString(  font, title.getString(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString(  font, title.getString(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( mouseX, mouseY, partialTicks );
        super.render( mouseX, mouseY, partialTicks );
    }

    @Override
    public void renderBackground( int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient( 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this ));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit(  x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        scrollPanel.mouseScrolled(mouseX, mouseY, scroll);
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

        scrollPanel.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY) ;
        return super.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
    }

}
