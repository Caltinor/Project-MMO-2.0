package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageTripleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import org.apache.logging.log4j.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnvilRepairHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handleAnvilRepair(AnvilRepairEvent event)
    {
        if(Config.forgeConfig.anvilHandlingEnabled.get())
        try
        {

            if(!event.getPlayer().level.isClientSide)
            {
                ServerPlayer player = (ServerPlayer) event.getPlayer();
                boolean bypassEnchantLimit = Config.forgeConfig.bypassEnchantLimit.get();
                int currLevel = Skill.getLevel(Skill.SMITHING.toString(), player);
                ItemStack rItem = event.getIngredientInput();		//IGNORED FOR PURPOSE OF REPAIR
                ItemStack lItem = event.getItemInput();
                ItemStack oItem = event.getItemResult();

                if(event.getItemInput().getItem().canBeDepleted())
                {
                    double anvilCostReductionPerLevel = Config.forgeConfig.anvilCostReductionPerLevel.get();
                    double extraChanceToNotBreakAnvilPerLevel = Config.forgeConfig.extraChanceToNotBreakAnvilPerLevel.get() / 100;
                    double anvilFinalItemBonusRepaired = Config.forgeConfig.anvilFinalItemBonusRepaired.get() / 100;
                    int anvilFinalItemMaxCostToAnvil = Config.forgeConfig.anvilFinalItemMaxCostToAnvil.get();

                    double bonusRepair = anvilFinalItemBonusRepaired * currLevel;
                    int maxCost = (int) Math.floor(50 - (currLevel * anvilCostReductionPerLevel));
                    if(maxCost < anvilFinalItemMaxCostToAnvil)
                        maxCost = anvilFinalItemMaxCostToAnvil;

                    event.setBreakChance(event.getBreakChance() / (1f + (float) extraChanceToNotBreakAnvilPerLevel * currLevel));

                    if(oItem.getBaseRepairCost() > maxCost)
                        oItem.setRepairCost(maxCost);

                    float repaired = oItem.getDamageValue() - lItem.getDamageValue();
                    if(repaired < 0)
                        repaired = -repaired;

                    oItem.setDamageValue((int) Math.floor(oItem.getDamageValue() - repaired * bonusRepair));

                    double award = (((repaired + repaired * bonusRepair * 2.5) / 100) * (1 + lItem.getBaseRepairCost() * 0.025));
//                    if(JsonConfig.data2.get(JType.SALVAGE).containsKey(oItem.getItem().getRegistryName().toString()))
//                        award *= (double) JsonConfig.data2.get(JType.SALVAGE).get(oItem.getItem().getRegistryName().toString()).get("xpPerItem");
                    //COUT

                    if(award > 0)
                    {
                        Vec3 pos = player.position();
                        WorldXpDrop xpDrop = WorldXpDrop.fromVector(XP.getDimResLoc(player.getLevel()), new Vec3(pos.x(), pos.y() + player.getEyeHeight(), pos.z()).add(player.getLookAngle().multiply(1.523, 1.523, 1.523)), 0.523, award, Skill.SMITHING.toString());
                        xpDrop.setDecaySpeed(0.25);
                        xpDrop.setSize(2);
                        XP.addWorldXpDrop(xpDrop, player);
                        NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.extraRepaired", "" + (int) repaired, "" + (int) (repaired * bonusRepair), true, 1), player);
                        XP.awardXp(player, Skill.SMITHING.toString(), "repairing an item by: " + repaired, award, false, false, false);
                    }
                }

                if(bypassEnchantLimit)
                {
                    Map<Enchantment, Integer> lEnchants = EnchantmentHelper.getEnchantments(rItem);
                    Map<Enchantment, Integer> rEnchants = EnchantmentHelper.getEnchantments(lItem);

                    Map<Enchantment, Integer> newEnchants = mergeEnchants(lEnchants, rEnchants, player, currLevel);

                    EnchantmentHelper.setEnchantments(newEnchants, oItem);
                }
            }
        }
        catch(Exception e)
        {
            LOGGER.error("ANVIL FAILED, PLEASE REPORT", e);
        }
    }

    public static Map<Enchantment, Integer> mergeEnchants(Map<Enchantment, Integer> lEnchants, Map<Enchantment, Integer> rEnchants, Player player, int currLevel)
    {
        Map<Enchantment, Integer> newEnchants = new HashMap<>();
        double bypassChance = Config.forgeConfig.upgradeChance.get();
        double failedBypassPenaltyChance = Config.forgeConfig.failedUpgradeKeepLevelChance.get();
        int levelsPerOneEnchantBypass = Config.forgeConfig.levelsPerOneEnchantBypass.get();
        int maxEnchantmentBypass = Config.forgeConfig.maxEnchantmentBypass.get();
        int maxEnchantLevel = Config.forgeConfig.maxEnchantLevel.get();
        boolean alwaysUseUpgradeChance = Config.forgeConfig.alwaysUseUpgradeChance.get();
        boolean creative = !XP.isPlayerSurvival(player);

        lEnchants.forEach((enchant, startLevel) ->
        {
            if(newEnchants.containsKey(enchant))
            {
                if(newEnchants.get(enchant) < startLevel)
                    newEnchants.replace(enchant, startLevel);
            }
            else
                newEnchants.put(enchant, startLevel);
        });


        rEnchants.forEach((enchant, startLevel) ->
        {
            if(newEnchants.containsKey(enchant))
            {
                if(newEnchants.get(enchant) < startLevel)
                    newEnchants.replace(enchant, startLevel);
            }
            else
                newEnchants.put(enchant, startLevel);
        });

        Set<Enchantment> keys = new HashSet<>(newEnchants.keySet());

        keys.forEach((enchant) ->
        {
            int startLevel = newEnchants.get(enchant);

            int maxPlayerBypass = (int) Math.floor((double) currLevel / (double) levelsPerOneEnchantBypass);
            if(maxPlayerBypass > maxEnchantmentBypass)
                maxPlayerBypass = maxEnchantmentBypass;

            if(maxEnchantLevel < startLevel && !creative)
            {
                if(maxEnchantLevel > 0)
                    newEnchants.replace(enchant, maxEnchantLevel);
                else
                    newEnchants.remove(enchant);
                NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.maxEnchantLevelWarning", enchant.getFullname(1).getString().replace(" I", ""), "" + maxEnchantLevel, false, 2), (ServerPlayer) player);
            }
            else if(enchant.getMaxLevel() + maxPlayerBypass < startLevel && !creative)
            {
                if(enchant.getMaxLevel() + maxPlayerBypass > 0)
                    newEnchants.replace(enchant, enchant.getMaxLevel() + maxPlayerBypass);
                else
                    newEnchants.remove(enchant);
                NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.enchantmentDegradedWarning", enchant.getFullname(1).getString().replace(" I", ""), "" + (enchant.getMaxLevel() + maxPlayerBypass), false, 2), (ServerPlayer) player);
            }
            else if(lEnchants.get(enchant) != null && rEnchants.get(enchant) != null)
            {
                if(lEnchants.get(enchant).intValue() == rEnchants.get(enchant).intValue()) //same values
                {
                    if(startLevel + 1 > maxEnchantLevel && !creative)
                    {
                        NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.maxEnchantLevelWarning", enchant.getFullname(1).getString().replace(" I", ""), "" + maxEnchantLevel, false, 2), (ServerPlayer) player);
                    }
                    else if(startLevel + 1 > enchant.getMaxLevel() + maxPlayerBypass && !creative)
                    {
                        player.displayClientMessage(new TranslatableComponent("pmmo.enchantLackOfLevelWarning", enchant.getFullname(1).getString().replace(" I", "")).setStyle(XP.textStyle.get("red")), false);
                    }
                    else
                    {
                        if(((startLevel >= enchant.getMaxLevel()) || alwaysUseUpgradeChance) && !creative)
                        {
                            if(Math.ceil(Math.random() * 100) <= bypassChance) //success
                            {
                                newEnchants.replace(enchant, startLevel + 1);
                                NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.enchantUpgradeSuccess", enchant.getFullname(1).getString().replace(" I", ""), "" + (startLevel + 1), false, 1), (ServerPlayer) player);
                            }
                            else if(Math.ceil(Math.random() * 100) <= failedBypassPenaltyChance) //fucked up twice
                            {
                                if(startLevel > 1)
                                    newEnchants.replace(enchant, startLevel - 1);
                                else
                                    newEnchants.remove(enchant);
                                NetworkHandler.sendToPlayer(new MessageTripleTranslation("pmmo.enchantUpgradeAndSaveFail", enchant.getFullname(1).getString().replace(" I", ""), "" + bypassChance, "" + failedBypassPenaltyChance, false, 2), (ServerPlayer) player);
                            }
                            else	//only fucked up once
                            {
                                newEnchants.replace(enchant, startLevel);
                                NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.enchantUpgradeFail", enchant.getFullname(1).getString().replace(" I", ""), "" + bypassChance, false, 3), (ServerPlayer) player);
                            }
                        }
                        else
                        {
                            newEnchants.replace(enchant, startLevel + 1);
                            NetworkHandler.sendToPlayer(new MessageDoubleTranslation("pmmo.enchantUpgradeSuccess", enchant.getFullname(1).getString().replace(" I", ""), "" + (startLevel + 1), false, 1), (ServerPlayer) player);
                        }
                    }
                }
            }
        });

        return newEnchants;
    }
}
