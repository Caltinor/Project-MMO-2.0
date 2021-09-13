package harmonised.pmmo.events;

import harmonised.pmmo.api.events.SalvageEvent;
import harmonised.pmmo.api.events.XpEvent;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageTripleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerInteractionHandler
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Set<Block> salvageStations = new HashSet<>();

    private static long lastWarning = 0;

    public static void initSalvageStations()
    {
        salvageStations.clear();
        for( Map.Entry<String, Map<String, Double>> entry : JsonConfig.data.get( JType.BLOCK_SPECIFIC ).entrySet() )
        {
            if( entry.getValue().getOrDefault( "salvageStation", 0D ) != 0 )
                salvageStations.add( XP.getBlock( entry.getKey() ) );
        }
        if( salvageStations.size() == 0 )
            salvageStations.add( Blocks.SMITHING_TABLE );
    }

    public static void handlePlayerInteract( PlayerInteractEvent event )
    {
        try
        {
            if( event instanceof PlayerInteractEvent.RightClickBlock || event instanceof PlayerInteractEvent.RightClickItem)
            {
                Player player = event.getPlayer();
                ItemStack itemStack = event.getItemStack();
                Item item = itemStack.getItem();

                if( item.getRegistryName() == null )
                    return;

                String regKey = item.getRegistryName().toString();
                int startLevel;
                boolean isRemote = player.level.isClientSide();
                boolean matched = false;

                if( event instanceof PlayerInteractEvent.RightClickItem)
                {
                    if( XP.isPlayerSurvival( player ) )
                    {
                        if( JsonConfig.data2.get( JType.SALVAGE ).containsKey( regKey ) )
                        {
                            for( Block salvageStationBlock : salvageStations )
                            {
                                matched = XP.scanBlock( salvageStationBlock, 1, player );
                                if( matched )
                                    break;
                            }

                            if( matched )
                            {
                                event.setCanceled( true );

//							if( isRemote )
//								player.sendStatusMessage( new TranslationTextComponent( "pmmo.cannotUseProximity", new TranslationTextComponent( matchedBlock.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                            }
                        }
                    }
                }

                if( item instanceof BlockItem )
                {
                    if( !XP.checkReq( player, item.getRegistryName(), JType.REQ_PLACE ) )
                    {
                        event.setCanceled( true );

                        if( isRemote )
                            player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToPlaceDown", new TranslatableComponent( item.getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                    }
                }
                else if( !XP.checkReq( player, item.getRegistryName(), JType.REQ_USE ) )
                {
                    event.setCanceled( true );

                    if( isRemote )
                        player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToUse", new TranslatableComponent( item.getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                }

                if( event instanceof PlayerInteractEvent.RightClickBlock)
                {
                    Block block = player.level.getBlockState( event.getPos() ).getBlock();

                    if( !XP.checkReq( player, block.getRegistryName(), JType.REQ_USE ) )
                    {
                        if( XP.isPlayerSurvival( player ) )
                        {
                            event.setCanceled( true );
                            if( isRemote && event.getHand().equals( InteractionHand.MAIN_HAND ) )
                            {
                                player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToUse", new TranslatableComponent( block.getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToUse", new TranslatableComponent( block.getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), false );

//                            if( JsonConfig.data.get( JType.REQ_USE ).containsKey( block.getRegistryName().toString() ) )
//                            {
                                for( Map.Entry<String, Double> entry : JsonConfig.data.get( JType.REQ_USE ).get( block.getRegistryName().toString() ).entrySet() )
                                {
                                    startLevel = Skill.getLevel( entry.getKey(), player );

                                    double entryValue = entry.getValue();

                                    if( startLevel < entryValue )
                                        player.displayClientMessage( new TranslatableComponent( "pmmo.levelDisplay", new TranslatableComponent( "pmmo." + entry.getKey() ), "" + DP.dpSoft( entryValue ) ).setStyle( XP.textStyle.get( "red" ) ), false );
                                    else
                                        player.displayClientMessage( new TranslatableComponent( "pmmo.levelDisplay", new TranslatableComponent( "pmmo." + entry.getKey() ), "" + DP.dpSoft( entryValue ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                                }
//                            }
                            }
                        }
                    }
                    else
                    {
                        event.setCanceled( false );

                        if( player.isCrouching() )
                        {
                            if( salvageStations.contains( block ) )
                            {
                                if( itemStack.isEmpty() )
                                    return;
                                if( isRemote )
                                    return;

                                boolean mainCanBeSalvaged = canBeSalvaged( player.getMainHandItem().getItem() );
                                boolean offCanBeSalvaged = canBeSalvaged( player.getOffhandItem().getItem() );

                                if( !( mainCanBeSalvaged || offCanBeSalvaged ) )
                                {
                                    player.displayClientMessage( new TranslatableComponent( "pmmo.cannotSalvage", new TranslatableComponent( item.getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }

                                SalvageEvent salvageEvent = new SalvageEvent( player, event.getPos() );
                                if( MinecraftForge.EVENT_BUS.post( salvageEvent ) )
                                    return;
                                event.setCanceled( true );

                                if( !( XP.getHorizontalDistance( player.position(), XP.blockToMiddleVec( event.getPos() ) ) < 2 ) )
                                {
                                    player.displayClientMessage( new TranslatableComponent( "pmmo.tooFarAwayToSalvage" ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }

                                if( !XP.isPlayerSurvival( player ) )
                                {
                                    player.displayClientMessage( new TranslatableComponent( "pmmo.survivalOnlyWarning" ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }

                                //SALVAGE IS AVAILABLE FOR ONE OR MORE HANDS
                                salvageItem( player, itemStack, event.getWorld(), event.getPos() );
                                if( Skill.getLevel( Skill.SMITHING.toString(), player ) >= Config.getConfig( "dualSalvageSmithingLevelReq" ) && event.getHand().equals( InteractionHand.MAIN_HAND ) && offCanBeSalvaged )
                                    salvageItem( player, player.getOffhandItem(), event.getWorld(), event.getPos() );
                            }
                        }
                    }
                }
            }
            else if( event instanceof PlayerInteractEvent.EntityInteractSpecific )
            {
                Player player = event.getPlayer();
                PlayerInteractEvent.EntityInteractSpecific entityInteractEvent = (PlayerInteractEvent.EntityInteractSpecific) event;
                Entity target = entityInteractEvent.getTarget();
                Map<String, Double> reqMap = XP.getXp( target, JType.REQ_ENTITY_INTERACT );
                if( !XP.checkReq( player, reqMap ) )
                {
                    event.setCanceled( true );
                    boolean isRemote = player.level.isClientSide();
                    if( isRemote && System.currentTimeMillis() - lastWarning > 1523 )
                    {
                        player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToInteractWith", target.getName() ).setStyle( XP.textStyle.get( "red" ) ), false );
                        player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToInteractWith", target.getName() ).setStyle( XP.textStyle.get( "red" ) ), true );
                        XP.sendPlayerSkillList( player, reqMap );
                        lastWarning = System.currentTimeMillis();
                    }
                }
            }
        }
        catch( Exception e )
        {
            LOGGER.error( e );
        }
    }

    public static void salvageItem(Player player, ItemStack itemStack, Level world, BlockPos pos )
    {
        Item item = itemStack.getItem();
        String regKey = item.getRegistryName().toString();
        Map<String, Map<String, Double>> salvageFromItemMap = JsonConfig.data2.get( JType.SALVAGE ).get( regKey );
        if( salvageFromItemMap == null )
            return; //THIS ITEM DOES NOT SALVAGE
        Map<String, Double> salvageToItemMap;
        boolean ableToSalvageAny = false;
        double award = 0;
        int startSmithingLevel = Skill.getLevel( Skill.SMITHING.toString(), player );
        int smithingLevel;
        Integer lowestReqLevel = null;

        for( Map.Entry<String, Map<String, Double>> salvageToItemEntry : salvageFromItemMap.entrySet() )
        {
            smithingLevel = startSmithingLevel;
            Item salvageToItem = XP.getItem( salvageToItemEntry.getKey() );
            salvageToItemMap = salvageToItemEntry.getValue();

            double baseChance = salvageToItemMap.get( "baseChance" );
            double chancePerLevel = salvageToItemMap.get( "chancePerLevel" );
            double maxSalvageMaterialChance = salvageToItemMap.get( "maxChance" );
            int reqLevel = (int) Math.floor( salvageToItemMap.get( "levelReq" ) );
            int salvageMax = (int) Math.floor( salvageToItemMap.get( "salvageMax" ) );
            smithingLevel -= reqLevel;

            if( lowestReqLevel == null || lowestReqLevel > reqLevel )
                lowestReqLevel = reqLevel;
            if( smithingLevel >= 0 )
            {
                ableToSalvageAny = true;
                double chance = baseChance + ( chancePerLevel * smithingLevel );

                if( chance > maxSalvageMaterialChance )
                    chance = maxSalvageMaterialChance;

                double startDmg = itemStack.getDamageValue();
                double maxDmg = itemStack.getMaxDamage();
                double displayDurabilityPercent = ( 1.00f - ( startDmg / maxDmg ) ) * 100;
                double durabilityPercent = ( 1.00f - ( startDmg / maxDmg ) );

                if( Double.isNaN( durabilityPercent ) )
                    durabilityPercent = 1;

                int potentialReturnAmount = (int) Math.floor( salvageMax * durabilityPercent );

                int returnAmount = 0;

                for( int i = 0; i < potentialReturnAmount; i++ )
                {
                    if( Math.ceil( Math.random() * 10000 ) <= chance * 100 )
                        returnAmount++;
                }
                award += salvageToItemMap.get( "xpPerItem" ) * returnAmount;

                if( returnAmount > 0 )
                    XP.dropItems( returnAmount, salvageToItem, world, pos );

                if( returnAmount == potentialReturnAmount )
                    NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageToItem.getDescriptionId(), false, 1 ), (ServerPlayer) player );
                else if( returnAmount > 0 )
                    NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageToItem.getDescriptionId(), false, 3 ), (ServerPlayer) player );
                else
                    NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageToItem.getDescriptionId(), true, 2 ), (ServerPlayer) player );
            }
        }

        if( ableToSalvageAny )
        {
            //ENCHANTS
            Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( itemStack );
//                                int maxEnchantmentBypass = Config.forgeConfig.maxEnchantmentBypass.get();
//                                int levelsPerOneEnchantBypass = Config.forgeConfig.levelsPerOneEnchantBypass.get();
            double maxSalvageEnchantChance = Config.forgeConfig.maxSalvageEnchantChance.get();
            double enchantSaveChancePerLevel = Config.forgeConfig.enchantSaveChancePerLevel.get();
//                                int maxPlayerBypass = (int) Math.floor( (double) startSmithingLevel / (double) levelsPerOneEnchantBypass );
//                                if( maxPlayerBypass > maxEnchantmentBypass )
//                                    maxPlayerBypass = maxEnchantmentBypass;
            double enchantChance = (startSmithingLevel - lowestReqLevel) * enchantSaveChancePerLevel;
            if( enchantChance > maxSalvageEnchantChance )
                enchantChance = maxSalvageEnchantChance;

            if( enchants.size() > 0 )
            {
                ItemStack salvagedBook = new ItemStack( Items.ENCHANTED_BOOK );
                Set<Enchantment> enchantKeys = enchants.keySet();
                Map<Enchantment, Integer> newEnchantMap = new HashMap<>();
                int enchantLevel;
                boolean fullEnchants = true;

                for( Enchantment enchant : enchantKeys )
                {
                    enchantLevel = 0;
                    for( int i = 1; i <= enchants.get( enchant ); i++ )
                    {
                        if( Math.floor( Math.random() * 100 ) < enchantChance )
                            enchantLevel = i;
                        else
                        {
                            fullEnchants = false;
//				    						i = enchants.get( enchant ) + 1;
                        }
                    }
                    if( enchantLevel > 0 )
                        newEnchantMap.put( enchant, enchantLevel );
                }
                if( newEnchantMap.size() > 0 )
                {
                    EnchantmentHelper.setEnchantments( newEnchantMap, salvagedBook );
                    Block.popResource( world, pos, salvagedBook );
                    if( fullEnchants )
                        player.displayClientMessage( new TranslatableComponent( "pmmo.savedAllEnchants" ).setStyle( XP.textStyle.get( "green" ) ), false );
                    else
                        player.displayClientMessage( new TranslatableComponent( "pmmo.savedSomeEnchants" ).setStyle( XP.textStyle.get( "yellow" ) ), false );
                }
            }

            if( award > 0 )
            {
                WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( world ), pos.getX() + 0.5, pos.getY() + 1.523, pos.getZ() + 0.5, 0.35, award, Skill.SMITHING.toString() );
                XP.addWorldXpDrop( xpDrop, (ServerPlayer) player);
                XP.awardXp( (ServerPlayer) player, Skill.SMITHING.toString(), item.getRegistryName().toString(), award, false, false, false );
            }

            itemStack.shrink( 1 );
            player.broadcastBreakEvent(InteractionHand.OFF_HAND );
        }
        else
            player.displayClientMessage( new TranslatableComponent( "pmmo.cannotSalvageLackLevelLonger", lowestReqLevel, new TranslatableComponent( item.getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
    }

    public static boolean canBeSalvaged( Item item )
    {
        return JsonConfig.data2.get( JType.SALVAGE ).containsKey( item.getRegistryName().toString() );
    }
}