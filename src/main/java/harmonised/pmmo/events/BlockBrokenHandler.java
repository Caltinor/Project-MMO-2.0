package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.WorldText;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageWorldText;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.*;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import javafx.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
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
        PlayerEntity player = event.getPlayer();
        if( !( player instanceof FakePlayer ) )
            processReq( event );
        ChunkDataHandler.delPos( XP.getDimResLoc( (World) event.getWorld() ), event.getPos() );
    }

    private static void processReq( BlockEvent.BreakEvent event )
    {
        PlayerEntity player = event.getPlayer();
        BlockState blockState = event.getState();
        Block block = blockState.getBlock();
        BlockPos pos = event.getPos();
        World world = (World) event.getWorld();
        Block blockAbove = world.getBlockState( pos.up() ).getBlock();
        ResourceLocation dimResLoc = XP.getDimResLoc( (World) event.getWorld() );

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
            player.sendStatusMessage( new StringTextComponent( block.getRegistryName().toString() ), false );

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
                ChunkDataHandler.delPos( XP.getDimResLoc( world ), pos );
            }
        }
        else
        {
            int startLevel;

            if( XP.getHarvestTool( blockState ).equals( "axe" ) )
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

            for( Map.Entry<String, Double> entry : JsonConfig.data.get( JType.REQ_BREAK ).get( block.getRegistryName().toString() ).entrySet() )
            {
                startLevel = Skill.getLevel( entry.getKey(), player );

                double entryValue = entry.getValue();

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
        final Map<String, Double> xpMap = XP.getXp( regKey, JType.XP_VALUE_BREAK );
        World world = (World) event.getWorld();
        boolean isRemote = world.isRemote();
        PlayerEntity player = event.getPlayer();
        boolean veiningAllowed = Config.getConfig( "veiningAllowed" ) != 0;

        if( XP.isVeining.contains( player.getUniqueID() ) && veiningAllowed && !WorldTickHandler.activeVein.containsKey( player ) )
            WorldTickHandler.scheduleVein( player, new VeinInfo( world, state, event.getPos(), player.getHeldItemMainhand() ) );

        if( !XP.isPlayerSurvival( player ) || isRemote )
            return;

        Material material = event.getState().getMaterial();
        double blockHardnessLimitForBreaking = Config.forgeConfig.blockHardnessLimitForBreaking.get();
        boolean wasPlaced = ChunkDataHandler.checkPos( world, event.getPos() ) != null;
        ItemStack toolUsed = player.getHeldItemMainhand();
        String skill = XP.getSkill( state );
//			String regKey = block.getRegistryName().toString();
        double hardness = Math.min( blockHardnessLimitForBreaking, state.getBlockHardness( event.getWorld(), event.getPos() ) );

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

        if( world instanceof ServerWorld )
        {
            LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                    .withRandom(world.rand)
                    .withParameter( LootParameters.field_237457_g_, player.getPositionVec() )
                    .withParameter( LootParameters.TOOL, toolUsed )
                    .withParameter( LootParameters.THIS_ENTITY, player )
                    .withNullableParameter( LootParameters.BLOCK_ENTITY, world.getTileEntity( event.getPos() ) );
            if (fortune > 0)
            {
                builder.withLuck(fortune);
            }
            drops = block.getDrops( event.getState(), builder );

            if( EnchantmentHelper.getEnchantments( toolUsed ).containsKey( Enchantments.SILK_TOUCH ) )
            {
                ItemStack noEnchantTool = toolUsed.copy();
                noEnchantTool.removeChildTag("Enchantments");

                builder = new LootContext.Builder((ServerWorld) world)
                        .withRandom(world.rand)
                        .withParameter( LootParameters.field_237457_g_, player.getPositionVec() )
                        .withParameter( LootParameters.TOOL, noEnchantTool )
                        .withParameter( LootParameters.THIS_ENTITY, player )
                        .withNullableParameter( LootParameters.BLOCK_ENTITY, world.getTileEntity( event.getPos() ) );
                if (fortune > 0)
                {
                    builder.withLuck(fortune);
                }
                noSilkDrops = block.getDrops( event.getState(), builder );
                if( noSilkDrops.size() > 0 && noSilkDrops.get(0).getItem().equals( block.asItem() ) )
                    dropsItself = true;
            }
        }
        else
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
                XP.dropItems( dropsLeft, block.asItem(), world, event.getPos() );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + dropsLeft, theDropItem.getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
            }

            totalDrops = rewardable + dropsLeft;
            award.put( skill, hardness );
            XP.addMapsAnyDouble( award, xpMap );
            XP.multiplyMapAnyDouble( award, totalDrops );

            awardMsg = "removing " + height + " + " + ( guaranteedDrop + extraDrop ) + " extra " + block.getRegistryName();
        }
        else if( ( material.equals( Material.PLANTS ) || material.equals( Material.OCEAN_PLANT ) || material.equals( Material.TALL_PLANTS ) ) && drops.size() > 0 ) //IS PLANT
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
                if( state.hasProperty( BlockStateProperties.AGE_0_1 ) )
                {
                    age = state.get( BlockStateProperties.AGE_0_1 );
                    maxAge = 1;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_0_2 ) )
                {
                    age = state.get( BlockStateProperties.AGE_0_2 );
                    maxAge = 2;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_0_3 ) )
                {
                    age = state.get( BlockStateProperties.AGE_0_3 );
                    maxAge = 3;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_0_5 ) )
                {
                    age = state.get( BlockStateProperties.AGE_0_5 );
                    maxAge = 5;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_0_7 ) )
                {
                    age = state.get( BlockStateProperties.AGE_0_7 );
                    maxAge = 7;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_0_15 ) )
                {
                    age = state.get( BlockStateProperties.AGE_0_15 );
                    maxAge = 15;
                }
                else if( state.hasProperty( BlockStateProperties.AGE_0_25 ) )
                {
                    age = state.get( BlockStateProperties.AGE_0_25 );
                    maxAge = 25;
                }
                else if( state.hasProperty( BlockStateProperties.PICKLES_1_4 ) )
                {
                    age = state.get( BlockStateProperties.PICKLES_1_4 );
                    maxAge = 4;
                    if( wasPlaced )
                        return;
                }
            }

            if( age == maxAge && age >= 0 || block instanceof SeaPickleBlock )
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
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
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
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                }

                XP.addMapsAnyDouble( award, XP.multiplyMapAnyDouble( XP.getXp( block.getRegistryName(), JType.XP_VALUE_BREAK ), totalExtraDrops ) );
            }

            awardMsg = "Mining " + block.getRegistryName();
        }

        //LOG
        if( XP.getExtraChance( player.getUniqueID(), block.getRegistryName(), JType.INFO_LOG, false ) > 0 && isEffective )
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
                    XP.dropItems( guaranteedDrop + extraDrop, theDropItem.getItem(), world, event.getPos() );
                    NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + totalExtraDrops, theDropItem.getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
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
                        XP.dropItemStack( itemStack, world, event.getPos() );
                        foundTreasure = true;

                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.youFoundTreasureItem", count, new TranslationTextComponent( itemStack.getTranslationKey() ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                        LOGGER.debug( player.getDisplayName().getString() + " found Treasure! " + count + " " + treasureItem.getKey() + " " + event.getPos() );
                    }

                    if( foundTreasure )
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.youFoundTreasure" ).setStyle( XP.textStyle.get( "green" ) ), true );
                }
            }
        }

        int gap = XP.getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), JType.REQ_TOOL );

        if( gap > 0 )
            player.getHeldItemMainhand().damageItem( gap - 1, player, (a) -> a.sendBreakAnimation(Hand.MAIN_HAND ) );

        BlockPos pos = event.getPos();
        for( String awardSkillName : award.keySet() )
        {
            double xp = award.get( awardSkillName ) / (gap + 1);

            if( (int) ( Math.random() * 152369 ) == 0 )
            {
                for( int i = 0; i < 1000; i++ )
                {
                    WorldText worldText = WorldText.fromBlockPos( XP.getDimResLoc( world ), pos.east( (int) ( Math.random()*30 - 15 ) ).north( (int) ( Math.random()*30 - 15 ) ), pos.east( (int) ( Math.random()*60 - 30 ) ).north( (int) ( Math.random()*60 - 30 ) ).up( 25 ) );
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
                    XP.addWorldTextRadius( worldText, 128 );
                }
            }

//            NetworkHandler.sendToPlayer( new MessageWorldText( new WorldText( XP.getDimResLoc( world ), Util.blockPosToVector( pos ), options ) ), (ServerPlayerEntity) player );

            WorldXpDrop xpDrop = WorldXpDrop.fromBlockPos( XP.getDimResLoc( world ), pos, 0.25, xp, awardSkillName );
            xpDrop.setDecaySpeed( 1.25 );
            XP.addWorldXpDrop( xpDrop, (ServerPlayerEntity) player );

            Skill.addXp( awardSkillName, (ServerPlayerEntity) player, award.get( awardSkillName ), awardMsg, false, false );
        }
    }

    public static double getTreasureItemChance( int level, Map<String, Double> map )
    {
        return Util.mapCapped( level, map.get( "startLevel" ), map.get( "endLevel" ), map.get( "startChance" ), map.get( "endChance" ) );
    }
}
