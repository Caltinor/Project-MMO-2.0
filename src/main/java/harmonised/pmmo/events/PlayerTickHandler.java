package harmonised.pmmo.events;

import harmonised.pmmo.baubles.BaublesHandler;
import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTickHandler
{
    private static Map<UUID, Long> lastAward = new HashMap<>();
    private static Map<UUID, Long> lastVeinAward = new HashMap<>();
    public static boolean syncPrefs = false;

    public static void handlePlayerTick( TickEvent.PlayerTickEvent event )
    {
        EntityPlayer player = event.player;

        if( XP.isPlayerSurvival( player ) && player.isEntityAlive() )
        {
            UUID uuid = player.getUniqueID();

            if( player.isSprinting() )
                AttributeHandler.updateSpeed( player );
            else
                AttributeHandler.resetSpeed( player );

            if( !lastAward.containsKey( uuid ) )
                lastAward.put( uuid, System.nanoTime() );
            if( !lastVeinAward.containsKey( uuid ) )
                lastVeinAward.put( uuid, System.nanoTime() );

            double gap = ( (System.nanoTime() - lastAward.get( uuid) ) / 1000000000D );
            double veinGap = ( (System.nanoTime() - lastVeinAward.get( uuid) ) / 1000000000D );

            if( gap > 0.5 )
            {
                int swimLevel = Skill.SWIMMING.getLevel( player );
                int flyLevel = Skill.FLYING.getLevel( player );
                int agilityLevel = Skill.AGILITY.getLevel( player );
                int nightvisionUnlockLevel = FConfig.nightvisionUnlockLevel;
                double swimAmp = EnchantmentHelper.getDepthStriderModifier( player );
                double speedAmp = 0;
                InventoryPlayer inv = player.inventory;

                XP.checkBiomeLevelReq( player );

                if( !player.world.isRemote )
                {
                    if( BaublesHandler.isLoaded() )
                    {
                        for( ItemStack stack : BaublesHandler.getBaublesItems( player ) )
                        {
                            XP.applyWornPenalty( player, stack );
                        }
                    }

                    if( !inv.getStackInSlot( 39 ).isEmpty() )	//Helm
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 39 ) );
                    if( !inv.getStackInSlot( 38 ).isEmpty() )	//Chest
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 38 ) );
                    if( !inv.getStackInSlot( 37 ).isEmpty() )	//Legs
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 37 ) );
                    if( !inv.getStackInSlot( 36 ).isEmpty() )	//Boots
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 36 ) );
                    if( !player.getHeldItemMainhand().isEmpty() )
                        XP.applyEnchantmentUsePenalty( player, player.getHeldItemMainhand() );
                    if( !player.getHeldItemOffhand().isEmpty() )
                        XP.applyEnchantmentUsePenalty( player, player.getHeldItemOffhand() );
                }
////////////////////////////////////////////XP_STUFF//////////////////////////////////////////

                if( player.isPotionActive( MobEffects.SPEED ) )
                    speedAmp = player.getActivePotionEffect( MobEffects.SPEED ).getAmplifier() + 1;

                double swimAward = ( 3D + swimLevel    / 10.00D ) * gap * ( 1D + swimAmp / 4D );
                double flyAward  = ( 1D + flyLevel     / 30.77D ) * gap ;
                double runAward  = ( 1D + agilityLevel / 30.77D ) * gap * ( 1D + speedAmp / 4D );

                lastAward.replace( uuid, System.nanoTime() );
                Block waterBlock = Blocks.WATER;
                BlockPos playerPos = XP.vecToBlock( player.getPositionVector() );
                Block currBlock;
                boolean waterBelow = true;

                for( int i = -1; i <= 1; i++ )
                {
                    for( int j = -1; j <= 1; j++ )
                    {
                        currBlock = player.getEntityWorld().getBlockState( playerPos.down().east( i ).north( j ) ).getBlock();
                        if( !( currBlock.equals( waterBlock ) ) )
                            waterBelow = false;
                    }
                }

                boolean waterAbove = player.getEntityWorld().getBlockState( playerPos.up()   ).getBlock().equals( waterBlock );

                if( swimLevel >= nightvisionUnlockLevel && player.isInWater() && waterAbove )
                    player.addPotionEffect( new PotionEffect( MobEffects.NIGHT_VISION, 300, 0, false, false ) );

                if( !player.world.isRemote )
                {
                    EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
                    if( player.isSprinting() )
                    {
                        if( player.isInWater() && ( waterAbove || waterBelow ) )
                            XP.awardXp( serverPlayer, Skill.SWIMMING, "swimming fast", swimAward * 1.25f, true, false, false );
                        else
                            XP.awardXp( serverPlayer, Skill.AGILITY, "running", runAward, true, false, false );
                    }

                    if( player.isInWater() && ( waterAbove || waterBelow ) )
                    {
                        if( !player.isSprinting() )
                            XP.awardXp( serverPlayer, Skill.SWIMMING, "swimming", swimAward, true, false, false );
                    }
                    else if( player.isElytraFlying() )
                        XP.awardXp( serverPlayer, Skill.FLYING, "flying", flyAward, true, false, false );

                    if( ( player.getRidingEntity() instanceof EntityBoat ) && player.isInWater() )
                        XP.awardXp( serverPlayer, Skill.SWIMMING, "swimming in a boat", swimAward / 5, true, false, false );
                }
////////////////////////////////////////////ABILITIES//////////////////////////////////////////
            }

            if( !player.world.isRemote )
            {
                if( veinGap > 0.25 )
                {
                    WorldTickHandler.updateVein( player, veinGap );
                    lastVeinAward.put( uuid, System.nanoTime() );
                }
            }
        }

        if( player.world.isRemote )
        {
//            if( XPOverlayGUI.screenshots.size() > 0 )
//            {
//                for( String key : new HashSet<>( XPOverlayGUI.screenshots ) )
//                {
//                    ScreenshotHandler.takeScreenshot( key, "levelup" );
//                    XPOverlayGUI.screenshots.remove( key );
//                    XPOverlayGUI.listOn = XPOverlayGUI.listWasOn;
//                }
//            }
            //COUT SCREENSHOT

            if( syncPrefs )
            {
                ClientHandler.syncPrefsToServer();
                syncPrefs = false;
            }
        }
    }
}