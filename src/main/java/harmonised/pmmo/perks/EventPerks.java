package harmonised.pmmo.perks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

public class EventPerks {
	private static final String PER_LEVEL = "power";
	private static final String MAX_BOOST = "max";
	private static final String COOLDOWN = "cooldown";
	private static final String STRENGTH = "power";
	private static Map<UUID, Long> breathe_cooldown = new HashMap<>();
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> JUMP = (player, nbt, level) -> {
		double perLevel = nbt.getDouble(PER_LEVEL);
		double maxBoost = nbt.getDouble(MAX_BOOST);
        double jumpBoost;
        jumpBoost = -0.013 + level * (0.14 / perLevel);
        jumpBoost = Math.min(maxBoost, jumpBoost);
        player.push(0, jumpBoost, 0);
        return new CompoundTag();
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> BREATH = (player, nbt, level) -> {
		long cooldown = nbt.getLong(COOLDOWN);
		int perLevel = Math.max(1, level * nbt.getInt(STRENGTH));
		long currentCD = breathe_cooldown.getOrDefault(player.getUUID(), System.currentTimeMillis());
		int currentAir = player.getAirSupply();
		if (currentAir < 2 && (currentCD < System.currentTimeMillis() - cooldown 
				|| currentCD + 20 >= System.currentTimeMillis())) {
			player.setAirSupply(currentAir + perLevel);
			player.sendMessage(new TranslatableComponent("pmmo.perks.breathrefresh"), ChatType.GAME_INFO, player.getUUID());
			breathe_cooldown.put(player.getUUID(), System.currentTimeMillis());
		}
		return new CompoundTag();
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> FALL_SAVE = (player, nbt, level) -> {
		CompoundTag output = new CompoundTag();
		
		return output;
	};
}
