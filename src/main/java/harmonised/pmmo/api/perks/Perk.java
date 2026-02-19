package harmonised.pmmo.api.perks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**A custom behavior executed by a player. The {@link CompoundTag} supplied 
 * to all functions contains the configuration and event context values.
 * 
 * @param conditions determines if a perk should execute based on configuration.
 * @param propertyDefaults default settings in case players omit or misconfigure
 * required values.
 * @param start a function to execute when this perk is first activated.
 * @param tick a function to execute each subsequent tick for the "duration"
 * specified in the config.  if "duration" is omitted, this function will be
 * skipped.
 * @param stop a function which executes after ticks have concluded.  if no ticks
 * were executed, this will execute the tick after the start function.
 */
public record Perk(
		BiPredicate<Player, CompoundTag> conditions,
		CompoundTag propertyDefaults,
		BiFunction<Player, CompoundTag, CompoundTag> start,
		TriFunction<Player, CompoundTag, Integer, CompoundTag> tick,
		BiFunction<Player, CompoundTag, CompoundTag> stop) {
	
	public static class Builder {
		BiPredicate<Player, CompoundTag> conditions = (p,n) -> true;
		CompoundTag propertyDefaults = new CompoundTag();
		BiFunction<Player, CompoundTag, CompoundTag> start = (p,c) -> new CompoundTag();
		TriFunction<Player, CompoundTag, Integer, CompoundTag> tick = (p,c,i) -> new CompoundTag();
		BiFunction<Player, CompoundTag, CompoundTag> stop = (p,c) -> new CompoundTag();
		
		protected Builder() {}
		public Builder addConditions(BiPredicate<Player, CompoundTag> conditions) {
			this.conditions = conditions;
			return this;
		}
		public Builder addDefaults(CompoundTag defaults) {
			this.propertyDefaults = defaults;
			return this;
		}
		public Builder setStart(BiFunction<Player, CompoundTag, CompoundTag> start) {
			this.start = start;
			return this;
		}
		public Builder setTick(TriFunction<Player, CompoundTag, Integer, CompoundTag> tick) {
			this.tick = tick;
			return this;
		}
		public Builder setStop(BiFunction<Player, CompoundTag, CompoundTag> stop) {
			this.stop = stop;
			return this;
		}
		public Perk build() {
			return new Perk(conditions, propertyDefaults, start, tick, stop);
		}
	}
	
	public static Builder begin() {return new Builder();}
	public static Perk empty() {return new Builder().build();}
	
	public boolean canActivate(Player player, CompoundTag settings) {
		return VALID_CONTEXT.test(player, settings) && conditions().test(player, settings);
	}
	
	public CompoundTag start(Player player, CompoundTag nbt) {
		return start.apply(player, nbt);
	}
	
	public CompoundTag tick(Player player, CompoundTag nbt, int elapsedTicks) {
		return tick.apply(player, nbt, elapsedTicks);
	}
	
	public CompoundTag stop(Player player, CompoundTag nbt) {
		return stop.apply(player, nbt);
	}
	
	public static final BiPredicate<Player, CompoundTag> VALID_CONTEXT = (player, src) -> {
		if (src.contains(APIUtils.COOLDOWN) && !Core.get(player.level()).getPerkRegistry().isPerkCooledDown(player, src))
			return false;
		boolean chanceSucceed = false;
		if (src.contains(APIUtils.CHANCE) && src.getDoubleOr(APIUtils.CHANCE, 0d) < player.level().random.nextDouble())
			return false;
		else if (src.contains(APIUtils.CHANCE)) chanceSucceed = true;
		if (src.contains(APIUtils.SKILLNAME)) {
			if (src.contains(FireworkHandler.FIREWORK_SKILL) && !src.getString(APIUtils.SKILLNAME).equals(src.getString(FireworkHandler.FIREWORK_SKILL)))
				return false;
			int skillLevel = src.getIntOr(APIUtils.SKILL_LEVEL, 0);
			if (src.contains(APIUtils.MAX_LEVEL) && skillLevel > src.getLongOr(APIUtils.MAX_LEVEL, Config.server().levels().maxLevel()))
				return false;
			if (src.contains(APIUtils.MIN_LEVEL) && skillLevel < src.getIntOr(APIUtils.MIN_LEVEL, 0))
				return false;
			boolean modulus = src.contains(APIUtils.MODULUS), 
					milestone = src.contains(APIUtils.MILESTONES);
			if (modulus || milestone) {
				boolean modulus_match = modulus,
						milestone_match = milestone;
				if (modulus && skillLevel % Math.max(1, src.getIntOr(APIUtils.MODULUS, 0)) != 0)
					modulus_match = false;
				if (milestone && !src.getListOrEmpty(APIUtils.MILESTONES).stream()
						.map(t -> t.asInt().orElse(0)).toList().contains(skillLevel))
					milestone_match = false;
				if (!modulus_match && !milestone_match)
					return false;
			}
		}
		if (chanceSucceed && src.contains(APIUtils.CHANCE_SUCCESS_MSG)) {
			String msg = src.getString(APIUtils.CHANCE_SUCCESS_MSG).get();
			player.displayClientMessage(Component.literal(msg), false);
		}
		return true;
	};
}
