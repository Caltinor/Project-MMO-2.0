package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageTripleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerInteractionHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handlePlayerInteract( PlayerInteractEvent event )
    {
        try
        {
            if( event.getItemStack().isEmpty() )
                return;
            if( event instanceof PlayerInteractEvent.RightClickBlock || event instanceof PlayerInteractEvent.RightClickItem)
            {
                EntityPlayer player = event.getEntityPlayer();
                ItemStack itemStack = event.getItemStack();
                Item item = itemStack.getItem();
                Block goldBlock 	= 	Blocks.GOLD_BLOCK;

                if( item.getRegistryName() == null )
                    return;

                String regKey = item.getRegistryName().toString();
                int startLevel;
                boolean isRemote = player.world.isRemote;
                boolean matched;

                if( event instanceof PlayerInteractEvent.RightClickItem)
                {
                    if( XP.isPlayerSurvival( player ) )
                    {
                        if( JsonConfig.data2.get( JType.SALVAGE ).containsKey( regKey ) )
                        {
                            matched = XP.scanBlock( goldBlock, 1, player );

                            if( matched )
                            {
                                event.setCanceled( true );

//							if( isRemote )
//								player.sendStatusMessage( new TextComponentTranslation( "pmmo.cannotUseProximity", new TextComponentTranslation( matchedBlock.getDisplayName() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                            }
                        }
                    }
                }

                if( item instanceof ItemBlock )
                {
                    if( !XP.checkReq( player, item.getRegistryName(), JType.REQ_PLACE ) )
                    {
                        event.setCanceled( true );

                        if( isRemote )
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToPlaceDown", new TextComponentTranslation( itemStack.getDisplayName() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                    }
                }
                else if( !XP.checkReq( player, item.getRegistryName(), JType.REQ_USE ) )
                {
                    event.setCanceled( true );

                    if( isRemote )
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToUse", new TextComponentTranslation( itemStack.getDisplayName() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                }

                if( event instanceof PlayerInteractEvent.RightClickBlock)
                {
                    Block block = player.world.getBlockState( event.getPos() ).getBlock();

                    if( !XP.checkReq( player, block.getRegistryName(), JType.REQ_USE ) )
                    {
                        if( XP.isPlayerSurvival( player ) )
                        {
                            event.setCanceled( true );
                            if( isRemote && event.getHand().equals( EnumHand.MAIN_HAND ) )
                            {
                                player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToUse", new TextComponentTranslation( new ItemStack( block ).getDisplayName() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToUse", new TextComponentTranslation( new ItemStack( block ).getDisplayName() ) ).setStyle( XP.textStyle.get( "red" ) ), false );

//                            if( JsonConfig.data.get( JType.REQ_USE ).containsKey( block.getRegistryName().toString() ) )
//                            {
                                for( Map.Entry<String, Double> entry : JsonConfig.data.get( JType.REQ_USE ).get( block.getRegistryName().toString() ).entrySet() )
                                {
                                    startLevel = Skill.getLevel( entry.getKey(), player );

                                    double entryValue = entry.getValue();

                                    if( startLevel < entryValue )
                                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.levelDisplay", new TextComponentTranslation( "pmmo." + entry.getKey() ), "" + DP.dpSoft( entryValue ) ).setStyle( XP.textStyle.get( "red" ) ), false );
                                    else
                                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.levelDisplay", new TextComponentTranslation( "pmmo." + entry.getKey() ), "" + DP.dpSoft( entryValue ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                                }
//                            }
                            }
                        }
                    }
                    else
                    {
                        event.setCanceled( false );

                        if( player.isSneaking() )
                        {
                            if( ( block.equals( goldBlock ) ) )
                            {
                                if( itemStack.isEmpty() )
                                    return;
                                if( isRemote )
                                    return;

                                boolean mainCanBeSalvaged = canBeSalvaged( player.getHeldItemMainhand().getItem() );
                                boolean offCanBeSalvaged = canBeSalvaged( player.getHeldItemOffhand().getItem() );

                                if( !( mainCanBeSalvaged || offCanBeSalvaged ) )
                                {
                                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.cannotSalvage", new TextComponentTranslation( itemStack.getDisplayName() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }
                                event.setCanceled( true );

                                if( !( XP.getHorizontalDistance( player.getPositionVector(), XP.blockToMiddleVec( event.getPos() ) ) < 2 ) )
                                {
                                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.tooFarAwayToSalvage" ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }

                                if( !XP.isPlayerSurvival( player ) )
                                {
                                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.survivalOnlyWarning" ).setStyle( XP.textStyle.get( "red" ) ), true );
                                    return;
                                }

                                //SALVAGE IS AVAILABLE FOR ONE OR MORE HANDS
                                salvageItem( player, itemStack, event.getWorld(), event.getPos() );
                                if( Skill.getLevel( Skill.SMITHING.toString(), player ) >= FConfig.getConfig( "dualSalvageSmithingLevelReq" ) && event.getHand().equals( EnumHand.MAIN_HAND ) && offCanBeSalvaged )
                                    salvageItem( player, player.getHeldItemOffhand(), event.getWorld(), event.getPos() );
                            }
                        }
                    }
                }
            }
        }
        catch( Exception e )
        {
            LOGGER.info( e.toString() );
        }
    }

    public static void salvageItem(EntityPlayer player, ItemStack itemStack, World world, BlockPos pos )
    {
        Item item = itemStack.getItem();
        String regKey = item.getRegistryName().toString();
        Map<String, Map<String, Double>> salvageFromItemMap = JsonConfig.data2.get( JType.SALVAGE ).get( regKey );
        if( salvageFromItemMap == null )
            return; //IT WAS THE OTHER HAND
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

                double startDmg = itemStack.getItemDamage();
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
                    XP.dropItemStack( new ItemStack( salvageToItem, returnAmount ), world, pos );

                ItemStack returnItemStack = new ItemStack( salvageToItem );

                if( returnAmount == potentialReturnAmount )
                    NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, returnItemStack.getDisplayName(), false, 1 ), (EntityPlayerMP) player );
                else if( returnAmount > 0 )
                    NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, returnItemStack.getDisplayName(), false, 3 ), (EntityPlayerMP) player );
                else
                    NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, returnItemStack.getDisplayName(), false, 2 ), (EntityPlayerMP) player );
            }
        }

        if( ableToSalvageAny )
        {
            //ENCHANTS
            Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( itemStack );
//                                int maxEnchantmentBypass = FConfig.maxEnchantmentBypass;
//                                int levelsPerOneEnchantBypass = FConfig.levelsPerOneEnchantBypass;
            double maxSalvageEnchantChance = FConfig.maxSalvageEnchantChance;
            double enchantSaveChancePerLevel = FConfig.enchantSaveChancePerLevel;
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
                    Block.spawnAsEntity( world, pos, salvagedBook );
                    if( fullEnchants )
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.savedAllEnchants" ).setStyle( XP.textStyle.get( "green" ) ), false );
                    else
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.savedSomeEnchants" ).setStyle( XP.textStyle.get( "yellow" ) ), false );
                }
            }

            if( award > 0 )
                XP.awardXp( (EntityPlayerMP) player, Skill.SMITHING.toString(), item.getRegistryName().toString(), award, false, false, false );

            itemStack.shrink( 1 );
//                                    player.sendBreakAnimation(EnumHand.OFF_HAND );
            //COUT BREAK ANIMATION
        }
        else
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.cannotSalvageLackLevelLonger", lowestReqLevel, new TextComponentTranslation( itemStack.getDisplayName() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
    }

    public static boolean canBeSalvaged( Item item )
    {
        return JsonConfig.data2.get( JType.SALVAGE ).containsKey( item.getRegistryName().toString() );
    }
}