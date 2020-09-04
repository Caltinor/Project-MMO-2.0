package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.*;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;

import java.util.*;

public class BlockBrokenHandler
{
    public static void handleBroken( BlockEvent.BreakEvent event )
    {
        PlayerEntity player = event.getPlayer();
        if( !( player instanceof FakePlayer ) )
            processReq( event );
    }

    private static void processReq( BlockEvent.BreakEvent event )
    {
        PlayerEntity player = event.getPlayer();
        BlockState blockState = event.getState();
        Block block = blockState.getBlock();
        World world = (World) event.getWorld();
        Material material = event.getState().getMaterial();

        Block blockAbove = world.getBlockState( event.getPos().up() ).getBlock();
        boolean passedBreakReq = true;

        if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( blockAbove.getRegistryName().toString() ) && blockAbove instanceof IPlantable)
            passedBreakReq = XP.checkReq( player, blockAbove.getRegistryName(), JType.REQ_BREAK );

        if( !passedBreakReq )
            block = blockAbove;
        else
            passedBreakReq = XP.checkReq( player, block.getRegistryName(), JType.REQ_BREAK );

        if( passedBreakReq )
        {
            if( XP.checkReq( player, player.getHeldItemMainhand().getItem().getRegistryName(), JType.REQ_TOOL ) )
            {
                processBroken( event );
                ChunkDataHandler.delPos( XP.getDimensionResLoc( world ), event.getPos() );
            }
        }
        else
        {
            int startLevel;

            if( XP.correctHarvestTool( material ).equals( "axe" ) )
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToChop", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToChop", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
            }
            else if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( blockAbove.getRegistryName().toString() ) || block instanceof IPlantable )
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToHarvest", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToHarvest", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
            }
            else
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToBreak", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToBreak", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
            }

            for( Map.Entry<String, Object> entry : JsonConfig.data.get( JType.REQ_BREAK ).get( block.getRegistryName().toString() ).entrySet() )
            {
                startLevel = XP.getLevel( Skill.getSkill( entry.getKey() ), player );

                double entryValue = 1;
                if( entry.getValue() instanceof Double )
                    entryValue = (double) entry.getValue();

                if( startLevel < entryValue )
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.levelDisplay", "pmmo." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 2 ), (ServerPlayerEntity) player );
                else
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.levelDisplay", "pmmo." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 1 ), (ServerPlayerEntity) player );
            }

            event.setCanceled( true );
        }
    }

    private static void processBroken( BlockEvent.BreakEvent event )
    {
        BlockState state = event.getState();
        Block block = state.getBlock();
        String regKey = block.getRegistryName().toString();
        World world = (World) event.getWorld();
        PlayerEntity player = event.getPlayer();

        boolean veiningAllowed = Config.config.containsKey("veiningAllowed") && Config.config.get("veiningAllowed") != 0;

        if( XP.isVeining.contains( player.getUniqueID() ) && veiningAllowed && !WorldTickHandler.activeVein.containsKey( player ) )
            WorldTickHandler.scheduleVein( player, new VeinInfo( world, state, event.getPos(), player.getHeldItemMainhand() ) );

        if( player.isCreative() )
            return;

        Material material = event.getState().getMaterial();
        double blockHardnessLimitForBreaking = Config.forgeConfig.blockHardnessLimitForBreaking.get();
        boolean wasPlaced = ChunkDataHandler.checkPos( XP.getDimensionResLoc( world ), event.getPos() ) != null;
        ItemStack toolUsed = player.getHeldItemMainhand();
        String skill = XP.getSkill( material ).name().toLowerCase();
//			String regKey = block.getRegistryName().toString();
        double hardness = state.getBlockHardness( event.getWorld(), event.getPos() );
        if( hardness > blockHardnessLimitForBreaking )
            hardness = blockHardnessLimitForBreaking;

        boolean isEffective = true;

//        for( ToolType toolType : player.getHeldItemMainhand().getToolTypes() )
//        {
//            ToolType blockToolType = block.getHarvestTool( state );
//
//            if( toolType == blockToolType )
//            {
//                isEffective = true;
//                break;
//            }
//        }

        String awardMsg = "";
        Map<String, Double> award = new HashMap<>();
        award.put( skill, hardness );

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( player.getHeldItemMainhand() );
        int fortune = 0;
        if( enchants.get( Enchantments.FORTUNE ) != null )
            fortune = enchants.get( Enchantments.FORTUNE );

        List<ItemStack> drops;

        if( world instanceof ServerWorld )
        {
            LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                    .withRandom(world.rand)
                    .withParameter( LootParameters.ORIGIN, player.getPositionVec() )
                    .withParameter( LootParameters.TOOL, toolUsed )
                    .withParameter( LootParameters.THIS_ENTITY, player )
                    .withNullableParameter( LootParameters.BLOCK_ENTITY, world.getTileEntity( event.getPos() ) );
            if (fortune > 0)
            {
                builder.withLuck(fortune);
            }
            drops = block.getDrops( event.getState(), builder );
        }
        else
            drops = new ArrayList<>();

        if( JsonConfig.data.get( JType.BLOCK_SPECIFIC ).containsKey( regKey ) && JsonConfig.data.get( JType.BLOCK_SPECIFIC ).get( regKey ).containsKey( "growsUpwards" ) ) //Handle Upwards Growing Plants
        {
            Block baseBlock = event.getState().getBlock();
            BlockPos baseBlockPos = event.getPos();

            double extraChance = XP.getExtraChance( player, block.getRegistryName(), JType.INFO_PLANT ) / 100;
            int rewardable, guaranteedDrop, extraDrop, totalDrops, guaranteedDropEach;
            rewardable = extraDrop = guaranteedDrop = totalDrops = 0;

            guaranteedDropEach = (int) Math.floor( extraChance );
            extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;

            int height = 0;
            BlockPos curBlockPos = new BlockPos( baseBlockPos.getX(), baseBlockPos.getY() + height, baseBlockPos.getZ() );
            BlockPos nextPos;
            Block nextBlock;
            block =  world.getBlockState( curBlockPos ).getBlock();
            boolean correctBlock = block.equals( baseBlock );
            while( correctBlock )
            {
                wasPlaced = ChunkDataHandler.checkPos( XP.getDimensionResLoc( world ), curBlockPos ) != null;
                if( !wasPlaced )
                {
                    rewardable++;
                    guaranteedDrop += guaranteedDropEach;

                    if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
                        extraDrop++;
                }
                height++;
                nextPos = new BlockPos( baseBlockPos.getX(), baseBlockPos.getY() + height, baseBlockPos.getZ() );
                nextBlock = world.getBlockState( nextPos ).getBlock();
                if( nextBlock.equals( baseBlock ) )
                {
                    curBlockPos = nextPos;
                    block = nextBlock;
                }
                else
                    correctBlock = false;
            }

            int dropsLeft = guaranteedDrop + extraDrop;

            if( dropsLeft > 0 )
            {
                XP.dropItems( dropsLeft, block.asItem(), world, event.getPos() );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + dropsLeft, drops.get( 0 ).getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
            }

            totalDrops = rewardable + dropsLeft;
            award = XP.addMaps( award, XP.multiplyMap( XP.getXp( baseBlock.getRegistryName(), JType.XP_VALUE_BREAK ), totalDrops ) );

            awardMsg = "removing " + height + " + " + ( guaranteedDrop + extraDrop ) + " extra";
        }
        else if( ( material.equals( Material.PLANTS ) || material.equals( Material.OCEAN_PLANT ) || material.equals( Material.TALL_PLANTS ) ) && drops.size() > 0 ) //IS PLANT
        {
            ItemStack theDropItem = drops.get( 0 );
            int theDropCount = 0;

            for( ItemStack drop : drops )
            {
                theDropCount += drop.getCount();
            }

            int age = -1;
            int maxAge = -1;

            if( !wasPlaced )
                award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), theDropItem.getCount() ) );

            if( state.contains( BlockStateProperties.AGE_0_1 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_1 );
                maxAge = 1;
            }
            else if( state.contains( BlockStateProperties.AGE_0_2 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_2 );
                maxAge = 2;
            }
            else if( state.contains( BlockStateProperties.AGE_0_3 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_3 );
                maxAge = 3;
            }
            else if( state.contains( BlockStateProperties.AGE_0_5 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_5 );
                maxAge = 5;
            }
            else if( state.contains( BlockStateProperties.AGE_0_7 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_7 );
                maxAge = 7;
            }
            else if( state.contains( BlockStateProperties.AGE_0_15) )
            {
                age = state.get( BlockStateProperties.AGE_0_15 );
                maxAge = 15;
            }
            else if( state.contains( BlockStateProperties.AGE_0_25 ) )
            {
                age = state.get( BlockStateProperties.AGE_0_25 );
                maxAge = 25;
            }
            else if( state.contains( BlockStateProperties.PICKLES_1_4 ) )
            {
                age = state.get( BlockStateProperties.PICKLES_1_4 );
                maxAge = 4;
                if( wasPlaced )
                    return;
            }

            if( age == maxAge && age >= 0 || block instanceof SeaPickleBlock)
            {
                award = new HashMap<>();
                award.put( skill, hardness );

                double extraChance = XP.getExtraChance( player, block.getRegistryName(), JType.INFO_PLANT ) / 100;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                }

                int totalDrops = theDropCount + totalExtraDrops;

                award = XP.multiplyMap( XP.addMaps( award, XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ) ), totalDrops );
                awardMsg = "harvesting " + ( theDropCount) + " + " + totalExtraDrops + " crops";
            }
            else if( !wasPlaced )
                awardMsg = "breaking a plant";
        }
        else if( XP.getExtraChance( player, block.getRegistryName(), JType.INFO_ORE ) > 0 )		//IS ORE
        {
            boolean isSilk = enchants.get( Enchantments.SILK_TOUCH ) != null;
            boolean noDropOre = false;
            if( drops.size() > 0 )
                noDropOre = block.asItem().equals( drops.get(0).getItem() );

            if( !wasPlaced && !isSilk )
                award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), drops.get( 0 ).getCount() ) );

            if( noDropOre && !wasPlaced || !noDropOre && !isSilk )			//EXTRA DROPS
            {
                double extraChance = XP.getExtraChance( player, block.getRegistryName(), JType.INFO_ORE ) / 100;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                if( !noDropOre && wasPlaced )
                    award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), ( drops.get( 0 ).getCount() ) ) );

                awardMsg = "mining a block";

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                }

                award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), totalExtraDrops ) );
            }
            else
                awardMsg = "mining a block";
        }
        else if( XP.getExtraChance( player, block.getRegistryName(), JType.INFO_LOG ) > 0 && isEffective )
        {
            if( !wasPlaced )			//EXTRA DROPS
            {
                double extraChance = XP.getExtraChance( player, block.getRegistryName(), JType.INFO_LOG ) / 100D;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                }

                award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), ( drops.get( 0 ).getCount() + totalExtraDrops ) ) );

                awardMsg = "cutting a block";
            }
            else
                awardMsg = "cutting a block";
        }
        else
        {
            if( !wasPlaced )
                award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), drops.size() ) );

            switch( XP.getSkill( material ) )
            {
                case MINING:
                    awardMsg = "mining a block";
                    break;

                case WOODCUTTING:
                    awardMsg = "cutting a block";
                    break;

                case EXCAVATION:
                    awardMsg = "digging a block";
                    break;

                case FARMING:
                    awardMsg = "harvesting";
                    break;

                default:
//						System.out.println( "INVALID SKILL ON BREAK" );
                    break;
            }
        }

        int gap = XP.getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), JType.REQ_TOOL );

        if( gap > 0 )
            player.getHeldItemMainhand().damageItem( gap - 1, player, (a) -> a.sendBreakAnimation(Hand.MAIN_HAND ) );

        if( XP.isPlayerSurvival( player ) )
        {
            for( String skillName : award.keySet() )
            {
                XP.awardXp( player, Skill.getSkill( skillName ), awardMsg, award.get( skillName ) / (gap + 1), false, false );
            }
        }
    }
}
