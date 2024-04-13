package harmonised.pmmo.events.impl;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.storage.Experience;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.math.BigInteger;
import java.util.HashMap;

public class PlayerDeathHandler {

	public static void handle(LivingDeathEvent event) {
		//we can skip the heavier instanceof calculation if there is no loss on death
		if (Config.server().levels().lossOnDeath() == 0.0) return;
	
		//IMPORTANT  this is confirmed by the call in EventHandler.  this assumption is critical to this cast
		Player player = (Player) event.getEntity();
		Core core = Core.get(player.level());

		new HashMap<>(core.getData().getXpMap(player.getUUID())).forEach((skill, xp) -> {
			long lossExp = 0;
			long lossLvl = 0;
			long lossScaled = Double.valueOf(Config.server().levels().lossOnDeath() * 10000d).longValue();
			if (Config.server().levels().loseOnlyExcess()) {
				lossExp = (xp.getXp() * lossScaled)/10000L;
				xp.addXp(-lossExp);
			}
			else if (Config.server().levels().loseOnDeath()) {
				BigInteger totalXp = BigInteger.valueOf(0L);
				for (long i = 0; i < xp.getLevel().getLevel(); i++) {
					totalXp = totalXp.add(BigInteger.valueOf(Experience.XpLevel.getXpForNextLevel(i)));
				}
				totalXp = totalXp.multiply(BigInteger.valueOf(lossScaled));
				totalXp = totalXp.divide(BigInteger.valueOf(10000L));
				while (totalXp.longValue() > xp.getXp()) {
					long currentXp = xp.getXp();
					long upperLimit = xp.getLevel().getXpToGain() - 1;
					xp.getLevel().decrement();
					xp.setXp(upperLimit);
					totalXp.subtract(BigInteger.valueOf(currentXp));
				}
				xp.setXp(totalXp.longValue());
			}
			core.getData().getXpMap(player.getUUID()).put(skill, xp);
		});
	}
}
