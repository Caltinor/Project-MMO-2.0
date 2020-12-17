package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageTripleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilRepairHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handleAnvilRepair( AnvilRepairEvent event )
    {
        if( FConfig.anvilHandlingEnabled )
            try
            {
                if( !event.getEntityPlayer().world.isRemote )
                {
                    EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
                    boolean bypassEnchantLimit = FConfig.bypassEnchantLimit;
                    int currLevel = Skill.getLevel( Skill.SMITHING.toString(), player );
                    ItemStack rItem = event.getIngredientInput();		//IGNORED FOR PURPOSE OF REPAIR
                    ItemStack lItem = event.getItemInput();
                    ItemStack oItem = event.getItemResult();

                    if( event.getItemInput().getItem().isDamageable() )
                    {
                        double anvilCostReductionPerLevel = FConfig.anvilCostReductionPerLevel;
                        double extraChanceToNotBreakAnvilPerLevel = FConfig.extraChanceToNotBreakAnvilPerLevel / 100;
                        double anvilFinalItemBonusRepaired = FConfig.anvilFinalItemBonusRepaired / 100;
                        int anvilFinalItemMaxCostToAnvil = FConfig.anvilFinalItemMaxCostToAnvil;

                        double bonusRepair = anvilFinalItemBonusRepaired * currLevel;
                        int maxCost = (int) Math.floor( 50 - ( currLevel * anvilCostReductionPerLevel ) );
                        if( maxCost < anvilFinalItemMaxCostToAnvil )
                            maxCost = anvilFinalItemMaxCostToAnvil;

                        event.setBreakChance( event.getBreakChance() / ( 1f + (float) extraChanceToNotBreakAnvilPerLevel * currLevel ) );

                        if( oItem.getRepairCost() > maxCost )
                            oItem.setRepairCost( maxCost );

                        double repaired = oItem.getItemDamage() - lItem.getItemDamage();
                        if( repaired < 0 )
                            repaired = -repaired;

                        oItem.setItemDamage( (int) Math.floor( oItem.getItemDamage() - repaired * bonusRepair ) );

                        double award = ( ( ( repaired + repaired * bonusRepair * 2.5 ) / 100 ) * ( 1 + lItem.getRepairCost() * 0.025 ) );
//                        if( JsonConfig.data.get( JType.SALVAGE ).containsKey( oItem.getItem().getRegistryName().toString() ) )
//                            award *= JsonConfig.data.get( JType.SALVAGE ).get( oItem.getItem().getRegistryName().toString() ).get( "xpPerItem" );
                        //COUT bad feature? can't remember

                        if( award > 0 )
                        {
                            NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraRepaired", "" + (int) repaired, "" + (int) ( repaired * bonusRepair ), true, 1 ), (EntityPlayerMP) player );
                            XP.awardXp( player, Skill.SMITHING.toString(), "repairing an item by: " + repaired, award, false, false, false );
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
                LOGGER.info( "ANVIL FAILED, PLEASE REPORT " + e.toString() );
            }
    }

    public static Map<Enchantment, Integer> mergeEnchants( Map<Enchantment, Integer> lEnchants, Map<Enchantment, Integer> rEnchants, EntityPlayer player, int currLevel )
    {
        Map<Enchantment, Integer> newEnchants = new HashMap<>();
        double bypassChance = FConfig.upgradeChance;
        double failedBypassPenaltyChance = FConfig.failedUpgradeKeepLevelChance;
        int levelsPerOneEnchantBypass = FConfig.levelsPerOneEnchantBypass;
        int maxEnchantmentBypass = FConfig.maxEnchantmentBypass;
        int maxEnchantLevel = FConfig.maxEnchantLevel;
        boolean alwaysUseUpgradeChance = FConfig.alwaysUseUpgradeChance;
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
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.maxEnchantLevelWarning", enchant.getTranslatedName( 1 ).replace( " I", "" ), "" + maxEnchantLevel, false, 2 ), (EntityPlayerMP) player );
            }
            else if( enchant.getMaxLevel() + maxPlayerBypass < startLevel && !creative )
            {
                if( enchant.getMaxLevel() + maxPlayerBypass > 0 )
                    newEnchants.replace( enchant, enchant.getMaxLevel() + maxPlayerBypass );
                else
                    newEnchants.remove( enchant );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.enchantmentDegradedWarning", enchant.getTranslatedName( 1 ).replace( " I", "" ), "" + (enchant.getMaxLevel() + maxPlayerBypass), false, 2 ), (EntityPlayerMP) player );
            }
            else if( lEnchants.get( enchant ) != null && rEnchants.get( enchant ) != null )
            {
                if( lEnchants.get( enchant ).intValue() == rEnchants.get( enchant ).intValue() ) //same values
                {
                    if( startLevel + 1 > maxEnchantLevel && !creative )
                    {
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.maxEnchantLevelWarning", enchant.getTranslatedName( 1 ).replace( " I", "" ), "" + maxEnchantLevel, false, 2 ), (EntityPlayerMP) player );
                    }
                    else if( startLevel + 1 > enchant.getMaxLevel() + maxPlayerBypass && !creative )
                    {
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.enchantLackOfLevelWarning", enchant.getTranslatedName( 1 ).replace( " I", "" ) ).setStyle( XP.textStyle.get( "red" ) ), false );
                    }
                    else
                    {
                        if( ( ( startLevel >= enchant.getMaxLevel() ) || alwaysUseUpgradeChance ) && !creative )
                        {
                            if( Math.ceil( Math.random() * 100 ) <= bypassChance ) //success
                            {
                                newEnchants.replace( enchant, startLevel + 1 );
                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.enchantUpgradeSuccess", enchant.getTranslatedName( 1 ).replace( " I", "" ), "" + (startLevel + 1), false, 1 ), (EntityPlayerMP) player );
                            }
                            else if( Math.ceil( Math.random() * 100 ) <= failedBypassPenaltyChance ) //fucked up twice
                            {
                                if( startLevel > 1 )
                                    newEnchants.replace( enchant, startLevel - 1 );
                                else
                                    newEnchants.remove( enchant );
                                NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.enchantUpgradeAndSaveFail", enchant.getTranslatedName( 1 ).replace( " I", "" ), "" + bypassChance, "" + failedBypassPenaltyChance, false, 2 ), (EntityPlayerMP) player );
                            }
                            else	//only fucked up once
                            {
                                newEnchants.replace( enchant, startLevel );
                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.enchantUpgradeFail", enchant.getTranslatedName( 1 ).replace( " I", "" ), "" + bypassChance, false, 3 ), (EntityPlayerMP) player );
                            }
                        }
                        else
                        {
                            newEnchants.replace( enchant, startLevel + 1 );
                            NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.enchantUpgradeSuccess", enchant.getTranslatedName( 1 ).replace( " I", "" ), "" + (startLevel + 1), false, 1 ), (EntityPlayerMP) player );
                        }
                    }
                }
            }
        });

        return newEnchants;
    }
}
