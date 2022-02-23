package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class PlayerDeathHandler {

	public static void handle(LivingDeathEvent event) {
		//we can skip the heavier instanceof calculation if there is no loss on death
		double lossRatio = 1 - Config.LOSS_ON_DEATH.get();
		if (lossRatio <= 0) return;
	
		//IMPORTANT  this is confirmed by the call in EventHandler.  this assumption is critical to this call
		Player player = (Player) event.getEntityLiving();
		Core core = Core.get(player.level);
		
		Map<String, Long> currentXp = new HashMap<>(core.getData().getXpMap(player.getUUID()));
		for (Map.Entry<String, Long> skill :currentXp.entrySet()) {
			long losableXp = skill.getValue();
			long finalXp = skill.getValue();
			if (!Config.LOSE_LEVELS_ON_DEATH.get()) {
				int currentLevel = core.getData().getLevelFromXP(losableXp);
				long levelXpThreshold = core.getData().getBaseXpForLevel(currentLevel);
				if (Config.LOSE_ONLY_EXCESS.get()) {
					finalXp -= Double.valueOf((losableXp - levelXpThreshold) * lossRatio).longValue();
				}
				else {
					losableXp = Math.min(
							Double.valueOf(losableXp - levelXpThreshold).longValue(), 
							Double.valueOf(losableXp * (1 -lossRatio)).longValue());
					finalXp -= losableXp;
				}					
			}
			else 
				finalXp = Double.valueOf(losableXp * lossRatio).longValue();	
			core.getData().setXpRaw(player.getUUID(), skill.getKey(), finalXp);
		}
	}
}
