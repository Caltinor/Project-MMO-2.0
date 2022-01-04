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
	private static final String PER_LEVEL = "per_level";
	private static final String MAX_BOOST = "max_boost";
	private static final String COOLDOWN = "cooldown";
	private static Map<UUID, Long> breathe_cooldown = new HashMap<>();
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> JUMP = (player, nbt, level) -> {
		double perLevel = nbt.contains(PER_LEVEL) ? nbt.getDouble(PER_LEVEL) : 0.0023;
		double maxBoost = nbt.contains(MAX_BOOST) ? nbt.getDouble(MAX_BOOST) : 0.33;
        double jumpBoost;
        jumpBoost = -0.011 + level * perLevel;
        jumpBoost = Math.min(maxBoost, jumpBoost);
        player.push(0, jumpBoost, 0);
        player.hurtMarked = true; 
        CompoundTag output = new CompoundTag();
        output.putDouble("power", jumpBoost);
        return output;
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> BREATH = (player, nbt, level) -> {
		long cooldown = nbt.contains(COOLDOWN) ? nbt.getLong(COOLDOWN) : 300l;
		double strength = nbt.contains(PER_LEVEL) ? nbt.getDouble(PER_LEVEL) : 1d;
		int perLevel = Math.max(1, (int)((double)level * strength));
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
		double perLevel = nbt.contains(PER_LEVEL) ? nbt.getDouble(PER_LEVEL) : 0.1;
		int saved = (int)(perLevel * (double)level);
		output.putInt("saved", saved);
		return output;
	};

	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DAMAGE_BOOST = (player, nbt, level) -> {
		CompoundTag output = new CompoundTag();
		if (!nbt.contains("damageIn")) return output;
		double perLevel = nbt.contains(PER_LEVEL) ? nbt.getDouble(PER_LEVEL) : 0.05;
		float damage = nbt.getFloat("damageIn") * (float)(perLevel * (double)level);
		output.putFloat("damage", damage);
		return output;
	};
}
