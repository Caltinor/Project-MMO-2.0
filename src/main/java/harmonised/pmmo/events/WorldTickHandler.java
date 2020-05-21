package harmonised.pmmo.events;

import com.mojang.authlib.GameProfile;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageUpdateNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.PMMOFakePlayer;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.VeinInfo;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.*;

public class WorldTickHandler
{
    public static Map<PlayerEntity, VeinInfo> activeVein;
    public static Map<PlayerEntity, ArrayList<BlockPos>> veinSet;
    private static double minVeinCost, levelsPerBlockMining, levelsPerBlockWoodcutting, levelsPerBlockExcavation, levelsPerBlockFarming, veinMaxBlocks;
    private static int veinMaxDistance;
//    public static long lastVeinUpdateTime = System.nanoTime();

    public static void refreshVein()
    {
        activeVein = new HashMap<>();
        veinSet = new HashMap<>();

        minVeinCost = Config.forgeConfig.minVeinCost.get();
        levelsPerBlockMining = Config.forgeConfig.levelsPerBlockMining.get();
        levelsPerBlockWoodcutting = Config.forgeConfig.levelsPerBlockWoodcutting.get();
        levelsPerBlockExcavation = Config.forgeConfig.levelsPerBlockExcavation.get();
        levelsPerBlockFarming = Config.forgeConfig.levelsPerBlockFarming.get();
        veinMaxDistance = (int) Math.floor( Config.forgeConfig.veinMaxDistance.get() );
        veinMaxBlocks = Config.forgeConfig.veinMaxBlocks.get();
    }

    public static void handleWorldTick( TickEvent.WorldTickEvent event )
    {
        int veinSpeed = (int) Math.floor( Config.forgeConfig.veinSpeed.get() );

        for( PlayerEntity player : event.world.getServer().getPlayerList().getPlayers() )
        {
            for( int i = 0; i < veinSpeed; i++ )
            {
                if( activeVein.containsKey( player ) && veinSet.get( player ).size() > 0 )
                {
                    VeinInfo veinInfo = activeVein.get(player);
                    World world = veinInfo.world;
                    ItemStack startItem = veinInfo.item;
                    BlockPos veinPos = veinSet.get( player ).get( 0 );
                    BlockState veinState = world.getBlockState(veinPos);
                    CompoundNBT abilitiesTag = XP.getAbilitiesTag( player );
                    double cost = getCost( veinState, veinPos, player );

                    if( ( abilitiesTag.getDouble( "veinLeft" ) >= cost || player.isCreative() ) && XP.isVeining.contains( player.getUniqueID() ) )
                    {
                        veinSet.get( player ).remove( 0 );

                        BlockEvent.BreakEvent veinEvent = new BlockEvent.BreakEvent( world, veinPos, veinState, player );
                        MinecraftForge.EVENT_BUS.post(veinEvent);

                        if ( !veinEvent.isCanceled() )
                        {
                            if( XP.isPlayerSurvival( player ) )
                            {
                                abilitiesTag.putDouble( "veinLeft", abilitiesTag.getDouble( "veinLeft" ) - cost );
                                NetworkHandler.sendToPlayer( new MessageUpdateNBT( XP.getAbilitiesTag( player ), "abilities" ), (ServerPlayerEntity) player );
                                if( startItem.getDamage() < startItem.getMaxDamage() && player.getHeldItemMainhand().equals( startItem ) )
                                    destroyBlock( world, veinPos, player, startItem );
                                else
                                {
                                    activeVein.remove( player );
                                    veinSet.remove( player );
                                }
                            }
                            else
                                world.destroyBlock(veinPos, false );
                        }
                    }
                    else
                    {
                        activeVein.remove( player );
                        veinSet.remove( player );
                    }
                }
                else
                {
                    activeVein.remove( player );
                    veinSet.remove( player );
                }
            }
        }
    }

    public static void destroyBlock( World world, BlockPos pos, PlayerEntity player, ItemStack toolUsed )
    {
        BlockState blockstate = world.getBlockState(pos);
        IFluidState ifluidstate = world.getFluidState(pos);
        world.playEvent(2001, pos, Block.getStateId(blockstate));

        TileEntity tileentity = blockstate.hasTileEntity() ? world.getTileEntity(pos) : null;
        Block.spawnDrops(blockstate, world, pos, tileentity, player, toolUsed );

        if( !toolUsed.getItem().equals( Items.AIR ) && world.setBlockState(pos, ifluidstate.getBlockState(), 3) )
            toolUsed.damageItem( 1, player, (a) -> a.sendBreakAnimation( Hand.MAIN_HAND ) );
    }

    public static boolean posIsAdjacent( BlockPos pos1, BlockPos pos2 )
    {
        for( int i = -1; i <= 1; i++ )
        {
            for( int j = -1; j <= 1; j++ )
            {
                for( int k = -1; k <= 1; k++ )
                {
                    if( pos1.up(i).north(j).east(k).equals( pos2 ) )
                        return true;
                }
            }
        }

        return false;
    }

    public static void scheduleVein(PlayerEntity player, VeinInfo veinInfo )
    {
        Skill skill = XP.getSkill( veinInfo.state.getMaterial() );
        boolean limitY = skill == Skill.FARMING && veinInfo.state.getBlockHardness( veinInfo.world, veinInfo.pos ) == 0;
        String dimensionKey = player.dimension.getRegistryName().toString();
        String blockKey = veinInfo.state.getBlock().getRegistryName().toString();
        ArrayList<BlockPos> blockPosArrayList;
        Map<String, Object> globalBlacklist = null;
        Map<String, Object> dimensionBlacklist = null;

        if( JsonConfig.data.get( "veinBlacklist" ).containsKey( "all_dimensions" ) )
            globalBlacklist = JsonConfig.data.get( "veinBlacklist" ).get( "all_dimensions" );

        if( JsonConfig.data.get( "veinBlacklist" ).containsKey( dimensionKey ) )
            dimensionBlacklist = JsonConfig.data.get( "veinBlacklist" ).get( dimensionKey );

        if( !player.isCreative() )
        {
            if( globalBlacklist != null && globalBlacklist.containsKey( blockKey ) )
                return;
            if( dimensionBlacklist != null && dimensionBlacklist.containsKey( blockKey ) )
                return;
        }
        long test;

//        test = System.currentTimeMillis();

        blockPosArrayList = getVeinShape( veinInfo, limitY );

//        System.out.println( System.currentTimeMillis() - test );
//        System.out.println( testBlockPosSet.size() );

//        blockPosArrayList.forEach( pos -> event.getWorld().destroyBlock( pos, false ) );

        if( blockPosArrayList.size() > 0 )
        {
            activeVein.put( player, veinInfo );
            veinSet.put( player, blockPosArrayList );
        }
    }

    private static ArrayList<BlockPos> getVeinShape( VeinInfo veinInfo, boolean limitY )
    {
        Set<BlockPos> vein = new HashSet<>();
        ArrayList<BlockPos> outVein = new ArrayList<>();
        ArrayList<BlockPos> curLayer = new ArrayList<>();
        ArrayList<BlockPos> nextLayer = new ArrayList<>();
        BlockPos originPos = veinInfo.pos;
        curLayer.add( originPos );
        BlockPos curPos2;
        Block block = veinInfo.state.getBlock();
        int yLimit = 1;
        if( limitY )
            yLimit = 0;

        while( vein.size() < veinMaxBlocks )
        {
            for( BlockPos curPos : curLayer )
            {
                if( curPos.withinDistance( originPos, veinMaxDistance ) )
                {
                    for( int i = -yLimit; i <= yLimit; i++ )
                    {
                        for( int j = -1; j <= 1; j++ )
                        {
                            for( int k = -1; k <= 1; k++ )
                            {
                                curPos2 = curPos.up(i).north(j).east(k);
                                if( !vein.contains( curPos2 ) && veinInfo.world.getBlockState( curPos2 ).getBlock().equals( block ) )
                                {
                                    vein.add( curPos2 );
                                    outVein.add( curPos2 );
                                    nextLayer.add( curPos2 );
                                }
                            }
                        }
                    }
                }
            }

            if( nextLayer.size() == 0 )
                return outVein;

            curLayer = nextLayer;
            nextLayer = new ArrayList<>();
        }

        return outVein;
    }

    private static boolean addMatch( World world, BlockPos tempPos, Block originBlock, PlayerEntity player, ArrayList<BlockPos> setIn )
    {
        if( setIn.contains( tempPos ) )
            return false;

        BlockState tempState = world.getBlockState( tempPos );
        if( tempState.getBlock().equals( originBlock ) )
        {
            BlockPos checkPos;

            for( int i = setIn.size() - 1; i >= 0; i-- )
            {
                checkPos = setIn.get( i );
                if( posIsAdjacent( tempPos, checkPos ) )
                {
                    if( setIn.size() >= veinMaxBlocks )
                        return false;

                    if( XP.isPlayerSurvival( player ) )
                    {
                        CompoundNBT abilitiesTag = XP.getAbilitiesTag( player );

                        double cost = getCost( originBlock.getDefaultState(), tempPos, player );

                        if( abilitiesTag.getDouble( "veinLeft" ) - ( cost * setIn.size() ) < 0 )
                            return false;
                    }

                    setIn.add( tempPos );
                    return true;
                }
            }
        }

        return false;
    }

    private static double getCost( BlockState state, BlockPos pos, PlayerEntity player )
    {
        Material material = state.getMaterial();
        double hardness = state.getBlockHardness( player.world, pos );
        Skill skill = XP.getSkill( material );
        double cost;

        switch( skill )
        {
            case MINING:
                cost = 100D / ( Skill.MINING.getLevel( player ) / levelsPerBlockMining );
                break;

            case WOODCUTTING:
                cost = 100D / ( Skill.WOODCUTTING.getLevel( player ) / levelsPerBlockWoodcutting );
                break;

            case EXCAVATION:
                cost = 100D / ( Skill.EXCAVATION.getLevel( player ) / levelsPerBlockExcavation );
                break;

            case FARMING:
                cost = 100D / ( Skill.FARMING.getLevel( player ) / levelsPerBlockFarming );
                break;

            default:
                if( !state.getBlock().equals( Blocks.AIR ) )
                    LogHandler.LOGGER.error( "WRONG SKILL AT VEIN COST: " + state.getBlock().getRegistryName() );
                return 0D;
        }

//        System.out.println( cost + " " + hardness );

        if( hardness > 5 )
            cost *= ( (hardness - 5D) / 5D);

        if( cost < minVeinCost )
            cost = minVeinCost;

        return cost;
    }

    public static void updateVein( PlayerEntity player, double gap )
    {
        if( !activeVein.containsKey( player ) )
        {
            CompoundNBT abilitiesTag = XP.getAbilitiesTag( player );

            if( !abilitiesTag.contains( "veinLeft" ) )
                abilitiesTag.putDouble( "veinLeft", 100D );

            double veinLeft = abilitiesTag.getDouble( "veinLeft" );

            if( veinLeft < 100 )
                abilitiesTag.putDouble( "veinLeft", veinLeft + gap );

            if( veinLeft > 100 )
                abilitiesTag.putDouble( "veinLeft", 100D );

            NetworkHandler.sendToPlayer( new MessageUpdateNBT( abilitiesTag, "abilities" ), (ServerPlayerEntity) player );
        }
    }
}
