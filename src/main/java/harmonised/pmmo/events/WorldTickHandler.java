package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageUpdateBoolean;
import harmonised.pmmo.network.MessageUpdateNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.VeinInfo;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.*;

public class WorldTickHandler
{
    public static Map<PlayerEntity, VeinInfo> activeVein;
    public static Map<PlayerEntity, ArrayList<BlockPos>> veinSet;
    private static double minVeinCost, minVeinHardness, levelsPerHardnessMining, levelsPerHardnessWoodcutting, levelsPerHardnessExcavation, levelsPerHardnessFarming, veinMaxBlocks, maxVeinCharge;
    private static int veinMaxDistance;
    private static final boolean veinWoodTopToBottom = Config.forgeConfig.veinWoodTopToBottom.get();
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

        if( event.world.getServer() == null )
            return;
        
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
                    cost = getVeinCost( veinState, veinPos, player );
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
                                    abilitiesTag.putDouble( "veinLeft", abilitiesTag.getDouble( "veinLeft" ) - cost );
                                    destroyBlock( world, veinPos, player, startItemStack );
                                }
                                else
                                {
                                    activeVein.remove( player );
                                    veinSet.remove( player );
                                    NetworkHandler.sendToPlayer( new MessageUpdateBoolean( false, 0 ), (ServerPlayerEntity) player );
                                }
                            }
                        }
                    }
                    else
                    {
                        activeVein.remove( player );
                        veinSet.remove( player );
                        NetworkHandler.sendToPlayer( new MessageUpdateBoolean( false, 0 ), (ServerPlayerEntity) player );
                    }
                }
                else
                {
                    activeVein.remove( player );
                    veinSet.remove( player );
                    NetworkHandler.sendToPlayer( new MessageUpdateBoolean( false, 0 ), (ServerPlayerEntity) player );
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
        double veinLeft = XP.getAbilitiesTag( player ).getDouble( "veinLeft" );
        double veinCost = getVeinCost( veinInfo.state, veinInfo.pos, player );
        String blockKey = veinInfo.state.getBlock().getRegistryName().toString();
        ArrayList<BlockPos> blockPosArrayList;

        if( !( canVeinGlobal( blockKey, player ) && canVeinDimension( blockKey, player )  ) )
            return;

        blockPosArrayList = getVeinShape( veinInfo, veinLeft, veinCost, player.isCreative(), false );

        if( blockPosArrayList.size() > 0 )
        {
            activeVein.put( player, veinInfo );
            veinSet.put( player, blockPosArrayList );
            NetworkHandler.sendToPlayer( new MessageUpdateBoolean( true, 0 ), (ServerPlayerEntity) player );
        }
    }

    public static boolean canVeinGlobal( String blockKey, PlayerEntity player )
    {
        if( player.isCreative() )
            return true;

        String dimensionKey = player.dimension.getRegistryName().toString();
        Map<String, Object> globalBlacklist = null;

        if( JsonConfig.data.get( "veinBlacklist" ).containsKey( "all_dimensions" ) )
            globalBlacklist = JsonConfig.data.get( "veinBlacklist" ).get( "all_dimensions" );

        return globalBlacklist == null || !globalBlacklist.containsKey(blockKey);
    }

    public static boolean canVeinDimension( String blockKey, PlayerEntity player )
    {
        if( player.isCreative() )
            return true;

        String dimensionKey = player.dimension.getRegistryName().toString();
        Map<String, Object> dimensionBlacklist = null;

        if( JsonConfig.data.get( "veinBlacklist" ).containsKey( dimensionKey ) )
            dimensionBlacklist = JsonConfig.data.get( "veinBlacklist" ).get( dimensionKey );

        return dimensionBlacklist == null || !dimensionBlacklist.containsKey(blockKey);
    }

    private static ArrayList<BlockPos> getVeinShape( VeinInfo veinInfo, double veinLeft, double veinCost, boolean isCreative, boolean isLooped )
    {
        Set<BlockPos> vein = new HashSet<>();
        ArrayList<BlockPos> outVein = new ArrayList<>();
        ArrayList<BlockPos> curLayer = new ArrayList<>();
        ArrayList<BlockPos> nextLayer = new ArrayList<>();
        BlockPos originPos = veinInfo.pos;
        BlockPos highestPos = originPos;
        curLayer.add( originPos );
        BlockPos curPos2;
        Block block = veinInfo.state.getBlock();
        Material material = veinInfo.state.getMaterial();
        String regKey = block.getRegistryName().toString();
        Skill skill = XP.getSkill( material );

        int yLimit = 1;

        if( JsonConfig.data.get( "blockSpecific" ).containsKey( regKey ) )
        {
            if( JsonConfig.data.get( "blockSpecific" ).get( regKey ).containsKey( "growsUpwards" ) )
                yLimit = 0;
        }

        while( ( isCreative || veinLeft > veinCost * vein.size() || ( veinWoodTopToBottom && !isLooped && skill.equals( Skill.WOODCUTTING  ) ) ) && vein.size() <= veinMaxBlocks )
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

                                    if( curPos2.getY() > highestPos.getY() )
                                        highestPos = new BlockPos( curPos2 );
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

        if( veinWoodTopToBottom && material.equals( Material.WOOD ) && !isLooped )
        {
            veinInfo.pos = highestPos;
            return getVeinShape( veinInfo, veinLeft, veinCost, isCreative, true );
        }

        return outVein;
    }

    public static double getVeinCost( BlockState state, BlockPos pos, PlayerEntity player )
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

        NetworkHandler.sendToPlayer( new MessageUpdateNBT( abilitiesTag, 1 ), (ServerPlayerEntity) player );
    }
}
