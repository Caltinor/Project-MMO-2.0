package harmonised.pmmo.features.salvaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Preconditions;

import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

public class SalvageLogic {
	public SalvageLogic() {}
	
	private Map<ResourceLocation, Map<ResourceLocation, SalvageData>> salvageData = new HashMap<>();
	private Random rand = new Random();
	
	public void reset() {
		salvageData = new HashMap<>();
	}
	
	public void setSalvageData(ResourceLocation itemID, ResourceLocation outputID, SalvageData data) {
		Preconditions.checkNotNull(itemID);
		Preconditions.checkNotNull(outputID);
		Preconditions.checkNotNull(data);
		salvageData.computeIfAbsent(itemID,s -> new HashMap<>()).put(outputID, data);
	}
	
	public void getSalvage(ServerPlayer player, Core core) {
		ItemStack salvageItem = player.getMainHandItem().isEmpty() 
				? player.getOffhandItem().isEmpty() 
						? ItemStack.EMPTY 
						: player.getOffhandItem()
				: player.getMainHandItem();
		boolean salvageMainHand = !player.getMainHandItem().isEmpty();
		boolean salvageOffHand = !salvageMainHand && !player.getMainHandItem().isEmpty();
		if (!salvageData.containsKey(salvageItem.getItem().getRegistryName())) return;
		Map<String, Long> playerXp = core.getData().getXpMap(player.getUUID());
		
		Map<String, Long> xpAwards = new HashMap<>();
		Map<ResourceLocation, SalvageData> resultData = salvageData.get(salvageItem.getItem().getRegistryName());
		for (Map.Entry<ResourceLocation, SalvageData> result : resultData.entrySet()) {
			//First look for any skills that do not meet the req and continue to the next output 
			//item if the req is not met. 
			for (Map.Entry<String, Integer> skill : result.getValue().levelReq().entrySet()) {
				if (skill.getValue() > Core.get(LogicalSide.SERVER).getData().getLevelFromXP(playerXp.getOrDefault(skill.getKey(), 0l))) continue;
			}
			
			//get the base calculation values including the bonuses from skills
			double base = result.getValue().baseChance();
			double max = result.getValue().maxChance();
			double bonus = 0d;
			for (Map.Entry<String, Double> skill : result.getValue().chancePerLevel().entrySet()) {
				bonus += skill.getValue() * Core.get(LogicalSide.SERVER).getData().getLevelFromXP(playerXp.getOrDefault(skill.getKey(), 0l));
			}
			
			//conduct random check for the total count possible and add each succcess to the output
			for (int i = 0; i < result.getValue().salvageMax(); i++) {
				if (rand.nextDouble(1d) < Math.min(max, base + bonus)) {
					player.drop(new ItemStack(ForgeRegistries.ITEMS.getValue(result.getKey())), false, true);
					for (Map.Entry<String, Long> award : result.getValue().xpAward().entrySet()) {
						xpAwards.merge(award.getKey(), award.getValue(), (o, n) -> o + n);
					}
				}
			}
		}
		if (salvageMainHand) player.getMainHandItem().shrink(1);
		if (salvageOffHand) player.getOffhandItem().shrink(1);
		List<ServerPlayer> party = PartyUtils.getPartyMembersInRange(player);
		core.awardXP(party, xpAwards);
	}
}
