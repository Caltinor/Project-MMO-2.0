package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageUpdateBoolean;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.VeinInfo;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class WorldTickHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static Map<PlayerEntity, VeinInfo> activeVein;
    public static Map<PlayerEntity, ArrayList<BlockPos>> veinSet;
    private static double minVeinCost, minVeinHardness, levelsPerHardnessMining, levelsPerHardnessWoodcutting, levelsPerHardnessExcavation, levelsPerHardnessFarming, levelsPerHardnessCrafting, veinMaxBlocks, maxVeinCharge, exhaustionPerBlock;
    private static int veinMaxDistance;
//    public static long lastVeinUpdateTime = System.nanoTime();

    public static void refreshVein()
    {
        activeVein = new HashMap<>();
        veinSet = new HashMap<>();

        minVeinCost = Config.getConfig( "minVeinCost" );
        minVeinHardness = Config.getConfig( "minVeinHardness" );
        levelsPerHardnessMining = Config.getConfig( "levelsPerHardnessMining" );
        levelsPerHardnessWoodcutting = Config.getConfig( "levelsPerHardnessWoodcutting" );
        levelsPerHardnessExcavation = Config.getConfig( "levelsPerHardnessExcavation" );
        levelsPerHardnessFarming = Config.getConfig( "levelsPerHardnessFarming" );
        levelsPerHardnessCrafting = Config.getConfig( "levelsPerHardnessCrafting" );
        veinMaxDistance = (int) Math.floor( Config.forgeConfig.veinMaxDistance.get() );
        exhaustionPerBlock = Config.forgeConfig.exhaustionPerBlock.get();
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
        Map<String, Double> abilitiesMap;
        String regKey;
        Skill skill;
        double cost;
        boolean correctBlock, correctItem, correctHeldItem, fullyGrown, isOwner;
        UUID blockUUID, playerUUID;
        int age = -1, maxAge = -2;

        if( event.world.getServer() == null )
            return;

        for( PlayerEntity player : event.world.getServer().getPlayerList().getPlayers() )
        {
            playerUUID = player.getUniqueID();

            for( int i = 0; i < veinSpeed; i++ )
            {
                if( activeVein.containsKey( player ) && veinSet.get( player ).size() > 0 )
                {
                    veinInfo = activeVein.get(player);
                    world = veinInfo.world;
                    startItemStack = veinInfo.itemStack;
                    startItem = veinInfo.startItem;
                    veinPos = veinSet.get( player ).get( 0 );
                    veinState = world.getBlockState( veinPos );
                    abilitiesMap = Config.getAbilitiesMap( player );
                    regKey = veinState.getBlock().getRegistryName().toString();
                    cost = getVeinCost( veinState, veinPos, player );
                    correctBlock = world.getBlockState( veinPos ).getBlock().equals( veinInfo.state.getBlock() );
                    correctItem = !startItem.isDamageable() || ( startItemStack.getDamage() < startItemStack.getMaxDamage() );
                    correctHeldItem = player.getHeldItemMainhand().getItem().equals( startItem );
                    blockUUID = ChunkDataHandler.checkPos( world.dimension.getType().getRegistryName(), veinPos );
                    isOwner = blockUUID == null || blockUUID.equals( playerUUID );
                    skill = XP.getSkill( veinState );

                    if( skill.equals( Skill.FARMING ) && !( JsonConfig.data.get( JType.BLOCK_SPECIFIC ).containsKey( regKey ) && JsonConfig.data.get( JType.BLOCK_SPECIFIC ).get( regKey ).containsKey( "growsUpwards" ) ) )
                    {
                        if( veinState.has( BlockStateProperties.AGE_0_1 ) )
                        {
                            age = veinState.get( BlockStateProperties.AGE_0_1 );
                            maxAge = 1;
                        }
                        else if( veinState.has( BlockStateProperties.AGE_0_2 ) )
                        {
                            age = veinState.get( BlockStateProperties.AGE_0_2 );
                            maxAge = 2;
                        }
                        else if( veinState.has( BlockStateProperties.AGE_0_3 ) )
                        {
                            age = veinState.get( BlockStateProperties.AGE_0_3 );
                            maxAge = 3;
                        }
                        else if( veinState.has( BlockStateProperties.AGE_0_5 ) )
                        {
                            age = veinState.get( BlockStateProperties.AGE_0_5 );
                            maxAge = 5;
                        }
                        else if( veinState.has( BlockStateProperties.AGE_0_7 ) )
                        {
                            age = veinState.get( BlockStateProperties.AGE_0_7 );
                            maxAge = 7;
                        }
                        else if( veinState.has( BlockStateProperties.AGE_0_15 ) )
                        {
                            age = veinState.get( BlockStateProperties.AGE_0_15 );
                            maxAge = 15;
                        }
                        else if( veinState.has( BlockStateProperties.AGE_0_25 ) )
                        {
                            age = veinState.get( BlockStateProperties.AGE_0_25 );
                            maxAge = 25;
                        }
                        else if( veinState.has( BlockStateProperties.PICKLES_1_4 ) )
                        {
                            age = veinState.get( BlockStateProperties.PICKLES_1_4 );
                            maxAge = 4;
                        }

                        if( age >= 0 && age != maxAge )
                        {
                            veinSet.get( player ).remove( 0 );
                            return;
                        }
                    }

                    if( ( abilitiesMap.get( "veinLeft" ) >= cost || player.isCreative() ) && XP.isVeining.contains( player.getUniqueID() ) )
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
                                else if( correctItem && correctHeldItem && player.getFoodStats().getFoodLevel() > 0 )
                                {
                                    if( Config.forgeConfig.veiningOtherPlayerBlocksAllowed.get() || isOwner )
                                    {
                                        abilitiesMap.put("veinLeft", abilitiesMap.get("veinLeft") - cost);
                                        destroyBlock( world, veinPos, player, startItemStack );
                                        player.addExhaustion( (float) exhaustionPerBlock );
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
        world.playEvent(2001, pos, Block.getStateId(blockstate) );

        TileEntity tileentity = blockstate.hasTileEntity() ? world.getTileEntity(pos) : null;
        Block.spawnDrops(blockstate, world, pos, tileentity, player, toolUsed );

        if( world.setBlockState(pos, ifluidstate.getBlockState(), 3) && toolUsed.isDamageable() && !player.isCreative() )
            toolUsed.damageItem( 1, player, (a) -> a.sendBreakAnimation( Hand.MAIN_HAND ) );
    }

    public static void scheduleVein(PlayerEntity player, VeinInfo veinInfo )
    {
        double veinLeft = Config.getAbilitiesMap( player ).getOrDefault( "veinLeft", 0D );
        double veinCost = getVeinCost( veinInfo.state, veinInfo.pos, player );
        String blockKey = veinInfo.state.getBlock().getRegistryName().toString();
        ArrayList<BlockPos> blockPosArrayList;

        if( !( canVeinGlobal( blockKey, player ) && canVeinDimension( blockKey, player )  ) || !XP.checkReq( player, player.getHeldItemMainhand().getItem().getRegistryName(), JType.REQ_TOOL ) )
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

        Map<String, Double> globalBlacklist = null;

        if( JsonConfig.data.get( JType.VEIN_BLACKLIST ).containsKey( "all_dimensions" ) )
            globalBlacklist = JsonConfig.data.get( JType.VEIN_BLACKLIST ).get( "all_dimensions" );

        return globalBlacklist == null || !globalBlacklist.containsKey(blockKey);
    }

    public static boolean canVeinDimension( String blockKey, PlayerEntity player )
    {
        if( player.isCreative() )
            return true;

        World world = player.world;
        if( world == null )
            return true;

        ResourceLocation dimensionKey = world.dimension.getType().getRegistryName();
        if( dimensionKey == null )
            return true;

        Map<String, Double> dimensionBlacklist = null;

        if( JsonConfig.data.get( JType.VEIN_BLACKLIST ).containsKey( dimensionKey.toString() ) )
            dimensionBlacklist = JsonConfig.data.get( JType.VEIN_BLACKLIST ).get( dimensionKey.toString() );

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

        if( JsonConfig.data.get( JType.BLOCK_SPECIFIC ).containsKey( regKey ) )
        {
            if( JsonConfig.data.get( JType.BLOCK_SPECIFIC ).get( regKey ).containsKey( "growsUpwards" ) )
                yLimit = 0;
        }

        while( ( isCreative || veinLeft * 10 > veinCost * vein.size() || ( Config.forgeConfig.veinWoodTopToBottom.get() && !isLooped && skill.equals( Skill.WOODCUTTING  ) ) ) && vein.size() <= veinMaxBlocks )
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

        if( !isLooped )
        {
            if( ( Config.forgeConfig.veinWoodTopToBottom.get() && material.equals( Material.WOOD ) ) /* || block.equals( Blocks.SAND ) || block.equals( Blocks.GRAVEL ) */ )
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
//        double startHardness = state.getBlockHardness( player.world, pos );
        double hardness = state.getBlockHardness( player.world, pos );

        if( hardness < minVeinHardness )
            hardness = minVeinHardness;

//        if( startHardness == 0 )
//            hardness = 0;

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

            case CRAFTING:
                cost = hardness / ( Skill.CRAFTING.getLevel( player ) / levelsPerHardnessCrafting );
                break;

            default:
                if( !state.getBlock().equals( Blocks.AIR ) && !skill.equals( Skill.INVALID_SKILL ) )
                    LOGGER.error( "WRONG SKILL AT VEIN COST: " + state.getBlock().getRegistryName() + " " + skill.name() );
                return hardness;
        }

        if( cost < minVeinCost )
            cost = minVeinCost;

        return cost;
    }

    public static void updateVein( PlayerEntity player, double gap )
    {
        Map<String, Double> abilitiesMap = Config.getAbilitiesMap( player );

        if( !abilitiesMap.containsKey( "veinLeft" ) )
            abilitiesMap.put( "veinLeft", maxVeinCharge );

        double veinLeft = abilitiesMap.get( "veinLeft" );
        if( veinLeft < 0 )
            veinLeft = 0D;

        if( !activeVein.containsKey( player ) )
            veinLeft += Math.min( gap, 2 );

        if( veinLeft > maxVeinCharge )
            veinLeft = maxVeinCharge;

        abilitiesMap.put( "veinLeft", veinLeft );

        NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT(NBTHelper.mapStringToNbt( abilitiesMap ), 1 ), (ServerPlayerEntity) player );
    }
}