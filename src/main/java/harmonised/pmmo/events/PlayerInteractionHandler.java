package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageTripleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerInteractionHandler
{
    public static void handleItemUse( PlayerInteractEvent event )
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
            String regKey = item.getRegistryName().toString();
            int startLevel;
            boolean isRemote = player.world.isRemote();
            boolean matched;

            if( event instanceof PlayerInteractEvent.RightClickItem)
            {
                if( XP.isPlayerSurvival( player ) )
                {
                    if( JsonConfig.data.get( "salvageInfo" ).containsKey( regKey ) )
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

            if( !XP.checkReq( player, item.getRegistryName(), "use" ) )
            {
                if( !(item instanceof BlockItem) || !XP.checkReq( player, item.getRegistryName(), "place" ) )
                {
                    event.setCanceled( true );

                    if( isRemote )
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.toUse", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                }
            }

            if( event instanceof PlayerInteractEvent.RightClickBlock)
            {
                Block block = player.world.getBlockState( event.getPos() ).getBlock();

                if( !XP.checkReq( player, block.getRegistryName(), "use" ) )
                {
                    if( XP.isPlayerSurvival( player ) )
                    {
                        event.setCanceled( true );
                        if( isRemote && event.getHand().equals( Hand.MAIN_HAND ) )
                        {
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.toUse", new TranslationTextComponent( block.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.toUse", new TranslationTextComponent( block.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), false );

//                            if( JsonConfig.data.get( "useReq" ).containsKey( block.getRegistryName().toString() ) )
//                            {
                                for( Map.Entry<String, Object> entry : JsonConfig.data.get( "useReq" ).get( block.getRegistryName().toString() ).entrySet() )
                                {
                                    startLevel = XP.getLevel( Skill.getSkill( entry.getKey() ), player );

                                    double entryValue = 1;
                                    if( entry.getValue() instanceof Double )
                                        entryValue = (double) entry.getValue();

                                    if( startLevel < entryValue )
                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "" + (int) Math.floor( entryValue ) ).setStyle( XP.textStyle.get( "red" ) ), false );
                                    else
                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "" + (int) Math.floor( entryValue ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                                }
//                            }
                        }
                    }
                }
                else
                {
                    event.setCanceled( false );

                    Block anvil 		=	Blocks.ANVIL;
                    Block ironBlock		= 	Blocks.IRON_BLOCK;
                    Block diamondBlock 	= 	Blocks.DIAMOND_BLOCK;

                    int smithingLevel = XP.getLevel( Skill.SMITHING, player );
                    int maxEnchantmentBypass = Config.forgeConfig.maxEnchantmentBypass.get();
                    int levelsPerOneEnchantBypass = Config.forgeConfig.levelsPerOneEnchantBypass.get();
                    int maxPlayerBypass = (int) Math.floor( (double) smithingLevel / (double) levelsPerOneEnchantBypass );
                    if( maxPlayerBypass > maxEnchantmentBypass )
                        maxPlayerBypass = maxEnchantmentBypass;

                    if( player.isCrouching() )
                    {
                        if( block.equals( ironBlock ) || block.equals( anvil ) )
                        {
                            if( event.getHand() == Hand.MAIN_HAND )
                            {
                                //Outdated, Replaced by Tooltip
                            }
                            else
                                return;
                        }

                        if( ( block.equals( goldBlock ) || block.equals( smithBlock ) ) )
                        {
                            if( JsonConfig.data.get( "salvageInfo" ).containsKey( regKey ) )
                                event.setCanceled( true );

                            if( isRemote )
                                return;

                            if( event.getHand().equals( Hand.OFF_HAND ) )
                            {
                                itemStack = player.getHeldItemOffhand();
                                item = itemStack.getItem();

                                if( !item.equals( Items.AIR ) )
                                {
                                    if( JsonConfig.data.get( "salvageInfo" ).containsKey( regKey ) )
                                    {
                                        if( player.getPosition().withinDistance( event.getPos(), 2 ) )
                                        {
                                            Map<String, Object> theMap = JsonConfig.data.get( "salvageInfo" ).get( regKey );
                                            Item salvageItem = XP.getItem( (String) theMap.get( "salvageItem" ) );
                                            if( !salvageItem.equals( Items.AIR ) )
                                            {
                                                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( itemStack );
                                                double baseChance = (double) theMap.get( "baseChance" );
                                                double chancePerLevel = (double) theMap.get( "chancePerLevel" );
                                                double maxSalvageMaterialChance = (double) theMap.get( "maxChance" );
                                                int reqLevel = (int) Math.floor( (double) theMap.get( "levelReq" ) );
                                                int salvageMax = (int) Math.floor( (double) theMap.get( "salvageMax" ) );
                                                smithingLevel -= reqLevel;
                                                if( smithingLevel >= 0 )
                                                {
                                                    double chance = baseChance + ( chancePerLevel * smithingLevel );
                                                    double maxSalvageEnchantChance = Config.forgeConfig.maxSalvageEnchantChance.get();
                                                    double enchantSaveChancePerLevel = Config.forgeConfig.enchantSaveChancePerLevel.get();

                                                    if( chance > maxSalvageMaterialChance )
                                                        chance = maxSalvageMaterialChance;

                                                    double enchantChance = smithingLevel * enchantSaveChancePerLevel;
                                                    if( enchantChance > maxSalvageEnchantChance )
                                                        enchantChance = maxSalvageEnchantChance;

                                                    double startDmg = itemStack.getDamage();
                                                    double maxDmg = itemStack.getMaxDamage();
                                                    double award = 0;
                                                    double displayDurabilityPercent = ( 1.00f - ( startDmg / maxDmg ) ) * 100;
                                                    double durabilityPercent = ( 1.00f - ( startDmg / maxDmg ) );

                                                    if( Double.isNaN( durabilityPercent ) )
                                                        durabilityPercent = 1;

                                                    int potentialReturnAmount = (int) Math.floor( salvageMax * durabilityPercent );

                                                    if( event.getHand() == Hand.OFF_HAND )
                                                    {
                                                        if( XP.isPlayerSurvival( player ) )
                                                        {
                                                            int returnAmount = 0;

                                                            for( int i = 0; i < potentialReturnAmount; i++ )
                                                            {
                                                                if( Math.ceil( Math.random() * 10000 ) <= chance * 100 )
                                                                    returnAmount++;
                                                            }
                                                            award += (double) theMap.get( "xpPerItem" ) * returnAmount;

                                                            if( returnAmount > 0 )
                                                                XP.dropItems( returnAmount, salvageItem, event.getWorld(), event.getPos() );

                                                            if( award > 0 )
                                                                XP.awardXp( player, Skill.SMITHING, "salvaging " + returnAmount + "/" + salvageMax + " from an item", award, false, false  );

                                                            if( returnAmount == potentialReturnAmount )
                                                                NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageItem.getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                                                            else if( returnAmount > 0 )
                                                                NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageItem.getTranslationKey(), true, 3 ), (ServerPlayerEntity) player );
                                                            else
                                                                NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.salvageMessage", "" + returnAmount, "" + potentialReturnAmount, salvageItem.getTranslationKey(), true, 2 ), (ServerPlayerEntity) player );

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
//				    													i = enchants.get( enchant ) + 1;
                                                                        }
                                                                    }
                                                                    if( enchantLevel > 0 )
                                                                        newEnchantMap.put( enchant, enchantLevel );
                                                                }
                                                                if( newEnchantMap.size() > 0 )
                                                                {
                                                                    EnchantmentHelper.setEnchantments( newEnchantMap, salvagedBook );
                                                                    block.spawnAsEntity( event.getWorld(), event.getPos(), salvagedBook );
                                                                    if( fullEnchants )
                                                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.savedAllEnchants" ).setStyle( XP.textStyle.get( "green" ) ), false );
                                                                    else
                                                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.savedSomeEnchants" ).setStyle( XP.textStyle.get( "yellow" ) ), false );
                                                                }
                                                            }
                                                            player.getHeldItemOffhand().shrink( 1 );
//				    									player.inventory.offHandInventory.set( 0, new ItemStack( Items.AIR, 0 ) );
                                                            player.sendBreakAnimation(Hand.OFF_HAND );
                                                        }
                                                        else
                                                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.survivalOnlyWarning" ).setStyle( XP.textStyle.get( "red" ) ), true );
                                                    }
                                                    else
                                                    {
                                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.offhandToDiss" ), false );
                                                        XP.sendMessage( "_________________________________", false, player );
                                                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.durabilityInfo", item.getTranslationKey(), "" + DP.dp( displayDurabilityPercent ), false, 0 ), (ServerPlayerEntity) player );
                                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.materialSaveChanceInfo", DP.dp( chance ), potentialReturnAmount ), false );
                                                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.repairInfo", "" + DP.dp( enchantChance ), "" + itemStack.getRepairCost(), false, 0 ), (ServerPlayerEntity) player );
                                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.enchantmentBypassInfo", "" + maxPlayerBypass ), false );
                                                    }
                                                }
                                                else
                                                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.cannotSalvageLackLevelLonger", reqLevel, new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                            }
                                            else
                                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.invalidSalvageItem", theMap.get( "salvageItem" ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                        }
                                        else
                                        {
                                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.tooFarAwayToSalvage" ).setStyle( XP.textStyle.get( "red" ) ), true );
                                        }
                                    }
                                    else
                                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.cannotSalvage", new TranslationTextComponent( item.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                                }
                            }
                        }

                        if( block.equals( diamondBlock ) && event.getHand() == Hand.MAIN_HAND )
                        {
                            int agilityLevel = XP.getLevel( Skill.AGILITY, player );
                            int enduranceLevel = XP.getLevel( Skill.ENDURANCE, player );
                            int combatLevel = XP.getLevel( Skill.COMBAT, player );
                            int swimLevel = XP.getLevel( Skill.SMITHING, player );
                            int nightvisionUnlockLevel = (int) Math.floor( Config.getConfig( "nightvisionUnlockLevel" ) );	//Swimming

                            double maxFallSaveChance = Config.forgeConfig.maxFallSaveChance.get();			//Agility
                            double saveChancePerLevel = Config.forgeConfig.saveChancePerLevel.get() / 100;
                            double speedBoostPerLevel = Config.getConfig( "speedBoostPerLevel" );
                            double maxSpeedBoost = Config.getConfig( "maxSpeedBoost" );

                            int levelsPerDamage = Config.forgeConfig.levelsPerDamage.get();					//Combat

                            double endurancePerLevel = Config.forgeConfig.endurancePerLevel.get();			//Endurance
                            double maxEndurance = Config.forgeConfig.maxEndurance.get();
                            double endurePercent = (enduranceLevel * endurancePerLevel);
                            if( endurePercent > maxEndurance )
                                endurePercent = maxEndurance;

                            double reach = AttributeHandler.getReach( player );
                            double agilityChance = agilityLevel * saveChancePerLevel;

                            double extraDamage = Math.floor( combatLevel / levelsPerDamage );

                            double speedBonus = agilityLevel * speedBoostPerLevel;

                            if( agilityChance > maxFallSaveChance )
                                agilityChance = maxFallSaveChance;

                            if( speedBonus > maxSpeedBoost )
                                speedBonus = maxSpeedBoost;

                            XP.sendMessage( "_________________________________" , false, player );
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.buildingInfo", DP.dp( reach ) ), false );
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.fallInfo", DP.dp( agilityChance ) ), false );
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.enduranceInfo", DP.dp( endurePercent ) ), false );
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.combatInfo", DP.dp( extraDamage ) ), false );
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.sprintInfo", DP.dp( speedBonus ) ), false );
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.enchantmentBypassInfo", maxPlayerBypass ), false );

                            if( swimLevel >= nightvisionUnlockLevel )
                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.underwaterNightVisionUnLocked", nightvisionUnlockLevel ), false );
                            else
                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.underwaterNightVisionLocked", nightvisionUnlockLevel ), false );
                        }
                    }
                }
            }
        }
        }
        catch( Exception e )
        {
            System.out.println( e );
        }
    }
}
