package harmonised.pmmo.events;

import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeNetVolume;
import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.*;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class BlockBrokenHandler
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean dynamicTreesLoaded = Loader.isModLoaded( "dynamictrees" );

    public static void handleBroken( BlockEvent.BreakEvent event )
    {
        EntityPlayer player = event.getPlayer();
        if( !( player instanceof FakePlayer ) )
            processReq( event );
        ChunkDataHandler.delPos( event.getWorld().getWorldType().getId(), event.getPos() );
    }

    private static void processReq( BlockEvent.BreakEvent event )
    {
        EntityPlayer player = event.getPlayer();
        IBlockState state = event.getState();
        Block block = state.getBlock();
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        Material material = event.getState().getMaterial();
        Block blockAbove = world.getBlockState( pos.up() ).getBlock();
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
                ChunkDataHandler.delPos( world.getWorldType().getId(), pos );
            }
        }
        else
        {
            int startLevel;

            if( XP.getHarvestTool( state ).equals( "axe" ) )
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToChop", block.getUnlocalizedName(), "", true, 2 ), (EntityPlayerMP) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToChop", block.getUnlocalizedName(), "", false, 2 ), (EntityPlayerMP) player );
            }
            else if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( blockAbove.getRegistryName().toString() ) || block instanceof IPlantable )
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToHarvest", block.getUnlocalizedName(), "", true, 2 ), (EntityPlayerMP) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToHarvest", block.getUnlocalizedName(), "", false, 2 ), (EntityPlayerMP) player );
            }
            else
            {
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToBreak", block.getUnlocalizedName(), "", true, 2 ), (EntityPlayerMP) player );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToBreak", block.getUnlocalizedName(), "", false, 2 ), (EntityPlayerMP) player );
            }

            for( Map.Entry<String, Double> entry : JsonConfig.data.get( JType.REQ_BREAK ).get( block.getRegistryName().toString() ).entrySet() )
            {
                startLevel = Skill.getLevel( entry.getKey(), player );

                double entryValue = entry.getValue();

                if( startLevel < entryValue )
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.levelDisplay", "pmmo." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 2 ), (EntityPlayerMP) player );
                else
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.levelDisplay", "pmmo." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 1 ), (EntityPlayerMP) player );
            }

            event.setCanceled( true );
        }
    }

    private static void processBroken( BlockEvent.BreakEvent event )
    {
        IBlockState state = event.getState();
        Block block = state.getBlock();
        BlockPos pos = event.getPos();
        String regKey = block.getRegistryName().toString();
        final Map<String, Double> xpMap = XP.getXp( regKey, JType.XP_VALUE_BREAK );
        World world = event.getWorld();
        boolean isRemote = world.isRemote;
        EntityPlayer player = event.getPlayer();
        boolean veiningAllowed = FConfig.getConfig( "veiningAllowed" ) != 0;

        if( XP.isVeining.contains( player.getUniqueID() ) && veiningAllowed && !WorldTickHandler.activeVein.containsKey( player ) )
            WorldTickHandler.scheduleVein( player, new VeinInfo( world, state, pos, player.getHeldItemMainhand() ) );

        if( !XP.isPlayerSurvival( player ) || isRemote )
            return;

        Material material = event.getState().getMaterial();
        double blockHardnessLimitForBreaking = FConfig.blockHardnessLimitForBreaking;
        boolean wasPlaced = ChunkDataHandler.checkPos( world, pos ) != null;
        ItemStack toolUsed = player.getHeldItemMainhand();
        String skill = XP.getSkill( state );
//			String regKey = block.getRegistryName().toString();
        double hardness = state.getBlockHardness( event.getWorld(), pos );
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

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( player.getHeldItemMainhand() );
        int fortune = 0;
        if( enchants.get( Enchantments.FORTUNE ) != null )
            fortune = enchants.get( Enchantments.FORTUNE );

        boolean dropsItself = false;

        List<ItemStack> drops, noSilkDrops;

        if( world instanceof WorldServer )
        {
            drops = block.getDrops( world, pos, state, fortune );

//            if( EnchantmentHelper.getEnchantments( toolUsed ).containsKey( Enchantments.SILK_TOUCH ) )
//            {
//                ItemStack noEnchantTool = toolUsed.copy();
//                noEnchantTool.removeChildTag("Enchantments");
//
//                builder = new LootContext.Builder((ServerWorld) world)
//                        .withRandom(world.rand)
//                        .withParameter( LootParameters.POSITION, player.getPosition() )
//                        .withParameter( LootParameters.TOOL, noEnchantTool )
//                        .withParameter( LootParameters.THIS_ENTITY, player )
//                        .withNullableParameter( LootParameters.BLOCK_ENTITY, world.getTileEntity( pos ) );
//                if (fortune > 0)
//                {
//                    builder.withLuck(fortune);
//                }
//                noSilkDrops = block.getDrops( event.getState(), builder );
//                if( noSilkDrops.size() > 0 && noSilkDrops.get(0).getItem().equals( XP.getBlockAsItem( block ) ) )
//                    dropsItself = true;
//            }
            //COUT SILK TOUCH
        }
        else
            drops = new ArrayList<>();

        Map<String, Double> award = new HashMap<>();
        award.put( skill, hardness );

        int dropItemCount = 0;

        if( drops.size() > 0 )
        {
            dropItemCount = drops.get(0).getCount();
            if( drops.get(0).getItem().equals( XP.getBlockAsItem( block ) ) )
                dropsItself = true;
        }

        if( !wasPlaced )
            award = XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( xpMap, Math.max( dropItemCount, 1 ) ) );

        ItemStack theDropItem = drops.size() > 0 ? drops.get( 0 ) : ItemStack.EMPTY;

        //PLANT
        if( JsonConfig.data.get( JType.BLOCK_SPECIFIC ).containsKey( regKey ) && JsonConfig.data.get( JType.BLOCK_SPECIFIC ).get( regKey ).containsKey( "growsUpwards" ) ) //Handle Upwards Growing Plants
        {
            Block baseBlock = event.getState().getBlock();
            BlockPos baseBlockPos = pos;

            double extraChance = XP.getExtraChance( player.getUniqueID(), block.getRegistryName(), JType.INFO_PLANT, false ) / 100;
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
                XP.dropItems( dropsLeft, XP.getBlockAsItem( block ), world, pos );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + dropsLeft, theDropItem.getDisplayName(), true, 1 ), (EntityPlayerMP) player );
            }

            totalDrops = rewardable + dropsLeft;
            award.put( skill, hardness );
            award = XP.addMapsAnyDouble( award, xpMap );
            XP.multiplyMapAnyDouble( award, totalDrops );

            awardMsg = "removing " + height + " + " + ( guaranteedDrop + extraDrop ) + " extra " + block.getRegistryName();
        }
        else if( block instanceof BlockCrops && drops.size() > 0 ) //IS PLANT
        {
            award = new HashMap<>();
            award.put( skill, hardness );

            int totalExtraDrops;

//            for( ItemStack drop : drops )
//            {
//                dropItemCount += drop.getCount();
//            }

            BlockCrops blockCrops = (BlockCrops) block;

            if( blockCrops.isMaxAge( state ) )
            {
                double extraChance = XP.getExtraChance( player.getUniqueID(), block.getRegistryName(), JType.INFO_PLANT, false ) / 100;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                totalExtraDrops = guaranteedDrop + extraDrop;

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, pos );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getDisplayName(), true, 1 ), (EntityPlayerMP) player );
                }

                awardMsg = "harvesting " + ( dropItemCount ) + " + " + totalExtraDrops + " " + block.getRegistryName();
                XP.multiplyMapAnyDouble( XP.addMapsAnyDouble( award, xpMap ), dropItemCount + totalExtraDrops );
            }
            else if( !wasPlaced )
            {
                awardMsg = "Breaking " + block.getRegistryName();
                XP.multiplyMapAnyDouble( XP.addMapsAnyDouble( award, xpMap ), dropItemCount );
            }
        }

        //ORE
        if( XP.getExtraChance( player.getUniqueID(), block.getRegistryName(), JType.INFO_ORE, false ) > 0 )
        {
            award = new HashMap<>();
            award.put( skill, hardness );

            boolean isSilk = enchants.get( Enchantments.SILK_TOUCH ) != null;

            if( !wasPlaced && !isSilk )
                XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), theDropItem.getCount() ) );

            if( dropsItself && !wasPlaced || !dropsItself && !isSilk )			//EXTRA DROPS
            {
                double extraChance = XP.getExtraChance( player.getUniqueID(), block.getRegistryName(), JType.INFO_ORE, false ) / 100;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                if( !dropsItself && wasPlaced )
                    XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), ( theDropItem.getCount() ) ) );

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, pos );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getDisplayName(), true, 1 ), (EntityPlayerMP) player );
                }

                XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), totalExtraDrops ) );
            }

            awardMsg = "Mining " + block.getRegistryName();
        }

        //LOG
        //Dynamic Trees
        if( dynamicTreesLoaded && block instanceof BlockBranch )
        {
            BlockBranch branchBlock = (BlockBranch) block;
            MapSignal signal = branchBlock.analyse( state, world, pos, null, new MapSignal());
            NodeNetVolume volumeNet = new NodeNetVolume();
            branchBlock.analyse( state, world, pos, signal.localRootDir, new MapSignal(volumeNet));
            double volume = volumeNet.getVolume();
            drops = branchBlock.getLogDrops( world, pos, branchBlock.getFamily().getSpeciesForLocation( world, pos ), (float) volume );
            String[] oreNames = OreDictionary.getOreNames();
            award = new HashMap<>();

            for( ItemStack itemStack : drops )
            {
                try
                {
                    int extraDrops;
                    ResourceLocation resLoc = null;
                    int[] oreIDs = OreDictionary.getOreIDs( itemStack );
                    for( int id : oreIDs )
                    {
                        String tagName = oreNames[ id ];
                        if( tagName.startsWith( "log" ) || tagName.startsWith( "stick" ) )
                        {
                            resLoc = itemStack.getItem().getRegistryName();
                            break;
                        }
                    }

                    if( resLoc != null )
                    {
                        double extraChance = XP.getExtraChance( player.getUniqueID(), resLoc, JType.INFO_LOG, false );
                        extraDrops = (int) ( itemStack.getCount() * extraChance / 100D );
                        XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( XP.getXp( resLoc, JType.XP_VALUE_BREAK ), itemStack.getCount() + extraDrops ) );

                        if( extraDrops > 0 )
                        {
                            ItemStack extraDropStack = itemStack.copy();
                            extraDropStack.setCount( extraDrops );
                            XP.dropItemStack( extraDropStack, world, pos );
                            NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + extraDrops, extraDropStack.getDisplayName(), true, 1 ), (EntityPlayerMP) player );
                        }
                    }
                }
                catch( Exception e )
                {
                    LOGGER.error( e );
                }
            }
        }
        else if( XP.getExtraChance( player.getUniqueID(), block.getRegistryName(), JType.INFO_LOG, false ) > 0 && isEffective )
        {
            if( !wasPlaced )			//EXTRA DROPS
            {
                award = new HashMap<>();
                award.put( skill, hardness );

                double extraChance = XP.getExtraChance( player.getUniqueID(), block.getRegistryName(), JType.INFO_LOG, false ) / 100D;

                int guaranteedDrop = (int) extraChance;
                int extraDrop;

                if( XP.rollChance( extraChance % 1 ) )
                    extraDrop = 1;
                else
                    extraDrop = 0;

                int totalExtraDrops = guaranteedDrop + extraDrop;

                if( totalExtraDrops > 0 )
                {
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, pos );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getDisplayName(), true, 1 ), (EntityPlayerMP) player );
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

                        award.put( Skill.EXCAVATION.toString(), award.getOrDefault( Skill.EXCAVATION.toString(), 0D ) + treasureItemMap.get( "xpPerItem" ) * count );

                        ItemStack itemStack = new ItemStack( item, count );
                        XP.dropItemStack( itemStack, world, pos );
                        foundTreasure = true;

                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.youFoundTreasureItem", count, new TextComponentTranslation( itemStack.getDisplayName() ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                        LOGGER.info( player.getDisplayName().getUnformattedText() + " found Treasure! " + count + " " + treasureItem.getKey() + " " + pos );
                    }

                    if( foundTreasure )
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.youFoundTreasure" ).setStyle( XP.textStyle.get( "green" ) ), true );
                }
            }
        }

        int gap = XP.getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), JType.REQ_TOOL );

        if( gap > 0 )
            player.getHeldItemMainhand().damageItem( gap - 1, player );

        for( String awardSkill : award.keySet() )
        {
            XP.awardXp( (EntityPlayerMP) player, awardSkill, awardMsg, award.get( awardSkill ) / (gap + 1), !skill.equals( awardSkill ), false, false );
        }
    }

    public static double getTreasureItemChance( int level, Map<String, Double> map )
    {
        return DP.mapCapped( level, map.get( "startLevel" ), map.get( "endLevel" ), map.get( "startChance" ), map.get( "endChance" ) );
    }
}