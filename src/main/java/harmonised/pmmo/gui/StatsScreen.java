package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.curios.Curios;
import harmonised.pmmo.events.DamageHandler;
import harmonised.pmmo.events.FishedHandler;
import harmonised.pmmo.events.JumpHandler;
import harmonised.pmmo.events.PlayerTickHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class StatsScreen extends Screen
{
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final List<GuiEventListener> children = Lists.newArrayList();
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    Window sr = minecraft.getWindow();
    Font font = minecraft.font;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y;
    private StatsScrollPanel scrollPanel;
    private List<StatsEntry> statsEntries;
    private UUID uuid;
    private JType jType = JType.STATS;

    public StatsScreen( UUID uuid, Component titleIn )
    {
        super( titleIn );
        this.uuid = uuid;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    public static void addXpMapEntryAsText( List<MutableComponent> text, Map<String, Double> xpBoosts )
    {
        String skill;
        Style color;
        for( Map.Entry<String, Double> entry : xpBoosts.entrySet() )
        {
            skill = entry.getKey();
            color = Skill.getSkillStyle( skill );
            skill = new TranslatableComponent( "pmmo." + skill ).getString();
            text.add( new TextComponent( ( entry.getValue() < 0 ? " " : " +" ) + new TranslatableComponent( "pmmo.levelDisplayPercentage", DP.dpSoft( entry.getValue() ), skill ).getString() ).setStyle( color ) );
        }
    }

    @Override
    protected void init()
    {
        statsEntries = new ArrayList<>();
        ArrayList<MutableComponent> text;
        Map<String, Double> map;

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().setScreen( new MainScreen( uuid, new TranslatableComponent( "pmmo.skills" ) ) );
        });

        Player player = Minecraft.getInstance().player;
        BaseComponent entryTitle;

        text = new ArrayList<>();

        entryTitle = new TranslatableComponent( "pmmo.damage" );
        text.add( new TranslatableComponent( "pmmo.damageBonusMelee", 100 * Skill.getLevel( Skill.COMBAT.toString(), player ) * Config.forgeConfig.damageBonusPercentPerLevelMelee.get() ).setStyle( Skill.getSkillStyle( Skill.COMBAT.toString() ) ) );
        text.add( new TranslatableComponent( "pmmo.damageBonusArchery", 100 * Skill.getLevel( Skill.ARCHERY.toString(), player ) * Config.forgeConfig.damageBonusPercentPerLevelArchery.get() ).setStyle( Skill.getSkillStyle( Skill.ARCHERY.toString() ) ) );
        text.add( new TranslatableComponent( "pmmo.damageBonusMagic", 100 * Skill.getLevel( Skill.MAGIC.toString(), player ) * Config.forgeConfig.damageBonusPercentPerLevelMagic.get() ).setStyle( Skill.getSkillStyle( Skill.MAGIC.toString() ) ) );
        text.add( new TranslatableComponent( "pmmo.damageBonusGunslinging", 100 * Skill.getLevel( Skill.MAGIC.toString(), player ) * Config.forgeConfig.damageBonusPercentPerLevelMagic.get() ).setStyle( Skill.getSkillStyle( Skill.GUNSLINGING.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.speed" );
        text.add( new TranslatableComponent( "pmmo.sprintSpeedBonus", DP.dpSoft( AttributeHandler.getSpeedBoostMultiplier( Skill.getLevel( Skill.AGILITY.toString(), player ) ) * 100D ) ).setStyle( Skill.getSkillStyle( Skill.AGILITY.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.jump" );
        text.add( new TranslatableComponent( "pmmo.jumpBonusSprint", DP.dpSoft( JumpHandler.getSprintJumpBoost( player ) / 0.14D ) ).setStyle( Skill.getSkillStyle( Skill.AGILITY.toString() ) ) );
        text.add( new TranslatableComponent( "pmmo.jumpBonusCrouch", DP.dpSoft( JumpHandler.getCrouchJumpBoost( player ) / 0.14D ) ).setStyle( Skill.getSkillStyle( Skill.AGILITY.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.fallSaveChance" );
        text.add( new TranslatableComponent( "pmmo.fallSaveChancePercentage", DP.dpSoft( DamageHandler.getFallSaveChance( player ) ) ).setStyle( Skill.getSkillStyle( Skill.AGILITY.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.endurance" );
        text.add( new TranslatableComponent( "pmmo.damageReductionPercentage", DP.dpSoft( DamageHandler.getEnduranceMultiplier( player ) * 100D ) ).setStyle( Skill.getSkillStyle( Skill.ENDURANCE.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.hearts" );
        text.add( new TranslatableComponent( "pmmo.heartBonus", AttributeHandler.getHeartBoost( player ) / 2 ).setStyle( Skill.getSkillStyle( Skill.ENDURANCE.toString() ) ) );
        double hpRegenTime = PlayerTickHandler.getHpRegenTime( player );
        if( hpRegenTime < Double.POSITIVE_INFINITY )
            text.add( new TranslatableComponent( "pmmo.halfHeartRegenerationSeconds", DP.dpSoft( 60D / hpRegenTime ) ).setStyle( Skill.getSkillStyle( Skill.ENDURANCE.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.reach" );
        text.add( new TranslatableComponent( "pmmo.reachBonus", DP.dpSoft( AttributeHandler.getReachBoost( player ) ) ).setStyle( Skill.getSkillStyle( Skill.BUILDING.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

//        text = new ArrayList<>();
//        entryTitle = new TranslatableComponent( "pmmo.respiration" );
//        text.add( new TranslatableComponent( "pmmo.respirationBonus", getRespirationBonus( player ) ).setStyle( Skill.getSkillStyle( Skill.SWIMMING.toString() ) ) );
//        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.underwaterNightVision" );
        text.add( new TranslatableComponent( Skill.getLevelDecimal( Skill.SWIMMING.toString(), player ) >= Config.getConfig( "nightvisionUnlockLevel" ) ? "pmmo.unlocked" : "pmmo.locked" ).setStyle( Skill.getSkillStyle( Skill.SWIMMING.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.dualSalvage" );
        text.add( new TranslatableComponent( Skill.getLevelDecimal( Skill.SMITHING.toString(), player ) >= Config.getConfig( "dualSalvageSmithingLevelReq" ) ? "pmmo.unlocked" : "pmmo.locked" ).setStyle( Skill.getSkillStyle( Skill.SMITHING.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.rareFishPool" );
        text.add( new TranslatableComponent( "pmmo.fishPoolChance", DP.dpSoft( FishedHandler.getFishPoolChance( player ) * 100D ) ).setStyle( Skill.getSkillStyle( Skill.FISHING.toString() ) ) );
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );

        ItemStack itemStack;
        text = new ArrayList<>();
        entryTitle = new TranslatableComponent( "pmmo.xpBonuses" );
        Inventory inv = player.getInventory();

        //Helm
        itemStack = inv.getItem( 39 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TranslatableComponent( itemStack.getDescriptionId() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        //Chest
        itemStack = inv.getItem( 38 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TranslatableComponent( itemStack.getDescriptionId() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        //Legs
        itemStack = inv.getItem( 37 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TranslatableComponent( itemStack.getDescriptionId() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        //Boots
        itemStack = inv.getItem( 36 );
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TranslatableComponent( itemStack.getDescriptionId() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        itemStack = player.getOffhandItem();
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, false );
            if( map.size() > 0 )
            {
                text.add( new TranslatableComponent( itemStack.getDescriptionId() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        itemStack = player.getMainHandItem();
        if( !itemStack.isEmpty() )
        {
            map = XP.getStackXpBoosts( itemStack, true );
            if( map.size() > 0 )
            {
                text.add( new TranslatableComponent( itemStack.getDescriptionId() ) );
                addXpMapEntryAsText( text, map );
            }
        }

        /*if( Curios.isLoaded() )
        {

            Collection<ICurioStacksHandler> curiosItems = Curios.getCurios(player).collect( Collectors.toSet() );

            for( ICurioStacksHandler value : curiosItems )
            {
                for (int i = 0; i < value.getSlots(); i++)
                {
                    addXpMapEntryAsText( text, XP.getStackXpBoosts( value.getStacks().getStackInSlot(i), true ) );
                }
            };
        }*/

        map = XP.getDimensionBoosts( "" + XP.getDimResLoc( player.level ) );
        if( map.size() > 0 )
        {
            text.add( new TranslatableComponent( "pmmo.dimension" ) );
            addXpMapEntryAsText( text, map );    //Dimension
        }
        map = XP.getBiomeBoosts( player );
        if( map.size() > 0 )
        {
            text.add( new TranslatableComponent( "pmmo.biome" ) );
            addXpMapEntryAsText( text, map );    //Biome
        }
        for( Map.Entry<String, Map<String, Double>> outterEntry : APIUtils.getXpBoostsMap( player ).entrySet() )
        {
            text.add( new TranslatableComponent( outterEntry.getKey() ) );
            addXpMapEntryAsText( text, NBTHelper.mapStringKeyToString( outterEntry.getValue() ) );    //Biome
        }
        statsEntries.add( new StatsEntry( 0, 0, entryTitle, text ) );
        scrollPanel = new StatsScrollPanel( new PoseStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
        addWidget(exitButton);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );

        if( font.width( title.getString() ) > 220 )
            drawCenteredString( stack,  font, title.getString(), sr.getGuiScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( stack,  font, title.getString(), sr.getGuiScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( stack, mouseX, mouseY, partialTicks );
        super.render( stack, mouseX, mouseY, partialTicks );
    }

    @Override
    public void renderBackground( PoseStack stack, int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this, stack ));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindForSetup( box );

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
