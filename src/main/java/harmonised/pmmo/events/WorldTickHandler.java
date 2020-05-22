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
import net.minecraft.item.Item;
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
    private static double minVeinCost, minVeinHardness, levelsPerHardnessMining, levelsPerHardnessWoodcutting, levelsPerHardnessExcavation, levelsPerHardnessFarming, veinMaxBlocks, maxVeinCharge;
    private static int veinMaxDistance;
//    public static long lastVeinUpdateTime = System.nanoTime();

    public static void refreshVein()
    {
        activeVein = new HashMap<>();
        veinSet = new HashMap<>();

        minVeinCost = Config.forgeConfig.minVeinCost.get();
        minVeinHardness = Config.forgeConfig.minVeinHardness.get();
        levelsPerHardnessMining = Config.forgeConfig.levelsPerHardnessMining.get();
        levelsPerHardnessWoodcutting = Config.forgeConfig.levelsPerHardnessWoodcutting.get();
        levelsPerHardnessExcavation = Config.forgeConfig.levelsPerHardnessExcavation.get();
        levelsPerHardnessFarming = Config.forgeConfig.levelsPerHardnessFarming.get();
        veinMaxDistance = (int) Math.floor( Config.forgeConfig.veinMaxDistance.get() );
        veinMaxBlocks = Config.forgeConfig.veinMaxBlocks.get();
        maxVeinCharge = Config.forgeConfig.maxVeinCharge.get();
    }

    public static void handleWorldTick( TickEvent.WorldTickEvent event )
    {
        int veinSpeed = (int) Math.floor( Config.forgeConfig.veinSpeed.get() );
        VeinInfo veinInfo;
        World world;
        ItemStack startItemStack;
        Item startItem;
        BlockPos veinPos;
        BlockState veinState;
        CompoundNBT abilitiesTag;
        double cost;
        boolean correctBlock, correctItem, correctHeldItem;
        
        
        for( PlayerEntity player : event.world.getServer().getPlayerList().getPlayers() )
        {
            for( int i = 0; i < veinSpeed; i++ )
            {
                if( activeVein.containsKey( player ) && veinSet.get( player ).size() > 0 )
                {
                    veinInfo = activeVein.get(player);
                    world = veinInfo.world;
                    startItemStack = veinInfo.itemStack;
                    startItem = veinInfo.startItem;
                    veinPos = veinSet.get( player ).get( 0 );
                    veinState = veinInfo.state;
                    abilitiesTag = XP.getAbilitiesTag( player );
                    cost = getCost( veinState, veinPos, player );
                    correctBlock = world.getBlockState( veinPos ).getBlock().equals( veinInfo.state.getBlock() );
                    correctItem = !startItem.isDamageable() || ( startItemStack.getDamage() < startItemStack.getMaxDamage() );
                    correctHeldItem = player.getHeldItemMainhand().getItem().equals( startItem );

                    if( ( abilitiesTag.getDouble( "veinLeft" ) >= cost || player.isCreative() ) && XP.isVeining.contains( player.getUniqueID() ) )
                    {
                        veinSet.get( player ).remove( 0 );

                        BlockEvent.BreakEvent veinEvent = new BlockEvent.BreakEvent( world, veinPos, veinState, player );
                        MinecraftForge.EVENT_BUS.post(veinEvent);

                        if ( !veinEvent.isCanceled() )
                        {
                            if( correctBlock )
                            {
                                if( player.isCreative() )
                                    world.destroyBlock(veinPos, false );
                                else if( correctItem && correctHeldItem )
                                {
                                    System.out.println( "happened" );
                                    abilitiesTag.putDouble( "veinLeft", abilitiesTag.getDouble( "veinLeft" ) - cost );
                                    destroyBlock( world, veinPos, player, startItemStack );
                                }
                                else
                                {
                                    activeVein.remove( player );
                                    veinSet.remove( player );
                                }

                            }
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

        if( world.setBlockState(pos, ifluidstate.getBlockState(), 3) && !player.isCreative() )
            toolUsed.damageItem( 1, player, (a) -> a.sendBreakAnimation( Hand.MAIN_HAND ) );
    }

    public static void scheduleVein(PlayerEntity player, VeinInfo veinInfo )
    {
        Skill skill = XP.getSkill( veinInfo.state.getMaterial() );
        double veinLeft = XP.getAbilitiesTag( player ).getDouble( "veinLeft" );
        double veinCost = getCost( veinInfo.state, veinInfo.pos, player );
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
//        long test;

//        test = System.currentTimeMillis();

        blockPosArrayList = getVeinShape( veinInfo, limitY, veinLeft, veinCost, player.isCreative() );

//        System.out.println( System.currentTimeMillis() - test );
//        System.out.println( testBlockPosSet.size() );

//        blockPosArrayList.forEach( pos -> event.getWorld().destroyBlock( pos, false ) );

        if( blockPosArrayList.size() > 0 )
        {
            activeVein.put( player, veinInfo );
            veinSet.put( player, blockPosArrayList );
        }
    }

    private static ArrayList<BlockPos> getVeinShape( VeinInfo veinInfo, boolean limitY, double veinLeft, double veinCost, boolean isCreative )
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

        while( ( isCreative || veinLeft > veinCost * vein.size() ) && vein.size() <= veinMaxBlocks )
        {
            for( BlockPos curPos : curLayer )
            {
                if( curPos.withinDistance( originPos, veinMaxDistance ) )
                {
                    for( int i = yLimit; i >= -yLimit; i-- )
                    {
                        for( int j = 1; j >= -1; j-- )
                        {
                            for( int k = 1; k >= -1; k-- )
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
                break;

            curLayer = nextLayer;
            nextLayer = new ArrayList<>();
        }

        return outVein;
    }

    private static double getCost( BlockState state, BlockPos pos, PlayerEntity player )
    {
        Material material = state.getMaterial();
        Skill skill = XP.getSkill( material );
        double cost;
        double hardness = state.getBlockHardness( player.world, pos );
        if( hardness < minVeinHardness )
            hardness = minVeinHardness;

        switch( skill )
        {
            case MINING:
                cost = hardness / ( (double) Skill.MINING.getLevel( player ) / levelsPerHardnessMining );
                break;

            case WOODCUTTING:
                cost = hardness / ( Skill.WOODCUTTING.getLevel( player ) / levelsPerHardnessWoodcutting );
                break;

            case EXCAVATION:
                cost = hardness / ( Skill.EXCAVATION.getLevel( player ) / levelsPerHardnessExcavation );
                break;

            case FARMING:
                cost = hardness / ( Skill.FARMING.getLevel( player ) / levelsPerHardnessFarming );
                break;

            default:
                if( !state.getBlock().equals( Blocks.AIR ) )
                    LogHandler.LOGGER.error( "WRONG SKILL AT VEIN COST: " + state.getBlock().getRegistryName() );
                return 0D;
        }

        if( cost < minVeinCost )
            cost = minVeinCost;

        return cost;
    }

    public static void updateVein( PlayerEntity player, double gap )
    {
//        System.out.println( XP.getAbilitiesTag( player ).getDouble( "veinLeft" ) );

        CompoundNBT abilitiesTag = XP.getAbilitiesTag( player );

        if( !abilitiesTag.contains( "veinLeft" ) )
            abilitiesTag.putDouble( "veinLeft", maxVeinCharge );

        double veinLeft = abilitiesTag.getDouble( "veinLeft" );
        if( veinLeft < 0 )
            veinLeft = 0D;

        if( !activeVein.containsKey( player ) )
            veinLeft += gap;

        if( veinLeft > maxVeinCharge )
            veinLeft = maxVeinCharge;

        abilitiesTag.putDouble( "veinLeft", veinLeft );

        NetworkHandler.sendToPlayer( new MessageUpdateNBT( abilitiesTag, "abilities" ), (ServerPlayerEntity) player );
    }
}
