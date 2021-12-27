package harmonised.pmmo.perks;

import org.apache.logging.log4j.util.TriConsumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class EventPerks {
	private static final String PER_LEVEL = "power";
	private static final String MAX_BOOST = "max";
	
	public static TriConsumer<ServerPlayer, CompoundTag, Integer> JUMP = (player, nbt, level) -> {
		double perLevel = nbt.getDouble(PER_LEVEL);
		double maxBoost = nbt.getDouble(MAX_BOOST);
        double jumpBoost;
        jumpBoost = -0.013 + level * (0.14 / perLevel);
        jumpBoost = Math.min(maxBoost, jumpBoost);
        player.push(0, jumpBoost, 0);
	};
}
