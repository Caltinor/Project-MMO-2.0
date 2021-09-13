package harmonised.pmmo.events;

import harmonised.pmmo.api.events.TreasureEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.WorldText;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.*;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class BlockBrokenHandler
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Map<ResourceLocation, Map<BlockPos, Long>> cooldownTracker = new HashMap<>();

    public static void handleBroken( BlockEvent.BreakEvent event )
    {
        Player player = event.getPlayer();
        if( !( player instanceof FakePlayer ) )
            processReq( event );
        ChunkDataHandler.delPos( XP.getDimResLoc( (Level) event.getWorld() ), event.getPos() );
    }

    private static void processReq( BlockEvent.BreakEvent event )
    {
        Player player = event.getPlayer();
        BlockState blockState = event.getState();
        Block block = blockState.getBlock();
        BlockPos pos = event.getPos();
        Level world = (Level) event.getWorld();
        Block blockAbove = world.getBlockState( pos.above() ).getBlock();
        ResourceLocation dimResLoc = XP.getDimResLoc( (Level) event.getWorld() );

        boolean passedBreakReq = true;

        if( !cooldownTracker.containsKey( dimResLoc ) )
            cooldownTracker.put( dimResLoc, new HashMap<>() );
        Map<BlockPos, Long> dimCooldownTracker = cooldownTracker.get( dimResLoc );
        Long cooldownSince = dimCooldownTracker.get( pos );
        if( cooldownSince != null )
        {
            if( System.currentTimeMillis() - cooldownSince > 50 )
                dimCooldownTracker.remove( pos );
            else
                return;
        }
        else
            dimCooldownTracker.put( pos, System.currentTimeMillis() );

        if( XP.isHoldingDebugItemInOffhand( player ) )
            player.displayClientMessage( new TextComponent( block.getRegistryName().toString() ), false );

        if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( blockAbove.getRegistryName().toString() ) && blockAbove instanceof IPlantable)
            passedBreakReq = XP.checkReq( player, blockAbove.getRegistryName(), JType.REQ_BREAK );

        if( !passedBreakReq )
            block = blockAbove;
        else
            passedBreakReq = XP.checkReq( player, block.getRegistryName(), JType.REQ_BREAK );

        if( passedBreakReq )
        {
            if( XP.checkReq( player, player.getMainHandItem().getItem().getRegistryName(), JType.REQ_TOOL ) )
            {
                processBroken( event );
                ChunkDataHandler.delPos( XP.getDimResLoc( world ), pos );
            }
        }
        else
        {
            int startLevel;

            if( XP.getHarvestTool( blockState ).equals( "axe" ) )
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToChop", block.getDescriptionId(), "", true, 2 ), (ServerPlayer) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToChop", block.getDescriptionId(), "", false, 2 ), (ServerPlayer) player );
            }
            else if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( blockAbove.getRegistryName().toString() ) || block instanceof IPlantable )
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToHarvest", block.getDescriptionId(), "", true, 2 ), (ServerPlayer) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToHarvest", block.getDescriptionId(), "", false, 2 ), (ServerPlayer) player );
            }
            else
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToBreak", block.getDescriptionId(), "", true, 2 ), (ServerPlayer) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToBreak", block.getDescriptionId(), "", false, 2 ), (ServerPlayer) player );
            }

            for( Map.Entry<String, Double> entry : JsonConfig.data.get( JType.REQ_BREAK ).get( block.getRegistryName().toString() ).entrySet() )
            {
                startLevel = Skill.getLevel( entry.getKey(), player );

                double entryValue = entry.getValue();

                if( startLevel < entryValue )
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.levelDisplay", "pmmo." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 2 ), (ServerPlayer) player );
                else
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.levelDisplay", "pmmo." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 1 ), (ServerPlayer) player );
            }

            event.setCanceled( true );
        }
    }

    @SuppressWarnings("deprecation")
	private static void processBroken( BlockEvent.BreakEvent event )
    {
        BlockState state = event.getState();
        if( state.getMaterial().isLiquid() )
            return;
        Block block = state.getBlock();
        BlockPos pos = event.getPos();
        String regKey = block.getRegistryName().toString();
        BlockEntity tile = event.getWorld().getBlockEntity(event.getPos());
        final Map<String, Double> xpMap = tile == null ? XP.getXpBypass( block.getRegistryName(), JType.XP_VALUE_BREAK ) : XP.getXp( tile, JType.XP_VALUE_BREAK );
        Level world = (Level) event.getWorld();
        BlockEntity tileEntity = world.getBlockEntity( event.getPos() );
        if( tileEntity != null )
            tileEntity = BlockEntity.loadStatic(pos, state, tileEntity.serializeNBT() );
        boolean isRemote = world.isClientSide();
        Player player = event.getPlayer();
        boolean veiningAllowed = Config.getConfig( "veiningAllowed" ) != 0;

        /*if( !Util.isProduction() )
        {
            BlockState coalState = Blocks.COAL_BLOCK.defaultBlockState();
            BlockState glassState = Blocks.GLASS.defaultBlockState();
            int mainRadius = 10;

//            DrawUtil.drawToWorld( world, pos.up( mainRadius ), DrawUtil.getSphereSolid( mainRadius ), glassState );
//            DrawUtil.drawToWorld( world, pos.up( mainRadius ), DrawUtil.getCircleSolid( mainRadius ), glassState );
//            DrawUtil.drawToWorld( world, pos.up( mainRadius ), DrawUtil.getCircle( mainRadius ), glassState );

//            DrawUtil.drawToWorld( world, pos.up(), DrawUtil.getCircleSolid( mainRadius ), glassState );
//            DrawUtil.drawToWorld( world, pos, DrawUtil.getCircle( mainRadius ), coalState );
        }*/

        if( XP.isVeining.contains( player.getUUID() ) && veiningAllowed && !WorldTickHandler.activeVein.containsKey( player ) )
            WorldTickHandler.scheduleVein( player, new VeinInfo( world, state, event.getPos(), player.getMainHandItem() ) );

        if( !XP.isPlayerSurvival( player ) || isRemote )
            return;

        Material material = event.getState().getMaterial();
        double blockHardnessLimitForBreaking = Config.forgeConfig.blockHardnessLimitForBreaking.get();
        boolean wasPlaced = ChunkDataHandler.checkPos( world, event.getPos() ) != null;
        ItemStack toolUsed = player.getMainHandItem();
        String skill = XP.getSkill( state );
//			String regKey = block.getRegistryName().toString();
        double hardness = Math.min( blockHardnessLimitForBreaking, state.getDestroySpeed( event.getWorld(), event.getPos() ) );

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
        switch( XP.getSkill( state ) )
        {
            case "mining":
                awardMsg = "Mining";
                break;

            case "woodcutting":
                awardMsg = "Chopping";
                break;

            case "excavation":
                awardMsg = "Digging";
                break;

            case "farming":
                awardMsg = "Harvesting";
                break;

            default:
                awardMsg = "Breaking";
                break;
        }
        awardMsg += " " + block.getRegistryName();

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( player.getMainHandItem() );
        int fortune = 0;
        if( enchants.get( Enchantments.BLOCK_FORTUNE ) != null )
            fortune = enchants.get( Enchantments.BLOCK_FORTUNE );

        boolean dropsItself = false;

        List<ItemStack> noSilkDrops, drops = null;

        if( block.canHarvestBlock( state, world, player.blockPosition(), player ) )
        {
            try
            {
                if( world instanceof ServerLevel )
                {
                    LootContext.Builder builder = new LootContext.Builder((ServerLevel) world)
                            .withRandom(world.random)
                            .withParameter( LootContextParams.ORIGIN, player.position() )
                            .withParameter( LootContextParams.TOOL, toolUsed )
                            .withParameter( LootContextParams.THIS_ENTITY, player )
                            .withOptionalParameter( LootContextParams.BLOCK_ENTITY, tileEntity );
                    if (fortune > 0)
                    {
                        builder.withLuck(fortune);
                    }
                    drops = block.getDrops( event.getState(), builder );

                    if( EnchantmentHelper.getEnchantments( toolUsed ).containsKey( Enchantments.SILK_TOUCH ) )
                    {
                        ItemStack noEnchantTool = toolUsed.copy();
                        noEnchantTool.removeTagKey("Enchantments");

                        builder = new LootContext.Builder((ServerLevel) world)
                                .withRandom(world.random)
                                .withParameter( LootContextParams.ORIGIN, player.position() )
                                .withParameter( LootContextParams.TOOL, noEnchantTool )
                                .withParameter( LootContextParams.THIS_ENTITY, player )
                                .withOptionalParameter( LootContextParams.BLOCK_ENTITY, tileEntity );
                        ;
                        if (fortune > 0)
                        {
                            builder.withLuck(fortune);
                        }
                        noSilkDrops = block.getDrops( event.getState(), builder );
                        if( noSilkDrops.size() > 0 && noSilkDrops.get(0).getItem().equals( block.asItem() ) )
                            dropsItself = true;
                    }
                }
            }
            catch( Exception e )
            {
                LOGGER.error( e );
            }
        }

        if( drops == null )
            drops = new ArrayList<>();

        Map<String, Double> award = new HashMap<>();
        award.put( skill, hardness );

        int dropItemCount = 0;

        if( drops.size() > 0 )
        {
            dropItemCount = drops.get(0).getCount();
            if( drops.get(0).getItem().equals( block.asItem() ) )
                dropsItself = true;
        }

        if( !wasPlaced )
            XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( xpMap, Math.max( dropItemCount, 1 ) ) );

        ItemStack theDropItem = drops.size() > 0 ? drops.get( 0 ) : ItemStack.EMPTY;

        //PLANT
        if( JsonConfig.data.get( JType.BLOCK_SPECIFIC ).containsKey( regKey ) && JsonConfig.data.get( JType.BLOCK_SPECIFIC ).get( regKey ).containsKey( "growsUpwards" ) ) //Handle Upwards Growing Plants
        {
            Block baseBlock = event.getState().getBlock();
            BlockPos baseBlockPos = event.getPos();

            double extraChance = XP.getExtraChance( player.getUUID(), block.getRegistryName(), JType.INFO_PLANT, false ) / 100;
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
                wasPlaced = ChunkDataHandler.checkPos( world, curBlockPos ) != null;
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
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + dropsLeft, theDropItem.getDescriptionId(), true, 1 ), (ServerPlayer) player );
            }

            totalDrops = rewardable + dropsLeft;
            award.put( skill, hardness );
            XP.addMapsAnyDouble( award, xpMap );
            XP.multiplyMapAnyDouble( award, totalDrops );

            awardMsg = "removing " + height + " + " + ( guaranteedDrop + extraDrop ) + " extra " + block.getRegistryName();
        }
        else if( ( material.equals( Material.PLANT ) || material.equals( Material.WATER_PLANT ) || material.equals( Material.REPLACEABLE_PLANT ) ) && drops.size() > 0 ) //IS PLANT
        {
            award = new HashMap<>();
            award.put( skill, hardness );

            int totalExtraDrops;

//            for( ItemStack drop : drops )
//            {
//                dropItemCount += drop.getCount();
//            }

            int age = -1;
            int maxAge = -1;

            {   //age stuff
                if( state.hasProperty( BlockStateProperties.AGE_1 ) )
                {
                    age = state.getValue( BlockStateProperties.AGE_1 );
                    maxAge = 1;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_2 ) )
                {
                    age = state.getValue( BlockStateProperties.AGE_2 );
                    maxAge = 2;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_3 ) )
                {
                    age = state.getValue( BlockStateProperties.AGE_3 );
                    maxAge = 3;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_5 ) )
                {
                    age = state.getValue( BlockStateProperties.AGE_5 );
                    maxAge = 5;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_7 ) )
                {
                    age = state.getValue( BlockStateProperties.AGE_7 );
                    maxAge = 7;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_15 ) )
                {
                    age = state.getValue( BlockStateProperties.AGE_15 );
                    maxAge = 15;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_25 ) )
                {
                    age = state.getValue( BlockStateProperties.AGE_25 );
                    maxAge = 25;
                }
                else if( state.hasProperty( BlockStateProperties.PICKLES ) )
                {
                    age = state.getValue( BlockStateProperties.PICKLES );
                    maxAge = 4;
                    if( wasPlaced )
                        return;
                }
            }

            if( age == maxAge && age >= 0 || block instanceof SeaPickleBlock )
            {
                double extraChance = XP.getExtraChance( player.getUUID(), block.getRegistryName(), JType.INFO_PLANT, false ) / 100;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                totalExtraDrops = guaranteedDrop + extraDrop;

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getItem().getDescriptionId(), true, 1 ), (ServerPlayer) player );
                }

                awardMsg = "Harvesting " + ( dropItemCount ) + " + " + totalExtraDrops + " " + block.getRegistryName();
                XP.multiplyMapAnyDouble( XP.addMapsAnyDouble( award, xpMap ), dropItemCount + totalExtraDrops );
            }
            else if( !wasPlaced )
            {
                awardMsg = "Breaking " + block.getRegistryName();
                XP.multiplyMapAnyDouble( XP.addMapsAnyDouble( award, xpMap ), dropItemCount );
            }
        }

        //ORE
        if( XP.getExtraChance( player.getUUID(), block.getRegistryName(), JType.INFO_ORE, false ) > 0 )
        {
            award = new HashMap<>();
            award.put( skill, hardness );

            boolean isSilk = enchants.get( Enchantments.SILK_TOUCH ) != null;

            if( !wasPlaced && !isSilk )
                XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( tile == null ? XP.getXpBypass( block.getRegistryName() , JType.XP_VALUE_BREAK ) : XP.getXp( tile, JType.XP_VALUE_BREAK), theDropItem.getCount() ) );

            if( dropsItself && !wasPlaced || !dropsItself && !isSilk )			//EXTRA DROPS
            {
                double extraChance = XP.getExtraChance( player.getUUID(), block.getRegistryName(), JType.INFO_ORE, false ) / 100;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                if( !dropsItself && wasPlaced )
                    XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( tile == null ? XP.getXpBypass( block.getRegistryName() , JType.XP_VALUE_BREAK ) : XP.getXp( tile, JType.XP_VALUE_BREAK), ( theDropItem.getCount() ) ) );

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getItem().getDescriptionId(), true, 1 ), (ServerPlayer) player );
                }

                XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( tile == null ? XP.getXpBypass( block.getRegistryName() , JType.XP_VALUE_BREAK ) : XP.getXp( tile, JType.XP_VALUE_BREAK), totalExtraDrops ) );
            }

            awardMsg = "Mining " + block.getRegistryName();
        }

        //LOG
        //Dynamic Trees
        /*if( ProjectMMOMod.dynamicTreesLoaded && block instanceof BranchBlock )
        {
            BranchBlock branchBlock = (BranchBlock) block;
            MapSignal signal = branchBlock.analyse( state, world, pos, null, new MapSignal());
            NetVolumeNode volumeNet = new NetVolumeNode();
            branchBlock.analyse( state, world, pos, signal.localRootDir, new MapSignal(volumeNet));
            NetVolumeNode.Volume volume = volumeNet.getVolume();
            float volumeFloat = volume.getVolume();
            drops = branchBlock.getLogDrops( world, pos, branchBlock.getFamily().getSpeciesForLocation( world, pos ), volume );
            award = new HashMap<>();

            for( ItemStack itemStack : drops )
            {
                try
                {
                    int extraDrops;
                    ResourceLocation resLoc = itemStack.getItem().getRegistryName();
//                    Set<ResourceLocation> tags = block.getTags();
//                    for( ResourceLocation tag : tags )
//                    {
//                        String tagName = tag.toString();
//                        if( tagName.equals( "minecraft:logs" ) )
//                        {
//                            resLoc = itemStack.getItem().getRegistryName();
//                            break;
//                        }
//                    }

                    double extraChance = XP.getExtraChance( player.getUniqueID(), resLoc, JType.INFO_LOG, false );
                    extraDrops = (int) ( itemStack.getCount() * extraChance / 100D );
                    XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( XP.getXpBypass( resLoc, JType.XP_VALUE_BREAK ), volumeFloat + extraDrops ) );

                    if( extraDrops > 0 )
                    {
                        ItemStack extraDropStack = itemStack.copy();
                        extraDropStack.setCount( extraDrops );
                        XP.dropItemStack( extraDropStack, world, pos );
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + extraDrops, extraDropStack.getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                    }
                }
                catch( Exception e )
                {
                    LOGGER.error( e );
                }
            }
        }
        else*/ if( XP.getExtraChance( player.getUUID(), block.getRegistryName(), JType.INFO_LOG, false ) > 0 && isEffective )
        {
            if( !wasPlaced )			//EXTRA DROPS
            {
                award = new HashMap<>();
                award.put( skill, hardness );

                double extraChance = XP.getExtraChance( player.getUUID(), block.getRegistryName(), JType.INFO_LOG, false ) / 100D;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getItem().getDescriptionId(), true, 1 ), (ServerPlayer) player );
                }

                XP.multiplyMapAnyDouble( XP.addMapsAnyDouble( award, xpMap ), dropItemCount + totalExtraDrops );
            }

            awardMsg = "Chopping " + block.getRegistryName().toString();
        }

        //TREASURE
        if( JsonConfig.data2.get( JType.TREASURE ).containsKey( block.getRegistryName().toString() ) )
        {
            if( !wasPlaced )
            {
                Map<String, Map<String, Double>> treasurePool = JsonConfig.data2.get( JType.TREASURE ).get( block.getRegistryName().toString() );
                Map<String, Double> treasureItemMap;
                int excavationLevel = Skill.getLevel( Skill.EXCAVATION.toString(), player );
                double chance;

                for( Map.Entry<String, Map<String, Double>> treasureItem : treasurePool.entrySet() )
                {
                    boolean foundTreasure = false;
                    treasureItemMap = treasureItem.getValue();
                    chance = getTreasureItemChance( excavationLevel, treasureItemMap );

                    if( Math.ceil( Math.random() * 10000 ) <= chance * 100 )
                    {
                        Item item = XP.getItem( treasureItem.getKey() );

                        int minCount = (int) Math.floor( treasureItemMap.get( "minCount" ) );
                        int maxCount = (int) Math.floor( treasureItemMap.get( "maxCount" ) );
                        int count;
                        count = (int) Math.floor( (Math.random() * maxCount) + minCount );
                        Map<String, Double> treasureAward = new HashMap<>();
                        treasureAward.put( Skill.EXCAVATION.toString(), treasureItemMap.get( "xpPerItem" ) * count );
                        BlockPos treasurePos = event.getPos();

                        ItemStack itemStack = new ItemStack( item, count );
                        TreasureEvent treasureEvent = new TreasureEvent( player, treasurePos, itemStack, treasureAward );
                        if( MinecraftForge.EVENT_BUS.post( treasureEvent ) )
                            return;

                        treasurePos = treasureEvent.getBlockPos();
                        itemStack = treasureEvent.getItemStack();
                        treasureAward = treasureEvent.getAward();

                        XP.dropItemStack( itemStack, world, treasurePos );
                        foundTreasure = true;

                        XP.addMapsAnyDouble( award, treasureAward );

                        player.displayClientMessage( new TranslatableComponent( "pmmo.youFoundTreasureItem", count, new TranslatableComponent( itemStack.getDescriptionId() ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                        LOGGER.debug( player.getDisplayName().getString() + " found Treasure! " + count + " " + treasureItem.getKey() + " " + event.getPos() );
                    }

                    if( foundTreasure )
                        player.displayClientMessage( new TranslatableComponent( "pmmo.youFoundTreasure" ).setStyle( XP.textStyle.get( "green" ) ), true );
                }
            }
        }

        int gap = XP.getSkillReqGap( player, player.getMainHandItem().getItem().getRegistryName(), JType.REQ_TOOL );

        if( gap > 0 )
            player.getMainHandItem().hurtAndBreak( gap - 1, player, (a) -> a.broadcastBreakEvent(InteractionHand.MAIN_HAND ) );

        ResourceLocation dimResLoc = XP.getDimResLoc( world );

        for( String awardSkillName : award.keySet() )
        {
            double xp = award.get( awardSkillName ) / (gap + 1);

            if( (int) ( Math.random() * 152369 ) == 0 )
            {
                for( int i = 0; i < 1000; i++ )
                {
                    WorldText worldText = WorldText.fromBlockPos( XP.getDimResLoc( world ), pos.east( (int) ( Math.random()*30 - 15 ) ).north( (int) ( Math.random()*30 - 15 ) ), pos.east( (int) ( Math.random()*60 - 30 ) ).north( (int) ( Math.random()*60 - 30 ) ).above( 25 ) );
                    worldText.setMaxOffset( 0.25f );
                    String text = "";
                    switch( (int) ( Math.random() * 4 ) )
                    {
                        case 0:
                            text = "owo";
                            break;

                        case 1:
                            text = "OwO";
                            break;

                        case 2:
                            text = "uwu";
                            break;

                        case 3:
                            text = "UwU";
                            break;
                    }
                    worldText.setText( text );
                    worldText.setHueColor( true );
                    worldText.setEndHue( 1080 );
                    worldText.setStartSize( 0 );
                    worldText.setEndSize( 50 );
                    worldText.setSecondsLifespan( (float) ( 10 + Math.random() * 50 ) );
                    worldText.setStartRot( (float) ( Math.random() * 360 - 180 ) );
                    worldText.setEndRot( (float) ( Math.random() * 360 - 180 ) );
                    XP.addWorldTextRadius( dimResLoc, worldText, 128 );
                }
            }

//            NetworkHandler.sendToPlayer( new MessageWorldText( new WorldText( XP.getDimResLoc( world ), Util.blockPosToVector( pos ), options ) ), (ServerPlayerEntity) player );

            WorldXpDrop xpDrop = WorldXpDrop.fromBlockPos( XP.getDimResLoc( world ), pos, 0.25, xp, awardSkillName );
            xpDrop.setDecaySpeed( 1.25 );
            XP.addWorldXpDrop( xpDrop, (ServerPlayer) player );

            Skill.addXp( awardSkillName, (ServerPlayer) player, award.get( awardSkillName ), awardMsg, false, false );
        }
    }

    public static double getTreasureItemChance( int level, Map<String, Double> map )
    {
        return Util.mapCapped( level, map.get( "startLevel" ), map.get( "endLevel" ), map.get( "startChance" ), map.get( "endChance" ) );
    }
}
