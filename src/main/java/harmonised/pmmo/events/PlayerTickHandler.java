package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.curios.Curios;
import harmonised.pmmo.gui.ScreenshotHandler;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.PMMOPoseSetter;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class PlayerTickHandler
{
    private static Map<UUID, Long> lastAward = new HashMap<>();

    public static void handlePlayerTick( TickEvent.PlayerTickEvent event )
    {
        PlayerEntity player = event.player;
        boolean crawlingAllowed;
        if( Config.getConfig( "crawlingAllowed" ) == 0 )
            crawlingAllowed = false;
        else
            crawlingAllowed = true;

        if( XP.isCrawling.contains( player.getUniqueID() ) && crawlingAllowed )
            PMMOPoseSetter.setPose( player, Pose.SWIMMING );

        if( XP.isPlayerSurvival( player ) && player.isAlive() )
        {
            UUID playerUUID = player.getUniqueID();

            if( player.isSprinting() )
                AttributeHandler.updateSpeed( player );
            else
                AttributeHandler.resetSpeed( player );

            if( !lastAward.containsKey( playerUUID ) )
                lastAward.put( playerUUID, System.currentTimeMillis() );

            long gap = System.currentTimeMillis() - lastAward.get( playerUUID );
            if( gap > 1000 )
            {
                int swimLevel = XP.getLevel( Skill.SWIMMING, player );
                int flyLevel = XP.getLevel( Skill.FLYING, player );
                int agilityLevel = XP.getLevel( Skill.AGILITY, player );
                int nightvisionUnlockLevel = Config.forgeConfig.nightvisionUnlockLevel.get();
                float swimAmp = EnchantmentHelper.getDepthStriderModifier( player );
                float speedAmp = 0;
                PlayerInventory inv = player.inventory;

                if( !player.world.isRemote() )
                    updateVein( player );

                XP.checkBiomeLevelReq( player );

                if( !player.world.isRemote() )
                {
                    if( Curios.isLoaded() )
                    {
                        Curios.getCurios(player).forEach(value ->
                        {
                            for (int i = 0; i < value.getSlots(); i++)
                            {
                                XP.applyWornPenalty( player, value.getStackInSlot(i).getItem() );
                            }
                        });
                    }

                    if( !inv.getStackInSlot( 39 ).isEmpty() )	//Helm
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 39 ).getItem() );
                    if( !inv.getStackInSlot( 38 ).isEmpty() )	//Chest
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 38 ).getItem() );
                    if( !inv.getStackInSlot( 37 ).isEmpty() )	//Legs
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 37 ).getItem() );
                    if( !inv.getStackInSlot( 36 ).isEmpty() )	//Boots
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 36 ).getItem() );
                }
////////////////////////////////////////////XP_STUFF//////////////////////////////////////////

                if( player.isPotionActive( Effects.SPEED ) )
                    speedAmp = player.getActivePotionEffect( Effects.SPEED ).getAmplifier() + 1;

                float swimAward = ( 3 + swimLevel    / 10.00f ) * ( gap / 1000f ) * ( 1 + swimAmp / 4 );
                float flyAward  = ( 1 + flyLevel     / 30.77f ) * ( gap / 1000f );
                float runAward  = ( 1 + agilityLevel / 30.77f ) * ( gap / 1000f ) * ( 1 + speedAmp / 4);

                lastAward.replace( playerUUID, System.currentTimeMillis() );
                Block waterBlock = Blocks.WATER;
                Block tallSeagrassBlock = Blocks.TALL_SEAGRASS;
                Block kelpBlock = Blocks.KELP_PLANT;
                BlockPos playerPos = player.getPosition();
                Block currBlock;
                boolean waterBelow = true;

                for( int i = -1; i <= 1; i++ )
                {
                    for( int j = -1; j <= 1; j++ )
                    {
                        currBlock = player.getEntityWorld().getBlockState( playerPos.down().east( i ).north( j ) ).getBlock();
                        if( !( currBlock.equals( waterBlock ) || currBlock.equals( tallSeagrassBlock ) || currBlock.equals( kelpBlock ) ) )
                            waterBelow = false;
                    }
                }

                boolean waterAbove = player.getEntityWorld().getBlockState( playerPos.up()   ).getBlock().equals( waterBlock );

                if( swimLevel >= nightvisionUnlockLevel && player.isInWater() && waterAbove )
                    player.addPotionEffect( new EffectInstance( Effects.NIGHT_VISION, 300, 0, false, false ) );

                if( player.isSprinting() )
                {
                    if( player.isInWater() && ( waterAbove || waterBelow ) )
                    {


                        XP.awardXp( player, Skill.SWIMMING, "swimming fast", swimAward * 1.25f, true, false );
                    }
                    else
                        XP.awardXp( player, Skill.AGILITY, "running", runAward, true, false );
                }

                if( player.isInWater() && ( waterAbove || waterBelow ) )
                {
                    if( !player.isSprinting() )
                        XP.awardXp( player, Skill.SWIMMING, "swimming", swimAward, true, false );
                }
                else if( player.isElytraFlying() )
                    XP.awardXp( player, Skill.FLYING, "flying", flyAward, true, false );

                if( (player.getRidingEntity() instanceof BoatEntity) && player.isInWater() )
                    XP.awardXp( player, Skill.SWIMMING, "swimming in a boat", swimAward / 5, true, false );
////////////////////////////////////////////ABILITIES//////////////////////////////////////////
//				if( !player.world.isRemote() )
//				{
//					CompoundNBT abilityTag = getAbilitiesTag( player );
//					if( !abilityTag.contains( "excavate" ) )
//						abilityTag.putDouble( "excavate", 0 );
//
//					abilityTag.putDouble( "excavate", abilityTag.getDouble( "excavate" ) + 1 );
//
//					System.out.println( abilityTag.getDouble( "excavate" ) );
//				}
            }
        }

        if( player.world.isRemote() )
        {
            if( XPOverlayGUI.screenshots.size() > 0 )
            {
                for( String key : new HashSet<>( XPOverlayGUI.screenshots ) )
                {
                    ScreenshotHandler.takeScreenshot( key, "levelup" );
                    XPOverlayGUI.screenshots.remove( key );
                    XPOverlayGUI.guiOn = XPOverlayGUI.guiWasOn;
                }
            }
        }
    }
    
    public static void updateVein( PlayerEntity player )
    {
        updateSpecificVein( player, Skill.MINING );
        updateSpecificVein( player, Skill.WOODCUTTING );
        updateSpecificVein( player, Skill.EXCAVATION );
        updateSpecificVein( player, Skill.FARMING );
    }
    
    private static void updateSpecificVein( PlayerEntity player, Skill skill )
    {
        CompoundNBT abilityTag = XP.getAbilitiesTag( player );

        String maxText = skill.name().toLowerCase() + "VeinMax";
        String leftText = skill.name().toLowerCase() + "VeinLeft";

        if( !abilityTag.contains( maxText ) )
            abilityTag.putInt( maxText, ( XP.getMaxVein( player, skill ) ) );

        int veinMax = abilityTag.getInt( maxText );

        if( !abilityTag.contains( leftText ) )
            abilityTag.putInt( leftText, veinMax );

        int veinLeft = abilityTag.getInt( leftText );

        if( veinLeft < veinMax )
            abilityTag.putInt( leftText, ++veinLeft );
        else if( veinLeft > veinMax )
            abilityTag.putInt( leftText, veinMax );
    }
}
