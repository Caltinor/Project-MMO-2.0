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
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

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

    public static void addXpMapEntryAsText( List<IFormattableTextComponent> text, Map<String, Double> xpBoosts )
    {
        String skill;
        Style color;
        for( Map.Entry<String, Double> entry : xpBoosts.entrySet() )
        {
            skill = entry.getKey();
            color = Skill.getSkillStyle( skill );
            skill = new TranslationTextComponent( "pmmo." + skill ).getString();
            text.add( new StringTextComponent( ( entry.getValue() < 0 ? " " : " +" ) + new TranslationTextComponent( "pmmo.levelDisplayPercentage", DP.dpSoft( entry.getValue() ), skill ).getString() ).setStyle( color ) );
        }
    }

    @Override
    protected void init()
    {
        statsEntries = new ArrayList<>();
        ArrayList<IFormattableTextComponent> text;
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
        text.add( new TranslationTextComponent( "pmmo.damageBonusMelee", Skill.getLevel( Skill.COMBAT.toString(), player ) / Config.forgeConfig.levelsPerDamageMelee.get() ).setStyle( Skill.getSkillStyle( Skill.COMBAT.toString() ) ) );
        text.add( new TranslationTextComponent( "pmmo.damageBonusArchery", Skill.getLevel( Skill.ARCHERY.toString(), player ) / Config.forgeConfig.levelsPerDamageArchery.get() ).setStyle( Skill.getSkillStyle( Skill.ARCHERY.toString() ) ) );
        text.add( new TranslationTextComponent( "pmmo.damageBonusMagic", Skill.getLevel( Skill.MAGIC.toString(), player ) / Config.forgeConfig.levelsPerDamageMagic.get() ).setStyle( Skill.getSkillStyle( Skill.MAGIC.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.speed" );
        text.add( new TranslationTextComponent( "pmmo.sprintSpeedBonus", DP.dpSoft( AttributeHandler.getSpeedBoostMultiplier( Skill.getLevel( Skill.AGILITY.toString(), player ) ) * 100D ) ).setStyle( Skill.getSkillStyle( Skill.AGILITY.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.jump" );
        text.add( new TranslationTextComponent( "pmmo.jumpBonusSprint", DP.dpSoft( JumpHandler.getSprintJumpBoost( player ) / 0.14D ) ).setStyle( Skill.getSkillStyle( Skill.AGILITY.toString() ) ) );
        text.add( new TranslationTextComponent( "pmmo.jumpBonusCrouch", DP.dpSoft( JumpHandler.getCrouchJumpBoost( player ) / 0.14D ) ).setStyle( Skill.getSkillStyle( Skill.AGILITY.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.fallSaveChance" );
        text.add( new TranslationTextComponent( "pmmo.fallSaveChancePercentage", DP.dpSoft( DamageHandler.getFallSaveChance( player ) ) ).setStyle( Skill.getSkillStyle( Skill.AGILITY.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.endurance" );
        text.add( new TranslationTextComponent( "pmmo.damageReductionPercentage", DP.dpSoft( DamageHandler.getEnduranceMultiplier( player ) * 100D ) ).setStyle( Skill.getSkillStyle( Skill.ENDURANCE.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.hearts" );
        text.add( new TranslationTextComponent( "pmmo.heartBonus", AttributeHandler.getHeartBoost( player ) / 2 ).setStyle( Skill.getSkillStyle( Skill.ENDURANCE.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.reach" );
        text.add( new TranslationTextComponent( "pmmo.reachBonus", DP.dpSoft( AttributeHandler.getReachBoost( player ) ) ).setStyle( Skill.getSkillStyle( Skill.BUILDING.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

//        text = new ArrayList<>();
//        entryTitle = new TranslationTextComponent( "pmmo.respiration" );
//        text.add( new TranslationTextComponent( "pmmo.respirationBonus", getRespirationBonus( player ) ).setStyle( Skill.getSkillStyle( Skill.SWIMMING.toString() ) ) );
//        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.underwaterNightVision" );
        text.add( new TranslationTextComponent( Skill.getLevelDecimal( Skill.SWIMMING.toString(), player ) >= Config.getConfig( "nightvisionUnlockLevel" ) ? "pmmo.unlocked" : "pmmo.locked" ).setStyle( Skill.getSkillStyle( Skill.SWIMMING.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.dualSalvage" );
        text.add( new TranslationTextComponent( Skill.getLevelDecimal( Skill.SMITHING.toString(), player ) >= Config.getConfig( "dualSalvageSmithingLevelReq" ) ? "pmmo.unlocked" : "pmmo.locked" ).setStyle( Skill.getSkillStyle( Skill.SMITHING.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslationTextComponent( "pmmo.rareFishPool" );
        text.add( new TranslationTextComponent( "pmmo.fishPoolChance", DP.dpSoft( FishedHandler.getFishPoolChance( player ) ) ).setStyle( Skill.getSkillStyle( Skill.FISHING.toString() ) ) );
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

            Collection<ICurioStacksHandler> curiosItems = Curios.getCurios(player).collect( Collectors.toSet() );

            for( ICurioStacksHandler value : curiosItems )
            {
                for (int i = 0; i < value.getSlots(); i++)
                {
                    addXpMapEntryAsText( text, XP.getStackXpBoosts( value.getStacks().getStackInSlot(i), true ) );
                }
            };
        }

        map = XP.getDimensionBoosts( "" + XP.getDimensionResLoc( player.world ) );
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
        for( Map.Entry<String, Map<String, Double>> outterEntry : Config.getXpBoostsMap( player ).entrySet() )
        {
            text.add( new TranslationTextComponent( outterEntry.getKey() ) );
            addXpMapEntryAsText( text, NBTHelper.MapStringKeyToString( outterEntry.getValue() ) );    //Biome
        }
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
        addButton(exitButton);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString( stack,  font, title.getString(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( stack,  font, title.getString(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( stack, mouseX, mouseY, partialTicks );
        super.render( stack, mouseX, mouseY, partialTicks );
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this, stack ));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit( stack,  x, y, 0, 0,  boxWidth, boxHeight );
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
