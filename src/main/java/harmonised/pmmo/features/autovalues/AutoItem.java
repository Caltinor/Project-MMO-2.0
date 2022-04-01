package harmonised.pmmo.features.autovalues;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoItem {

	public static Map<String, Integer> processReqs(ReqType type, ResourceLocation stackID) {
		//exit early if the event type is not valid for an item
		if (!type.itemApplicable) 
			return new HashMap<>();
		
		Map<String, Integer> outMap = new HashMap<>();
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(stackID));
		if (type.equals(ReqType.WEAR)) {
			if (stack.getItem() instanceof TieredItem) {
				if (stack.getItem() instanceof SwordItem) {
					
				}
				else if (stack.getItem() instanceof AxeItem) {
					
				}
				else if (stack.getItem() instanceof PickaxeItem) {
					
				}
				else if (stack.getItem() instanceof ShovelItem) {
	
				}
				else if (stack.getItem() instanceof HoeItem) {
	
				}
			}
			else if (stack.getItem() instanceof ArmorItem) {
				if (stack.getItem() instanceof ElytraItem) {
					
				}
				else {
					
				}
			}
		}
		else if (type.equals(ReqType.USE_ENCHANTMENT)) {
			
		}
		else if (type.equals(ReqType.TOOL)) {
			
		}
		else if (type.equals(ReqType.WEAPON)) {
			
		}
		else if (type.equals(ReqType.USE)) {
			
		}
		else if (type.equals(ReqType.PLACE)) {
			
		}
		else if (type.equals(ReqType.BREAK)) {
			
		}
		else if (type.equals(ReqType.INTERACT)) {
			
		}
		
		return outMap;
	}
	
	public static Map<String, Long> processXpGains(EventType type, ResourceLocation stackID) {
		//exit early if the event type is not valid for an item
		if (!type.itemApplicable)
			return new HashMap<>();
		
		Map<String, Long> outMap = new HashMap<>();
		ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(stackID));
		if (type.equals(EventType.ANVIL_REPAIR)) {
			if (stack.isDamageableItem()) {
				
			}
		}
		else if (type.equals(EventType.BLOCK_PLACE)) {
			if (stack.getItem() instanceof BlockItem) {
				
			}
		}
		else if (type.equals(EventType.BREW)) {
			
		}
		else if (type.equals(EventType.CRAFT)) {
			
		}
		else if (type.equals(EventType.ENCHANT)) {
			
		}
		else if (type.equals(EventType.FISH)) {
			
		}
		else if (type.equals(EventType.SMELT)) {
			
		}
		else if (type.equals(EventType.ACTIVATE_ITEM)) {
			
		}
		
		return outMap;	
	}
}
