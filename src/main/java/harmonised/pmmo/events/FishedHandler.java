package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class FishedHandler
{
    public static double getFishPoolChance(PlayerEntity player)
    {
        double fishPoolBaseChance = Config.getConfig("fishPoolBaseChance");
        double fishPoolChancePerLevel = Config.getConfig("fishPoolChancePerLevel");
        double fishPoolMaxChance = Config.getConfig("fishPoolMaxChance");
        return Math.min(fishPoolMaxChance, fishPoolBaseChance + fishPoolChancePerLevel * Skill.getLevelDecimal(Skill.FISHING.toString(), player)) / 100D;
    }

    public static void handleFished(ItemFishedEvent event)
    {
        if(!(event.getPlayer() instanceof ServerPlayerEntity))
            return;
        if(Config.forgeConfig.disableNormalFishDrops.get())
            event.setCanceled(true);
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        int startLevel = Skill.getLevel(Skill.FISHING.toString(), player);
        int level;
        NonNullList<ItemStack> items = event.getDrops();
        double award = 10D;
        for(ItemStack itemStack : items)
        {
            Map<String, Double> itemXp = XP.getXp(itemStack, JType.XP_VALUE_GENERAL);
            if(itemXp.containsKey("fishing"))
                award = itemXp.get("fishing");
        }
        Map<String, Map<String, Double>> fishPool = JsonConfig.data.get(JType.FISH_POOL);

        if(fishPool != null)
        {
            double fishPoolChance = getFishPoolChance(player);
            if(Math.random() < fishPoolChance)
            {
                String matchKey = null;
                Map<String, Double> match = new HashMap<>();

                double totalWeight = getTotalFishPoolWeight(startLevel);
                double weight;
                double result;
                double currentWeight = 0;
                int count, minCount, maxCount;

                result = Math.floor(Math.random() * (totalWeight + 1));

                for(Map.Entry<String, Map<String, Double>> entry : fishPool.entrySet())
                {
                    weight = getFishPoolWeight(startLevel, entry.getValue());

                    if(currentWeight < result && currentWeight + weight >= result)
                    {
                        matchKey = entry.getKey();
                        match = new HashMap<>(entry.getValue());
                        break;
                    }

                    currentWeight += weight;
                }

                if(matchKey == null)
                    return;

                Item item = XP.getItem(matchKey);

                minCount = (int) Math.floor(match.get("minCount"));
                maxCount = (int) Math.floor(match.get("maxCount"));

                count = (int) Math.floor((Math.random() * maxCount) + minCount);

                ItemStack itemStack = new ItemStack(item, count);

                if(itemStack.isDamageable())
                    itemStack.setDamage((int) Math.floor(Math.random() * itemStack.getMaxDamage()));

                if(itemStack.isEnchantable())
                {
                    Map<String, Map<String, Double>> enchantMap = JsonConfig.data.get(JType.FISH_ENCHANT_POOL);
                    Map<Enchantment, Integer> outEnchants = new HashMap<>();

                    for(Map.Entry<String, Map<String, Double>> entry : enchantMap.entrySet())
                    {
                        Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(XP.getResLoc(entry.getKey()));
                        Map<String, Double> enchantInfo = entry.getValue();

                        if(enchant.canApply(itemStack))
                        {
                            int enchantLevelReq = (int) Math.floor(enchantInfo.get("levelReq"));
                            int itemLevelReq = (int) Math.floor(match.get("enchantLevelReq"));
                            int totalLevelReq = enchantLevelReq + itemLevelReq;
                            if(startLevel >= totalLevelReq)
                            {
                                level = startLevel - totalLevelReq;
                                double chancePerLevel = enchantInfo.get("chancePerLevel");
                                double maxChance = enchantInfo.get("maxChance");
                                double enchantChance = (chancePerLevel * level);
                                if(enchantChance > maxChance)
                                    enchantChance = maxChance;

                                double levelPerLevel = enchantInfo.get("levelPerLevel");
                                double maxEnchantLevel = enchantInfo.get("maxLevel");
                                int potentialEnchantLevel;

                                if(levelPerLevel > 0)
                                    potentialEnchantLevel = (int) Math.floor(level / levelPerLevel);
                                else
                                    potentialEnchantLevel = (int) Math.floor(maxEnchantLevel);

                                if(potentialEnchantLevel > maxEnchantLevel)
                                    potentialEnchantLevel = (int) Math.floor(maxEnchantLevel);

                                int enchantLevel = 0;

                                for(int i = 0; i < potentialEnchantLevel; i++)
                                {
                                    if(Math.random() * 10000 < enchantChance * 100)
                                    {
                                        enchantLevel++;
                                    }
//									else
//										break;
                                }

                                if(enchantLevel > 0)
                                    outEnchants.put(enchant, enchantLevel);
                            }
                        }
                    }

                    if(outEnchants.size() > 0)
                        EnchantmentHelper.setEnchantments(outEnchants, itemStack);
                }

                XP.dropItemStack(itemStack, player.world, player.getPositionVec());
                player.sendStatusMessage(new TranslationTextComponent("pmmo.extraFished", count, new TranslationTextComponent(itemStack.getTranslationKey())).setStyle(XP.textStyle.get("green")), true);
                player.sendStatusMessage(new TranslationTextComponent("pmmo.extraFished", count, new TranslationTextComponent(itemStack.getTranslationKey())).setStyle(XP.textStyle.get("green")), false);

                award += match.get("xp") * count;
            }


            Vector3d bobPos = event.getHookEntity().getPositionVec();
            Vector3d xpDropPos = new Vector3d(bobPos.getX(), bobPos.getY() + 2, bobPos.getZ());
            WorldXpDrop xpDrop = WorldXpDrop.fromVector(XP.getDimResLoc(player.getServerWorld()), xpDropPos, 0.5, award, Skill.FISHING.toString());
            xpDrop.setDecaySpeed(0.2);
            XP.addWorldXpDrop(xpDrop, player);
            XP.awardXp(player, Skill.FISHING.toString(), "catching " + items, award, false, false, false);
        }
    }
    
    public static double getTotalFishPoolWeight(double level)
    {
        double totalWeight = 0;
        for(Map.Entry<String, Map<String, Double>> entry : JsonConfig.data.get(JType.FISH_POOL).entrySet())
        {
            totalWeight += getFishPoolWeight(level, entry.getValue());
        }
        return totalWeight;
    }

    public static double getFishPoolWeight(double level, Map<String, Double> fishItem)
    {
        return Util.mapCapped(level, fishItem.get("startLevel"), fishItem.get("endLevel"), fishItem.get("startWeight"), fishItem.get("endWeight"));
    }
}