package harmonised.pmmo.features.salvaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Preconditions;

import harmonised.pmmo.config.datapack.codecs.CodecTypeSalvage;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class SalvageLogic {
	public SalvageLogic() {}
	
	private Map<ResourceLocation, Map<ResourceLocation, CodecTypeSalvage>> salvageData = new HashMap<>();
	private Random rand = new Random();
	
	public void setSalvageData(ResourceLocation itemID, ResourceLocation outputID, CodecTypeSalvage data) {
		Preconditions.checkNotNull(itemID);
		Preconditions.checkNotNull(outputID);
		Preconditions.checkNotNull(data);
		salvageData.computeIfAbsent(itemID,s -> new HashMap<>()).put(outputID, data);
	}
	
	public List<ItemStack> getSalvageResults(ResourceLocation salvagedItem, Map<String, Long> playerXp) {
		List<ItemStack> out = new ArrayList<>();
		if (!salvageData.containsKey(salvagedItem)) return out;
		
		Map<ResourceLocation, CodecTypeSalvage> resultData = salvageData.get(salvagedItem);
		for (Map.Entry<ResourceLocation, CodecTypeSalvage> result : resultData.entrySet()) {
			//First look for any skills that do not meet the req and continue to the next output 
			//item if the req is not met. 
			for (Map.Entry<String, Integer> skill : result.getValue().levelReq().entrySet()) {
				if (skill.getValue() > PmmoSavedData.get().getLevelFromXP(playerXp.getOrDefault(skill.getKey(), 0l))) continue;
			}
			
			//get the base calculation values including the bonuses from skills
			double base = result.getValue().baseChance();
			double max = result.getValue().maxChance();
			double bonus = 0d;
			for (Map.Entry<String, Double> skill : result.getValue().chancePerLevel().entrySet()) {
				bonus += skill.getValue() * PmmoSavedData.get().getLevelFromXP(playerXp.getOrDefault(skill.getKey(), 0l));
			}
			
			//conduct random check for the total count possible and add each succcess to the output
			for (int i = 0; i < result.getValue().salvageMax(); i++) {
				if (rand.nextDouble(1d) < Math.min(max, base + bonus)) {
					out.add(new ItemStack(ForgeRegistries.ITEMS.getValue(result.getKey())));
				}
			}
		}
		
		return out;
	}
}
