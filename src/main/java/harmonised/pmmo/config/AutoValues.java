package harmonised.pmmo.config;

import com.google.common.collect.Multimap;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.*;

import java.util.*;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.OreBlock;

public class AutoValues
{
    public static final Logger LOGGER = LogManager.getLogger();
    private static boolean outputToConsole = Config.forgeConfig.outputAllAutoValuesToLog.get();

    public static Set<Item> itemsWithCookRecipe = new HashSet<>();
    public static Set<Item> itemsWithCraftRecipe = new HashSet<>();
    public static Set<Item> itemsWithBlastRecipe = new HashSet<>();
    public static Map<Item, Set<Item>> cooksFrom = new HashMap<>();

    private static void addJsonConfigValue(String resLoc, JType jType, Map<String, Double> values, boolean fillIfExists)
    {
        double value;
        boolean hadEntry = JsonConfig.localData.get(jType).containsKey(resLoc);
        if(!hadEntry)
            JsonConfig.localData.get(jType).put(resLoc, new HashMap<>());

        if(!hadEntry || fillIfExists)
        {
            for(Map.Entry<String, Double> entry : values.entrySet())
            {
                value = entry.getValue();
                if(JsonConfig.levelJTypes.contains(jType))
                    value = Math.max(1, Math.min(Config.forgeConfig.maxLevel.get(), value));
                if(!JsonConfig.localData.get(jType).get(resLoc).containsKey(entry.getKey()))
                    JsonConfig.localData.get(jType).get(resLoc).put(entry.getKey(), value);
                if(outputToConsole)
                {
                    //addData("xp_value_smelt", 	"#forge:ores/aluminum",	{ "smithing": 12.5 });
                    StringBuilder addDataString = new StringBuilder("addData(\"" + jType.toString().toLowerCase() + "\",\t\"" + resLoc + "\", { ");
                    boolean firstEntry = true;
                    for(Map.Entry<String, Double> jsonEntry : values.entrySet())
                    {
                        if(!firstEntry)
                            addDataString.append(", ");
                        else
                            firstEntry = false;
                        addDataString.append("\"").append(jsonEntry.getKey()).append("\": ").append(jsonEntry.getValue());
                    }
                    addDataString.append(" });");
                    LOGGER.info(addDataString.toString());
                }
            }
            if(JsonConfig.localData.get(jType).get(resLoc).size() == 0)
                JsonConfig.localData.get(jType).remove(resLoc);
        }
    }

    private static Map<Attribute, AttributeModifier> mergeMultimaps(Multimap<Attribute, AttributeModifier> ... maps)
    {
        Map<Attribute, AttributeModifier> output = new HashMap<>();

        for(Multimap<Attribute, AttributeModifier> map : maps)
        {
            for(Map.Entry<Attribute, AttributeModifier> entry : map.entries())
            {
                output.put(entry.getKey(), entry.getValue());
            }
        }

        return output;
    }

    public static double getWearReqFromStack(ItemStack itemStack)
    {
        Multimap<Attribute, AttributeModifier> headHandAttributes = itemStack.getAttributeModifiers(EquipmentSlot.HEAD);
        Multimap<Attribute, AttributeModifier> chestHandAttributes = itemStack.getAttributeModifiers(EquipmentSlot.CHEST);
        Multimap<Attribute, AttributeModifier> legsHandAttributes = itemStack.getAttributeModifiers(EquipmentSlot.LEGS);
        Multimap<Attribute, AttributeModifier> feetHandAttributes = itemStack.getAttributeModifiers(EquipmentSlot.FEET);

        Map<Attribute, AttributeModifier> attributes = mergeMultimaps(headHandAttributes, chestHandAttributes, legsHandAttributes, feetHandAttributes);

        AttributeModifier armorAttribute = attributes.get(Attributes.ARMOR);
        AttributeModifier armorToughnessAttribute = attributes.get(Attributes.ARMOR_TOUGHNESS);

        double armor            = armorAttribute          == null ? 0D : armorAttribute.getAmount();
        double armorToughness   = armorToughnessAttribute == null ? 0D : armorToughnessAttribute.getAmount();

        ResourceLocation regKey = itemStack.getItem().getRegistryName();
        double wearReqOffset = regKey == null ? 0D : XP.getJsonMap(regKey.toString(), JType.ITEM_SPECIFIC).getOrDefault("autoValueOffsetWear", 0D);
        double wearReq = Math.min(Config.forgeConfig.maxLevel.get(), Math.ceil(armor * Config.forgeConfig.armorReqScale.get() + armorToughness * Config.forgeConfig.armorToughnessReqScale.get() + wearReqOffset));

        if(Config.forgeConfig.autoGenerateRoundedValuesOnly.get())
            wearReq = Math.ceil(wearReq);

        if(wearReq > 1)
            wearReq += Config.getConfig("autoGenerateWearReqOffset");

        return Math.max(0, wearReq);
    }

    public static double getWeaponReqFromStack(ItemStack itemStack)
    {
        Multimap<Attribute, AttributeModifier> mainHandAttributes = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        Multimap<Attribute, AttributeModifier> offHandAttributes = itemStack.getAttributeModifiers(EquipmentSlot.OFFHAND);

        Map<Attribute, AttributeModifier> attributes = mergeMultimaps(mainHandAttributes, offHandAttributes);

        AttributeModifier attackSpeedAttribute = attributes.get(Attributes.ATTACK_SPEED);
        AttributeModifier attackDamageAttribute = attributes.get(Attributes.ATTACK_DAMAGE);

        double attackSpeed      = attackSpeedAttribute    == null ? 0D : attackSpeedAttribute.getAmount();
        double attackDamage     = attackDamageAttribute   == null ? 0D : attackDamageAttribute.getAmount();

        ResourceLocation regKey = itemStack.getItem().getRegistryName();
        double weaponReqOffset = regKey == null ? 0D : XP.getJsonMap(regKey.toString(), JType.ITEM_SPECIFIC).getOrDefault("autoValueOffsetWeapon", 0D);
        double weaponReq = Math.min(XP.getMaxLevel(), Math.ceil((attackDamage) * Config.forgeConfig.attackDamageReqScale.get() * (4+attackSpeed) + weaponReqOffset));

        if(Config.forgeConfig.autoGenerateRoundedValuesOnly.get())
            weaponReq = Math.ceil(weaponReq);

        if(weaponReq > 0)
            weaponReq += Config.getConfig("autoGenerateWeaponReqOffset");

        return Math.max(0, weaponReq);
    }

    public static Map<String, Double> getToolReqFromStack(ItemStack itemStack)
    {
        Map<String, Double> reqTool = new HashMap<>();
        double speed, toolReq;

        ResourceLocation regKey = itemStack.getItem().getRegistryName();
        double toolReqOffset = regKey == null ? 0D : XP.getJsonMap(regKey.toString(), JType.ITEM_SPECIFIC).getOrDefault("autoValueOffsetTool", 0D);

        //Woodcutting
        speed = itemStack.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState());
        toolReq = Math.max(1, Math.min(Config.forgeConfig.maxLevel.get(), speed * Config.forgeConfig.toolReqScaleLog.get() + toolReqOffset));
        if(toolReq > 5)
            reqTool.put(Skill.WOODCUTTING.toString(), toolReq);

        //Mining
        speed = itemStack.getDestroySpeed(Blocks.STONE.defaultBlockState());
        toolReq = Math.max(1, Math.min(Config.forgeConfig.maxLevel.get(), speed * Config.forgeConfig.toolReqScaleOre.get() + toolReqOffset));
        if(toolReq > 5)
            reqTool.put(Skill.MINING.toString(), toolReq);

        //Excavation
        speed = itemStack.getDestroySpeed(Blocks.DIRT.defaultBlockState());
        toolReq = Math.max(1, Math.min(Config.forgeConfig.maxLevel.get(), speed * Config.forgeConfig.toolReqScaleDirt.get() + toolReqOffset));
        if(toolReq > 5)
            reqTool.put(Skill.EXCAVATION.toString(), toolReq);

        if(Config.forgeConfig.autoGenerateRoundedValuesOnly.get())
            XP.ceilMapAnyDouble(reqTool);

        for(String skill : reqTool.keySet())
        {
            double level = reqTool.get(skill);
            if(level > 1)
                reqTool.replace(skill, Math.max(0, level + Config.getConfig("autoGenerateToolReqOffset")));
        }

        return reqTool;
    }

    public static String getItemSpecificSkillOrDefault(String resLoc, String defaultSkill)
    {
        String skill = getItemSpecificSkill(resLoc);
        return skill == null ? defaultSkill : skill;
    }

    public static String getItemSpecificSkill(String resLoc)
    {
        Map<String, Double> itemSpecificMap = JsonConfig.data.get(JType.ITEM_SPECIFIC).getOrDefault(resLoc.toString(), new HashMap<>());
        String skill = null;

        if(itemSpecificMap.getOrDefault("archeryWeapon", 0D) != 0)
            skill = Skill.ARCHERY.toString();
        else if(itemSpecificMap.getOrDefault("magicWeapon", 0D) != 0)
            skill = Skill.MAGIC.toString();
        else if(itemSpecificMap.getOrDefault("meleeWeapon", 0D) != 0)
            skill = Skill.COMBAT.toString();
        else if(itemSpecificMap.getOrDefault("gunWeapon", 0D) != 0)
            skill = Skill.GUNSLINGING.toString();

        return skill;
    }
    
    public static ListTag getItemSpecificWeaponTypes(String resLoc) {
    	ListTag list = new ListTag();
    	Map<String, Double> itemSpecificMap = JsonConfig.data.get(JType.ITEM_SPECIFIC).getOrDefault(resLoc.toString(), new HashMap<>());
    	if (itemSpecificMap.getOrDefault("meleeWeapon", 0d) == 1d) list.add(StringTag.valueOf("meleeWeapon"));
    	if (itemSpecificMap.getOrDefault("archeryWeapon", 0d) == 1d) list.add(StringTag.valueOf("archeryWeapon"));
    	if (itemSpecificMap.getOrDefault("magicWeapon", 0d) == 1d) list.add(StringTag.valueOf("magicWeapon"));
    	if (itemSpecificMap.getOrDefault("gunWeapon", 0d) == 1d) list.add(StringTag.valueOf("gunWeapon"));
    	return list;
    }

    public static void setAutoValues()
    {
        if(Config.forgeConfig.autoGenerateValuesEnabled.get())
        {
            Collection<Recipe<?>> allRecipes = PmmoSavedData.getServer().getRecipeManager().getRecipes();
            itemsWithCookRecipe.clear();
            itemsWithCraftRecipe.clear();
            itemsWithBlastRecipe.clear();

            for(Recipe<?> recipe : allRecipes)
            {
                if(recipe == null || recipe.getResultItem() == null)
                    continue;
                Item item = recipe.getResultItem().getItem();
                if(recipe.getType() == RecipeType.CRAFTING)
                    itemsWithCraftRecipe.add(item);
                else if(recipe.getType() == RecipeType.SMOKING)
                {
                    Item outputItem = recipe.getResultItem().getItem();
                    itemsWithCookRecipe.add(outputItem);
                    for(Ingredient ingredient : recipe.getIngredients())
                    {
                        for(ItemStack itemStack : ingredient.getItems())
                        {
                            if(!cooksFrom.containsKey(outputItem))
                                cooksFrom.put(outputItem, new HashSet<>());
                            cooksFrom.get(outputItem).add(itemStack.getItem());
                        }
                    }
                }
//                else if(recipe.getType() == IRecipeType.BLASTING)
//                {
//                    itemsWithBlastRecipe.add(item);
//                }
            }

            for(Item item : ForgeRegistries.ITEMS)
            {
                try
                {
                    ItemStack itemStack = new ItemStack(item);
                    String resLoc = item.getRegistryName().toString();
                    String wearReqSkill = Config.forgeConfig.autoGenerateWearReqAsCombat.get() ? Skill.COMBAT.toString() : Skill.ENDURANCE.toString();

                    double enduranceReq = getWearReqFromStack(itemStack) + XP.getJsonMap(resLoc, JType.ITEM_SPECIFIC).getOrDefault("autoValueOffsetWear", 0D);
                    double combatReq = getWeaponReqFromStack(itemStack) + XP.getJsonMap(resLoc, JType.ITEM_SPECIFIC).getOrDefault("autoValueOffsetWeapon", 0D);
                    Map<String, Double> reqTool = getToolReqFromStack(itemStack);

                    //Wear Req
                    if(enduranceReq > 1 && Config.forgeConfig.wearReqEnabled.get() && Config.forgeConfig.autoGenerateWearReqEnabled.get())
                    {
                        Map<String, Double> reqWear     = new HashMap<>();
                        reqWear.put(wearReqSkill, Math.max(1, enduranceReq));
                        addJsonConfigValue(resLoc, JType.REQ_WEAR, reqWear, false);
                    }

                    //Weapon Req
                    if(combatReq > 1 && Config.forgeConfig.weaponReqEnabled.get() && Config.forgeConfig.autoGenerateWeaponReqEnabled.get())
                    {
                        Map<String, Double> reqWeapon   = new HashMap<>();
                        reqWeapon.put(getItemSpecificSkillOrDefault(item.getRegistryName().toString(), Skill.COMBAT.toString()),  Math.max(1, combatReq));
                        addJsonConfigValue(resLoc, JType.REQ_WEAPON, reqWeapon, false);
                    }

                    //Tool Req
                    if(reqTool.size() > 0 && Config.forgeConfig.toolReqEnabled.get() && Config.forgeConfig.autoGenerateToolReqEnabled.get())
                    {
                        addJsonConfigValue(resLoc, JType.REQ_TOOL, reqTool, true);
                    }

                    //Crafting Xp Value
                    if(Config.forgeConfig.autoGenerateCraftingXpEnabled.get())
                    {
                        double highestToolReq = reqTool.values().stream().reduce(Math::max).orElse(0D);

                        double craftingXp = 0;
                        double smithingXp = 0;
                        double cookingXp = 0;

                        if(enduranceReq > 0 || combatReq > 0 || highestToolReq > 0)
                        {
                            craftingXp = enduranceReq * 10D +                           Math.max(Math.max(combatReq - 10, 1) * 5D,  (Math.max(highestToolReq - 10, 1)) * 5D);
                            smithingXp = (Math.max(enduranceReq - 10, 1)) * 5D  +   Math.max(Math.max(combatReq - 20, 1) * 2D,  (Math.max(highestToolReq - 20, 1)) * 2D);

                            craftingXp *= Config.forgeConfig.autoGeneratedCraftingXpValueMultiplierCrafting.get();
                            smithingXp *= Config.forgeConfig.autoGeneratedCraftingXpValueMultiplierSmithing.get();
                        }

                        Map<String, Double> xpValueMap = new HashMap<>();

                        if(item.isEdible())
                        {
                            FoodProperties food = item.getFoodProperties();
                            if(food != null)
                            {
                                float saturation = food.getSaturationModifier();
                                float healing = food.getNutrition();
                                cookingXp = saturation * 15 + healing;

                                if(cookingXp > 0)
                                    cookingXp *= Config.forgeConfig.autoGeneratedCraftingXpValueMultiplierCooking.get();
                            }
                        }

                        if(craftingXp > 0)
                            xpValueMap.put(Skill.CRAFTING.toString(), craftingXp);
                        if(smithingXp > 0)
                            xpValueMap.put(Skill.SMITHING.toString(), smithingXp);
                        if(cookingXp > 0)
                        {
                            if(itemsWithCraftRecipe.contains(item))
                                xpValueMap.put(Skill.COOKING.toString(), cookingXp);
                        }
                        addJsonConfigValue(resLoc, JType.XP_VALUE_CRAFT, xpValueMap, true);
                    }

                    //Cooking Xp Value
                    if((Config.forgeConfig.autoGenerateCookingXpEnabled.get() || Config.forgeConfig.autoGenerateCookingExtraChanceEnabled.get()) && itemsWithCookRecipe.contains(item))
                    {
                        double cookingXp = 0;
                        if(item.isEdible())
                        {
                            FoodProperties food = item.getFoodProperties();
                            if(food != null)
                            {
                                float saturation = food.getSaturationModifier();
                                float healing = food.getNutrition();
                                cookingXp = saturation * 15 + healing;
                                cookingXp *= Config.forgeConfig.autoGeneratedCraftingXpValueMultiplierCooking.get();

                                if(cookingXp > 0)
                                {
                                    Map<String, Double> xpValueMap = new HashMap<>();
                                    xpValueMap.put(Skill.COOKING.toString(), cookingXp);
                                    for(Item rawItem : cooksFrom.get(item))
                                    {
                                        String rawResLoc = rawItem.getRegistryName().toString();
                                        if(Config.forgeConfig.autoGenerateCookingXpEnabled.get())
                                            addJsonConfigValue(rawResLoc, JType.XP_VALUE_COOK, xpValueMap, true);
                                        if(Config.forgeConfig.autoGenerateCookingExtraChanceEnabled.get())
                                        {
                                            Map<String, Double> extraChanceMap = new HashMap<>();
                                            extraChanceMap.put("extraChance", 10D/(saturation*15+healing));
                                            addJsonConfigValue(rawResLoc, JType.INFO_COOK, extraChanceMap, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception e)
                {
                    LOGGER.debug(e);
                }
            }
            if(Config.forgeConfig.autoGenerateExtraChanceEnabled.get())
            {
                for(Block block : ForgeRegistries.BLOCKS)
                {
                    try
                    {
//                ItemStack itemStack = new ItemStack(block);
                        String resLoc = block.getRegistryName().toString();
                        JType jType = JType.NONE;
                        Map<String, Double> infoMap = new HashMap<>();
                        double chance = 0;
                        Set<ResourceLocation> tags = block.getTags();

                        //Ore/Log/Plant Extra Chance
                        if(block instanceof OreBlock || tags.contains(new ResourceLocation("forge:ores")))
                        {
                            jType = JType.INFO_ORE;
                            chance = Config.forgeConfig.defaultExtraChanceOre.get();
                        }
                        else if(block instanceof CropBlock || tags.contains(new ResourceLocation("minecraft:crops")))
                        {
                            jType = JType.INFO_PLANT;
                            chance = Config.forgeConfig.defaultExtraChancePlant.get();
                        }
                        else if(tags.contains(new ResourceLocation("minecraft:logs")))
                        {
                            jType = JType.INFO_LOG;
                            chance = Config.forgeConfig.defaultExtraChanceLog.get();
                        }
                        if(!jType.equals(JType.NONE))
                            infoMap.put("extraChance", chance);

                        if(infoMap.size() > 0 && infoMap.getOrDefault("extraChance", 0D) > 0)
                            addJsonConfigValue(resLoc, jType, infoMap, false);
                    }
                    catch(Exception e)
                    {
                        LOGGER.error(e);
                    }
                }
            }

            JsonConfig.data = JsonConfig.localData;
        }
    }
}