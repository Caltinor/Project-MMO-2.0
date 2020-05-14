package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageTripleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnvilRepairHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void handleAnvilRepair( AnvilRepairEvent event )
    {
        try
        {
            PlayerEntity player = event.getPlayer();
            if( !player.world.isRemote )
            {
                boolean bypassEnchantLimit = Config.forgeConfig.bypassEnchantLimit.get();
                int currLevel = XP.getLevel( Skill.SMITHING, player );
                ItemStack rItem = event.getIngredientInput();		//IGNORED FOR PURPOSE OF REPAIR
                ItemStack lItem = event.getItemInput();
                ItemStack oItem = event.getItemResult();

                if( event.getItemInput().getItem().isDamageable() )
                {
                    double anvilCostReductionPerLevel = Config.forgeConfig.anvilCostReductionPerLevel.get();
                    double extraChanceToNotBreakAnvilPerLevel = Config.forgeConfig.extraChanceToNotBreakAnvilPerLevel.get() / 100;
                    double anvilFinalItemBonusRepaired = Config.forgeConfig.anvilFinalItemBonusRepaired.get() / 100;
                    int anvilFinalItemMaxCostToAnvil = Config.forgeConfig.anvilFinalItemMaxCostToAnvil.get();

                    double bonusRepair = anvilFinalItemBonusRepaired * currLevel;
                    int maxCost = (int) Math.floor( 50 - ( currLevel * anvilCostReductionPerLevel ) );
                    if( maxCost < anvilFinalItemMaxCostToAnvil )
                        maxCost = anvilFinalItemMaxCostToAnvil;

                    event.setBreakChance( event.getBreakChance() / ( 1f + (float) extraChanceToNotBreakAnvilPerLevel * currLevel ) );

                    if( oItem.getRepairCost() > maxCost )
                        oItem.setRepairCost( maxCost );

                    float repaired = oItem.getDamage() - lItem.getDamage();
                    if( repaired < 0 )
                        repaired = -repaired;

                    oItem.setDamage( (int) Math.floor( oItem.getDamage() - repaired * bonusRepair ) );

                    double award = ( ( ( repaired + repaired * bonusRepair * 2.5 ) / 100 ) * ( 1 + lItem.getRepairCost() * 0.025 ) );
                    if( JsonConfig.data.get( "salvageInfo" ).containsKey( oItem.getItem().getRegistryName().toString() ) )
                        award *= (double) JsonConfig.data.get( "salvageInfo" ).get( oItem.getItem().getRegistryName().toString() ).get( "xpPerItem" );

                    if( award > 0 )
                    {
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraRepaired", "" + (int) repaired, "" + (int) ( repaired * bonusRepair ), true, 1 ), (ServerPlayerEntity) player );
                        XP.awardXp( player, Skill.SMITHING, "repairing an item by: " + repaired, award, false );
                    }
                }

                if( bypassEnchantLimit )
                {
                    Map<Enchantment, Integer> lEnchants = EnchantmentHelper.getEnchantments( rItem );
                    Map<Enchantment, Integer> rEnchants = EnchantmentHelper.getEnchantments( lItem );

                    Map<Enchantment, Integer> newEnchants = mergeEnchants( lEnchants, rEnchants, player, currLevel );

                    EnchantmentHelper.setEnchantments( newEnchants, oItem );
                }
            }
        }
        catch( Exception e )
        {
            LOGGER.error( "ANVIL FAILED, PLEASE REPORT", e );
            System.out.println( "ANVIL FAILED, PLEASE REPORT" );
        }
    }

    public static Map<Enchantment, Integer> mergeEnchants( Map<Enchantment, Integer> lEnchants, Map<Enchantment, Integer> rEnchants, PlayerEntity player, int currLevel )
    {
        Map<Enchantment, Integer> newEnchants = new HashMap<>();
        double bypassChance = Config.forgeConfig.upgradeChance.get();
        double failedBypassPenaltyChance = Config.forgeConfig.failedUpgradeKeepLevelChance.get();
        int levelsPerOneEnchantBypass = Config.forgeConfig.levelsPerOneEnchantBypass.get();
        int maxEnchantmentBypass = Config.forgeConfig.maxEnchantmentBypass.get();
        int maxEnchantLevel = Config.forgeConfig.maxEnchantLevel.get();
        boolean alwaysUseUpgradeChance = Config.forgeConfig.alwaysUseUpgradeChance.get();
        boolean creative = !XP.isPlayerSurvival( player );

        lEnchants.forEach( ( enchant, startLevel ) ->
        {
            if( newEnchants.containsKey( enchant ) )
            {
                if( newEnchants.get( enchant ) < startLevel )
                    newEnchants.replace( enchant, startLevel );
            }
            else
                newEnchants.put( enchant, startLevel );
        });


        rEnchants.forEach( ( enchant, startLevel ) ->
        {
            if( newEnchants.containsKey( enchant ) )
            {
                if( newEnchants.get( enchant ) < startLevel )
                    newEnchants.replace( enchant, startLevel );
            }
            else
                newEnchants.put( enchant, startLevel );
        });

        Set<Enchantment> keys = new HashSet<>( newEnchants.keySet() );

        keys.forEach( ( enchant ) ->
        {
            int startLevel = newEnchants.get( enchant );

            int maxPlayerBypass = (int) Math.floor( (double) currLevel / (double) levelsPerOneEnchantBypass );
            if( maxPlayerBypass > maxEnchantmentBypass )
                maxPlayerBypass = maxEnchantmentBypass;

            if( maxEnchantLevel < startLevel && !creative )
            {
                if( maxEnchantLevel > 0 )
                    newEnchants.replace( enchant, maxEnchantLevel );
                else
                    newEnchants.remove( enchant );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.maxEnchantLevelWarning", enchant.getRegistryName().toString(), "" + maxEnchantLevel, false, 2 ), (ServerPlayerEntity) player );
            }
            else if( enchant.getMaxLevel() + maxPlayerBypass < startLevel && !creative )
            {
                if( enchant.getMaxLevel() + maxPlayerBypass > 0 )
                    newEnchants.replace( enchant, enchant.getMaxLevel() + maxPlayerBypass );
                else
                    newEnchants.remove( enchant );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.enchantmentDegradedWarning", enchant.getRegistryName().toString(), "" + (enchant.getMaxLevel() + maxPlayerBypass), false, 2 ), (ServerPlayerEntity) player );
            }
            else if( lEnchants.get( enchant ) != null && rEnchants.get( enchant ) != null )
            {
                if( lEnchants.get( enchant ).intValue() == rEnchants.get( enchant ).intValue() ) //same values
                {
                    if( startLevel + 1 > maxEnchantLevel && !creative )
                    {
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.maxEnchantLevelWarning", enchant.getRegistryName().toString(), "" + maxEnchantLevel, false, 2 ), (ServerPlayerEntity) player );
                    }
                    else if( startLevel + 1 > enchant.getMaxLevel() + maxPlayerBypass && !creative )
                    {
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.enchantLackOfLevelWarning", enchant.getRegistryName() ).setStyle( XP.textStyle.get( "red" ) ), false );
                    }
                    else
                    {
                        if( ( ( startLevel >= enchant.getMaxLevel() ) || alwaysUseUpgradeChance ) && !creative )
                        {
                            if( Math.ceil( Math.random() * 100 ) <= bypassChance ) //success
                            {
                                newEnchants.replace( enchant, startLevel + 1 );
                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.enchantUpgradeSuccess", enchant.getRegistryName().toString(), "" + (startLevel + 1), false, 1 ), (ServerPlayerEntity) player );
                            }
                            else if( Math.ceil( Math.random() * 100 ) <= failedBypassPenaltyChance ) //fucked up twice
                            {
                                if( startLevel > 1 )
                                    newEnchants.replace( enchant, startLevel - 1 );
                                else
                                    newEnchants.remove( enchant );
                                NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.enchantUpgradeAndSaveFail", enchant.getRegistryName().toString(), "" + bypassChance, "" + failedBypassPenaltyChance, false, 2 ), (ServerPlayerEntity) player );
                            }
                            else	//only fucked up once
                            {
                                newEnchants.replace( enchant, startLevel );
                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.enchantUpgradeFail", enchant.getRegistryName().toString(), "" + bypassChance, false, 3 ), (ServerPlayerEntity) player );
                            }
                        }
                        else
                        {
                            newEnchants.replace( enchant, startLevel + 1 );
                            NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.enchantUpgradeSuccess", enchant.getRegistryName().toString(), "" + (startLevel + 1), false, 1 ), (ServerPlayerEntity) player );
                        }
                    }
                }
            }
        });

        return newEnchants;
    }
}
