package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.curios.Curios;
import harmonised.pmmo.gui.ScreenshotHandler;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class PlayerTickHandler
{
    private static Map<UUID, Long> lastAward = new HashMap<>();
    private static Map<UUID, Long> lastVeinAward = new HashMap<>();
    private static Map<UUID, Long> lastInvCheck = new HashMap<>();
    public static boolean syncPrefs = false;

    public static void handlePlayerTick( TickEvent.PlayerTickEvent event )
    {
        PlayerEntity player = event.player;

        if( XP.isPlayerSurvival( player ) && player.isAlive() )
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
            if( !lastInvCheck.containsKey( uuid ) )
                lastInvCheck.put( uuid, System.nanoTime() );

            double gap = ( (System.nanoTime() - lastAward.get( uuid) ) / 1000000000D );
            double veinGap = ( (System.nanoTime() - lastVeinAward.get( uuid) ) / 1000000000D );
            double invGap = ( (System.nanoTime() - lastInvCheck.get( uuid) ) / 1000000000D );

            if( gap > 0.5 )
            {
                int swimLevel = Skill.SWIMMING.getLevel( player );
                int flyLevel = Skill.FLYING.getLevel( player );
                int agilityLevel = Skill.AGILITY.getLevel( player );
                int nightvisionUnlockLevel = Config.forgeConfig.nightvisionUnlockLevel.get();
                float swimAmp = EnchantmentHelper.getDepthStriderModifier( player );
                float speedAmp = 0;
                PlayerInventory inv = player.inventory;

                XP.checkBiomeLevelReq( player );

                if( !player.world.isRemote() )
                {
                    if( Curios.isLoaded() )
                    {
                        Curios.getCurios(player).forEach(value ->
                        {
                            for (int i = 0; i < value.getSlots(); i++)
                            {
                                XP.applyWornPenalty( player, value.getStackInSlot(i) );
                            }
                        });
                    }

                    if( !inv.getStackInSlot( 39 ).isEmpty() )	//Helm
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 39 ) );
                    if( !inv.getStackInSlot( 38 ).isEmpty() )	//Chest
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 38 ) );
                    if( !inv.getStackInSlot( 37 ).isEmpty() )	//Legs
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 37 ) );
                    if( !inv.getStackInSlot( 36 ).isEmpty() )	//Boots
                        XP.applyWornPenalty( player, player.inventory.getStackInSlot( 36 ) );
                }
////////////////////////////////////////////XP_STUFF//////////////////////////////////////////

                if( player.isPotionActive( Effects.SPEED ) )
                    speedAmp = player.getActivePotionEffect( Effects.SPEED ).getAmplifier() + 1;

                double swimAward = ( 3D + swimLevel    / 10.00D ) * gap * ( 1D + swimAmp / 4D );
                double flyAward  = ( 1D + flyLevel     / 30.77D ) * gap ;
                double runAward  = ( 1D + agilityLevel / 30.77D ) * gap * ( 1D + speedAmp / 4D );

                lastAward.replace( uuid, System.nanoTime() );
                Block waterBlock = Blocks.WATER;
                Block tallSeagrassBlock = Blocks.TALL_SEAGRASS;
                Block kelpBlock = Blocks.KELP_PLANT;
                BlockPos playerPos = XP.vecToBlock( player.getPositionVec() );
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

                if( !player.world.isRemote() )
                {
                    if( player.isSprinting() )
                    {
                        if( player.isInWater() && ( waterAbove || waterBelow ) )
                            XP.awardXp( player, Skill.SWIMMING, "swimming fast", swimAward * 1.25f, true, false );
                        else
                            XP.awardXp( player, Skill.AGILITY, "running", runAward, true, false );
                    }

                    if( player.isInWater() && ( waterAbove || waterBelow || player.areEyesInFluid( FluidTags.WATER ) ) )
                    {
                        if( !player.isSprinting() )
                            XP.awardXp( player, Skill.SWIMMING, "swimming", swimAward, true, false );
                    }
                    else if( player.isElytraFlying() )
                        XP.awardXp( player, Skill.FLYING, "flying", flyAward, true, false );

                    if( (player.getRidingEntity() instanceof BoatEntity) && player.isInWater() )
                        XP.awardXp( player, Skill.SWIMMING, "swimming in a boat", swimAward / 5, true, false );
                }
////////////////////////////////////////////ABILITIES//////////////////////////////////////////
//				if( !player.world.isRemote() )
//				{
//					Map<String, Double> abilitiesMap = getabilitiesMap( player );
//					if( !abilitiesMap.contains( "excavate" ) )
//						abilitiesMap.putDouble( "excavate", 0 );
//
//					abilitiesMap.putDouble( "excavate", abilitiesMap.getDouble( "excavate" ) + 1 );
//
//					System.out.println( abilitiesMap.getDouble( "excavate" ) );
//				}
            }

            if( !player.world.isRemote() )
            {
                if( veinGap > 0.25 )
                {
                    WorldTickHandler.updateVein( player, veinGap );
                    lastVeinAward.put( uuid, System.nanoTime() );
                }

                if( invGap > 1 )
                {

                    for( ItemStack itemStack : player.inventory.mainInventory )
                    {
                        tagOwnership( itemStack, uuid );
                    }
                    for( ItemStack itemStack : player.inventory.offHandInventory )
                    {
                        tagOwnership( itemStack, uuid );
                    }
                    lastInvCheck.put( uuid, System.nanoTime() );
                }
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
                    XPOverlayGUI.listOn = XPOverlayGUI.listWasOn;
                }
            }

            if( syncPrefs )
            {
                ClientHandler.syncPrefsToServer();
                syncPrefs = false;
            }
        }
    }

    public static void tagOwnership( ItemStack itemStack, UUID uuid )
    {
        if( !itemStack.isEmpty() )
        {
            CompoundNBT tag = itemStack.getTag();
            String regKey = itemStack.getItem().getRegistryName().toString();
            if( XP.hasElement( regKey, JType.INFO_SMELT ) || XP.hasElement( regKey, JType.XP_VALUE_SMELT ) || XP.hasElement( regKey, JType.INFO_COOK ) || XP.hasElement( regKey, JType.XP_VALUE_COOK ) || XP.hasElement( regKey, JType.INFO_BREW ) || XP.hasElement( regKey, JType.XP_VALUE_BREW ) )
            {
                if( tag == null )
                    itemStack.setTag( new CompoundNBT() );

                itemStack.getTag().putString( "lastOwner", uuid.toString() );
            }
            else if( tag != null && tag.contains( "lastOwner" ) )
                tag.remove( "lastOwner" );
        }
    }
}