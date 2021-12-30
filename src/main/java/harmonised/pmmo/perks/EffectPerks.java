package harmonised.pmmo.perks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class EffectPerks {
	private static final String COOLDOWN = "cooldown";
	private static final String DURATION = "time";
	private static final String STRENGTH = "power";
	private static final String MIN_LEVEL = "min_level";
	private static final CompoundTag EMPTY = new CompoundTag();
	private static Map<UUID, Long> regen_cooldown = new HashMap<>();

	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> NIGHT_VISION = (player, nbt, level) -> {
		int min = nbt.contains(MIN_LEVEL) ? nbt.getInt(MIN_LEVEL) : 1;
		if (level < min) return EMPTY;
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 30000));
		return EMPTY;
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> NIGHT_VISION_TERM = (player, nbt, level) -> {
		MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
		if (effect == null) return EMPTY;
		if (effect.getDuration() > 480) {
			player.removeEffect(MobEffects.NIGHT_VISION);
		}
		return EMPTY;
	};
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> REGEN = (player, nbt, level) -> {
		long cooldown = nbt.contains(COOLDOWN) ? nbt.getLong(COOLDOWN) : 300l;
		int duration = nbt.contains(DURATION) ? nbt.getInt(DURATION) : 1;
		double strength = nbt.contains(STRENGTH) ? nbt.getDouble(STRENGTH) : 0.02;
		int perLevel = Math.max(0, (int)((double)level * strength));
		long currentCD = regen_cooldown.getOrDefault(player.getUUID(), System.currentTimeMillis());
		if (currentCD < System.currentTimeMillis() - cooldown 
				|| currentCD + 20 >= System.currentTimeMillis()) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, perLevel));
			regen_cooldown.put(player.getUUID(), System.currentTimeMillis());
		}
		return EMPTY;
	};
}
