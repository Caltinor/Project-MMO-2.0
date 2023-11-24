package harmonised.pmmo.features.autovalues;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.features.autovalues.AutoValueConfig.AttributeKey;
import harmonised.pmmo.features.autovalues.AutoValueConfig.UtensilTypes;
import harmonised.pmmo.features.autovalues.AutoValueConfig.WearableTypes;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoItem {
	//Default values set by MC or used by wooden items
	private static final double BASE_ATK_SPD = 1.6;
	private static final double BASE_DURABILITY = 59d;
	private static final double BASE_DAMAGE = 4;
	
	public static final ReqType[] REQTYPES = {ReqType.WEAR, ReqType.USE_ENCHANTMENT, ReqType.TOOL, ReqType.WEAPON};
	public static final EventType[] EVENTTYPES = {EventType.ANVIL_REPAIR, EventType.BLOCK_PLACE, EventType.CRAFT,
			EventType.CONSUME, EventType.ENCHANT, EventType.FISH, EventType.SMELT};	

	public static Map<String, Integer> processReqs(ReqType type, ResourceLocation stackID) {
		//exit early if the event type is not valid for an item
		if (!type.itemApplicable) 
			return new HashMap<>();
		
		final Map<String, Integer> outMap = new HashMap<>();
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(stackID));
		switch (type) {
		case WEAR: {
			if (stack.getItem() instanceof TieredItem) {
				if (stack.getItem() instanceof SwordItem) 
					outMap.putAll(getUtensilData(UtensilTypes.SWORD, type, stack, true));
				else if (stack.getItem() instanceof AxeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.AXE, type, stack, false));
				else if (stack.getItem() instanceof PickaxeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.PICKAXE, type, stack, false));
				else if (stack.getItem() instanceof ShovelItem) 
					outMap.putAll(getUtensilData(UtensilTypes.SHOVEL, type, stack, false));
				else if (stack.getItem() instanceof HoeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.HOE, type, stack, false));
			}
			else if (stack.getItem() instanceof ArmorItem)
				outMap.putAll(getWearableData(type, stack, true));
			else if (stack.getItem() instanceof ElytraItem) 
				outMap.putAll(getWearableData(type, stack, false));
			break;
		}
		case USE_ENCHANTMENT: {
			double scale = 0;
			for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()){
				scale += entry.getValue() / entry.getKey().getMaxLevel();
			}
			for (Map.Entry<String, Integer> entry : AutoValueConfig.getItemReq(type).entrySet()) {
				outMap.put(entry.getKey(), (int)((double)entry.getValue() * scale));
			}
			break;
		}
		case TOOL: {
			if (stack.getItem() instanceof TieredItem) {
				if (stack.getItem() instanceof SwordItem) 
					outMap.putAll(getUtensilData(UtensilTypes.SWORD, type, stack, false));
				else if (stack.getItem() instanceof AxeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.AXE, type, stack, false));
				else if (stack.getItem() instanceof PickaxeItem)
					outMap.putAll(getUtensilData(UtensilTypes.PICKAXE, type, stack, false));
				else if (stack.getItem() instanceof ShovelItem) 
					outMap.putAll(getUtensilData(UtensilTypes.SHOVEL, type, stack, false));
				else if (stack.getItem() instanceof HoeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.HOE, type, stack, false));
			}
			break;
		}
		case WEAPON: {
			if (stack.getItem() instanceof TieredItem) {
				if (stack.getItem() instanceof SwordItem)
					outMap.putAll(getUtensilData(UtensilTypes.SWORD, type, stack, true));
				else if (stack.getItem() instanceof AxeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.AXE, type, stack, true));
				else if (stack.getItem() instanceof PickaxeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.PICKAXE, type, stack, true));
				else if (stack.getItem() instanceof ShovelItem) 
					outMap.putAll(getUtensilData(UtensilTypes.SHOVEL, type, stack, true));
				else if (stack.getItem() instanceof HoeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.HOE, type, stack, true));
			}
			break;
		}
		case BREAK: {
			if (stack.getItem() instanceof BlockItem) {
				outMap.putAll(AutoBlock.processReqs(type, stackID));
			}
			break;
		}
		default: 
		}
		return outMap;
	}
	
	public static Map<String, Long> processXpGains(EventType type, ResourceLocation stackID) {
		//exit early if the event type is not valid for an item
		if (!type.itemApplicable)
			return new HashMap<>();
		
		Map<String, Long> outMap = new HashMap<>();
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(stackID));
		switch (type) {
		case ANVIL_REPAIR: {
			if (stack.isRepairable()) {
				AutoValueConfig.getItemXpAward(type).forEach((skill, xp) -> {
					outMap.put(skill, (long) (xp * (stack.getMaxDamage()*0.25)));
				});
			}
			break;
		}
		case BLOCK_PLACE: case BLOCK_BREAK:{
			if (stack.getItem() instanceof BlockItem) {
				outMap.putAll(AutoBlock.processXpGains(type, RegistryUtil.getId(((BlockItem)stack.getItem()).getBlock())));
			}
			break;
		}
		case CRAFT: {
			if (stack.getItem() instanceof TieredItem) {
				if (stack.getItem() instanceof SwordItem) 
					outMap.putAll(getUtensilData(UtensilTypes.SWORD, type, stack, true));
				else if (stack.getItem() instanceof AxeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.AXE, type, stack, false));
				else if (stack.getItem() instanceof PickaxeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.PICKAXE, type, stack, false));
				else if (stack.getItem() instanceof ShovelItem) 
					outMap.putAll(getUtensilData(UtensilTypes.SHOVEL, type, stack, false));
				else if (stack.getItem() instanceof HoeItem) 
					outMap.putAll(getUtensilData(UtensilTypes.HOE, type, stack, false));
			}
			else if (stack.getItem() instanceof ArmorItem) 					
				outMap.putAll(getWearableData(type, stack, true));
			else if (stack.getItem() instanceof ElytraItem) 
				outMap.putAll(getWearableData(type, stack, false));
			else
				outMap.putAll(AutoValueConfig.getItemXpAward(type));
			break;
		}
		case CONSUME: {
			if (stack.isEdible()) {
				AutoValueConfig.getItemXpAward(type).forEach((skill, xp) -> {
					@SuppressWarnings("deprecation")
					FoodProperties properties = stack.getItem().getFoodProperties();
					Float nutritionScale = (float)properties.getNutrition() * properties.getSaturationModifier();
					Float xpAward = nutritionScale * (float) xp;
					outMap.put(skill, xpAward.longValue());
				});
			}
		}
		case BREW: {
			if (ForgeRegistries.ITEMS.tags().getTag(Reference.BREWABLES).contains(stack.getItem()))
				outMap.putAll(AutoValueConfig.BREWABLES_OVERRIDE.get());
			break;
		}		
		case SMELT: {
			if (ForgeRegistries.ITEMS.tags().getTag(Reference.SMELTABLES).contains(stack.getItem()))
				outMap.putAll(AutoValueConfig.SMELTABLES_OVERRIDE.get());
			break;
		}
		case ENCHANT: case FISH: {
			//The proportion calculation for enchant is handled in the event, we just need a default skill/value
			outMap.putAll(AutoValueConfig.getItemXpAward(type));
			break;
		}		
		default:
		}
		return outMap;
	}
	
	//=========================ITEM CLASS SPECIFIC METHODS==============================================
	private static Map<String, Integer> getUtensilData(UtensilTypes utensil, ReqType type, ItemStack stack, boolean asWeapon) {
		Map<String, Integer> outMap = new HashMap<>();
		//if the item being evaluated is a basic item, return a blank map
		if (stack.getItem() instanceof TieredItem && getTier((TieredItem) stack.getItem()) <= 0 ) 
			return outMap;
		
		final double scale = getUtensilAttributes(utensil, stack, asWeapon);
		Map<String, Integer> configValue = type == ReqType.TOOL || (type == ReqType.WEAR && !asWeapon)
				? AutoValueConfig.getToolReq(stack) 
				: (type == ReqType.WEAR && asWeapon) 
					? AutoValueConfig.getItemReq(ReqType.WEAPON)
					: AutoValueConfig.getItemReq(type);					
		configValue.forEach((skill, level) -> {
			outMap.put(skill, (int)Math.max(0, (double)level * (scale)));
		});
		MsLoggy.DEBUG.log(LOG_CODE.AUTO_VALUES, "AutoItem Req Map: "+MsLoggy.mapToString(outMap));
		return outMap;
	}
	private static Map<String, Long> getUtensilData(UtensilTypes utensil, EventType type, ItemStack stack, boolean asWeapon) {
		Map<String, Long> outMap = new HashMap<>();
		final double scale = getUtensilAttributes(utensil, stack, asWeapon);
		AutoValueConfig.getItemXpAward(type).forEach((skill, level) -> {
			outMap.put(skill, Double.valueOf(Math.max(0,(double)level * scale)).longValue());
		});
		MsLoggy.DEBUG.log(LOG_CODE.AUTO_VALUES, "AutoItem XpGain Map: "+MsLoggy.mapToString(outMap));
		return outMap;
	}
	private static Map<String, Integer> getWearableData(ReqType type, ItemStack stack, boolean isArmor) {
		Map<String, Integer> outMap = new HashMap<>();
		//if the item being evaluated is a basic item, return a blank map
		if (stack.getItem() instanceof ArmorItem && ((ArmorItem)stack.getItem()).getMaterial().equals(ArmorMaterials.LEATHER)) 
			return outMap;
		
		final double scale = getWearableAttributes(WearableTypes.fromSlot(LivingEntity.getEquipmentSlotForItem(stack), !isArmor), stack, isArmor);
		AutoValueConfig.getItemReq(type).forEach((skill, level) -> {
			outMap.put(skill, (int)Math.max(0, (double)level * (scale)));
		});
		return outMap;
	}
	private static Map<String, Long> getWearableData(EventType type, ItemStack stack, boolean isArmor) {
		Map<String, Long> outMap = new HashMap<>();
		final double scale = getWearableAttributes(WearableTypes.fromSlot(LivingEntity.getEquipmentSlotForItem(stack), !isArmor), stack, isArmor);
		AutoValueConfig.getItemXpAward(type).forEach((skill, level) -> {
			outMap.put(skill, Double.valueOf(Math.max(0, (double)level * scale)).longValue());
		});
		return outMap;
	}

	//=========================UTILITY METHODS==============================================
 	private static double getAttributeAmount(ItemStack stack, EquipmentSlot slot, Attribute attribute) {
		return stack.getAttributeModifiers(slot).get(attribute).stream().collect(Collectors.summingDouble(a -> a.getAmount()));
	}
	@SuppressWarnings("deprecation")
	private static double getTier(TieredItem item) {
		return (double)item.getTier().getLevel();
	}
	private static double getDamage(ItemStack stack) {
		return (getAttributeAmount(stack, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE) + EnchantmentHelper.getDamageBonus(stack, null) - BASE_DAMAGE);
	}
	private static double getAttackSpeed(ItemStack stack) {
		return (Math.abs(getAttributeAmount(stack, EquipmentSlot.MAINHAND, Attributes.ATTACK_SPEED)) - BASE_ATK_SPD);
	}
	private static double getDurability(ItemStack stack) {
		return ((double)stack.getMaxDamage() - BASE_DURABILITY);
	}
	
	private static double getUtensilAttributes(UtensilTypes type, ItemStack stack, boolean asWeapon) {
		//Universally Used Attributes
		double durabilityScale = getDurability(stack) * AutoValueConfig.getUtensilAttribute(type, AttributeKey.DUR);		
		double tierScale = getTier((TieredItem) stack.getItem()) * AutoValueConfig.getUtensilAttribute(type, AttributeKey.TIER);
		//Weapon specific
		double damageScale = asWeapon ? getDamage(stack) * AutoValueConfig.getUtensilAttribute(type, AttributeKey.DMG) : 0d;
		double atkSpdScale = asWeapon ? getAttackSpeed(stack) * AutoValueConfig.getUtensilAttribute(type, AttributeKey.SPD) : 0d;
		//Tool specified
		double digSpeedScale = asWeapon ? 0d : stack.getDestroySpeed(Blocks.COBWEB.defaultBlockState()) + AutoValueConfig.getUtensilAttribute(type, AttributeKey.DIG);
		MsLoggy.DEBUG.log(LOG_CODE.AUTO_VALUES, "AutoItem Attributes: DUR="+durabilityScale+" TIER="+tierScale+" DMG="+damageScale+" SPD="+atkSpdScale+" DIG="+digSpeedScale);
		return damageScale + atkSpdScale + digSpeedScale + durabilityScale + tierScale;
	}
	
	private static double getWearableAttributes(WearableTypes type, ItemStack stack, boolean isArmor) {
		//Universally Used Attributes
		double durabilityScale = (double)stack.getMaxDamage() * AutoValueConfig.getWearableAttribute(type, AttributeKey.DUR);
		//Armor Specific
		ArmorMaterial material = isArmor ? ((ArmorItem)stack.getItem()).getMaterial() : null;
		double armorScale = isArmor ? material.getDefenseForType(((ArmorItem)stack.getItem()).getType()) * AutoValueConfig.getWearableAttribute(type, AttributeKey.AMR) : 0d;
		double toughnessScale = isArmor? material.getToughness() * AutoValueConfig.getWearableAttribute(type, AttributeKey.TUF) : 0d;
		double knockbackScale = isArmor? material.getKnockbackResistance() * AutoValueConfig.getWearableAttribute(type, AttributeKey.KBR) : 0d;
		//return and log output
		MsLoggy.DEBUG.log(LOG_CODE.AUTO_VALUES, "AutoItem Attributes: DUR="+durabilityScale+" ARM="+armorScale+" TUF="+toughnessScale+" KBR="+knockbackScale);
		return durabilityScale + armorScale + toughnessScale + knockbackScale;
	}
}
