package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageTripleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerInteractionHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handlePlayerInteract( PlayerInteractEvent event )
    {
        try
        {
            if( event instanceof PlayerInteractEvent.RightClickBlock || event instanceof PlayerInteractEvent.RightClickItem)
            {
                PlayerEntity player = event.getPlayer();
                ItemStack itemStack = event.getItemStack();
                Item item = itemStack.getItem();
                Block goldBlock 	= 	Blocks.GOLD_BLOCK;
                Block smithBlock    =   Blocks.SMITHING_TABLE;

                if( item.getRegistryName() == null )
                    return;

                String regKey = item.getRegistryName().toString();
                int startLevel;
                boolean isRemote = player.world.isRemote();
                boolean matched;

                if( event instanceof PlayerInteractEvent.RightClickItem)
                {
                    if( XP.isPlayerSurvival( player ) )
                    {
                        if( JsonConfig.data2.get( JType.SALVAGE ).containsKey( regKey ) )
                        {
                            matched = XP.scanBlock( smithBlock, 1, player );
                            if( !matched )
                                matched = XP.scanBlock( goldBlock, 1, player );

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
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToPlaceDown", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                    }
                }
                else if( !XP.checkReq( player, item.getRegistryName(), JType.REQ_USE ) )
                {
                    event.setCanceled( true );

                    if( isRemote )
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToUse", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                }

                if( event instanceof PlayerInteractEvent.RightClickBlock)
                {
                    Block block = player.world.getBlockState( event.getPos() ).getBlock();

                    if( !XP.checkReq( player, block.getRegistryName(), JType.REQ_USE ) )
                    {
                        if( XP.isPlayerSurvival( player ) )
                        {
                            event.setCanceled( true );
                            if( isRemote && event.getHand().equals( Hand.MAIN_HAND ) )
                            {
                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToUse", new TranslationTextComponent( block.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToUse", new TranslationTextComponent( block.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), false );

//                            if( JsonConfig.data.get( JType.REQ_USE ).containsKey( block.getRegistryName().toString() ) )
//                            {
                                for( Map.Entry<String, Double> entry : JsonConfig.data.get( JType.REQ_USE ).get( block.getRegistryName().toString() ).entrySet() )
                                {
                                    startLevel = Skill.getSkill( entry.getKey() ).getLevel( player );

                                    double entryValue = entry.getValue();

                                    if( startLevel < entryValue )
                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "" + DP.dpSoft( entryValue ) ).setStyle( XP.textStyle.get( "red" ) ), false );
                                    else
                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "" + DP.dpSoft( entryValue ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                                }
//                            }
                            }
                        }
                    }
                    else
                    {
                        event.setCanceled( false );

                        int startSmithingLevel = Skill.SMITHING.getLevel( player );
                        int smithingLevel;
                        Integer lowestReqLevel = null;

                        if( player.isCrouching() )
                        {
                            if( ( block.equals( goldBlock ) || block.equals( smithBlock ) ) )
                            {
                                if( item.equals( Items.AIR ) )
                                    return;

                                if( isRemote )
                                    return;

                                if( !event.getHand().equals( Hand.OFF_HAND ) )
                                    return;

                                itemStack = player.getHeldItemOffhand();
                                item = itemStack.getItem();

                                if( JsonConfig.data2.get( JType.SALVAGE ).containsKey( item.getRegistryName().toString() ) )
                                    event.setCanceled( true );
                                else
                                {
                                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.cannotSalvage", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }

                                if( !( XP.getHorizontalDistance( player.getPositionVec(), XP.blockToMiddleVec( event.getPos() ) ) < 2 ) )
                                {
                                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.tooFarAwayToSalvage" ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }

//                            if( event.getHand() != Hand.OFF_HAND )
//                            {
//                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.offhandToDiss" ), false );
//                                XP.sendMessage( "_________________________________", false, player );
//                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.durabilityInfo", item.getTranslationKey(), "" + DP.dp( displayDurabilityPercent ), false, 0 ), (ServerPlayerEntity) player );
//                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.materialSaveChanceInfo", DP.dp( chance ), potentialReturnAmount ), false );
//                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.repairInfo", "" + DP.dp( enchantChance ), "" + itemStack.getRepairCost(), false, 0 ), (ServerPlayerEntity) player );
//                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.enchantmentBypassInfo", "" + maxPlayerBypass ), false );
//                                return;
//                            }

                                if( !XP.isPlayerSurvival( player ) )
                                {
                                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.survivalOnlyWarning" ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }

                                //SALVAGE IS AVAILABLE
                                Map<String, Map<String, Double>> salvageFromItemMap = JsonConfig.data2.get( JType.SALVAGE ).get( regKey );
                                Map<String, Double> salvageToItemMap;
                                boolean ableToSalvageAny = false;
                                double award = 0;

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

                                        double startDmg = itemStack.getDamage();
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
                                            XP.dropItems( returnAmount, salvageToItem, event.getWorld(), event.getPos() );

                                        if( returnAmount == potentialReturnAmount )
                                            NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageToItem.getTranslationKey(), false, 1 ), (ServerPlayerEntity) player );
                                        else if( returnAmount > 0 )
                                            NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageToItem.getTranslationKey(), false, 3 ), (ServerPlayerEntity) player );
                                        else
                                            NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageToItem.getTranslationKey(), true, 2 ), (ServerPlayerEntity) player );
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
                                            Block.spawnAsEntity( event.getWorld(), event.getPos(), salvagedBook );
                                            if( fullEnchants )
                                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.savedAllEnchants" ).setStyle( XP.textStyle.get( "green" ) ), false );
                                            else
                                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.savedSomeEnchants" ).setStyle( XP.textStyle.get( "yellow" ) ), false );
                                        }
                                    }

                                    if( award > 0 )
                                        XP.awardXp( (ServerPlayerEntity) player, Skill.SMITHING, item.getRegistryName().toString(), award, false, false, false );

                                    player.getHeldItemOffhand().shrink( 1 );
                                    player.sendBreakAnimation(Hand.OFF_HAND );
                                }
                                else
                                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.cannotSalvageLackLevelLonger", lowestReqLevel, new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                            }
                        }
                    }
                }
            }
        }
        catch( Exception e )
        {
            LOGGER.error( e );
        }
    }
}