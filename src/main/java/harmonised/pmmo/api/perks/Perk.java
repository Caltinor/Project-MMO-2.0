package harmonised.pmmo.api.perks;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

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
 * @param description a translatable description of what the perk does for use
 * in infomational displays.
 * @param status a function which provides player-specific information about this
 * perk as it pertains to the player.  For example, if the perk provides a per-level
 * bonus, this function would return the value at the player's level. 
 */
public record Perk(
		BiPredicate<Player, CompoundTag> conditions,
		CompoundTag propertyDefaults,
		BiFunction<Player, CompoundTag, CompoundTag> start,
		TriFunction<Player, CompoundTag, Integer, CompoundTag> tick,
		BiFunction<Player, CompoundTag, CompoundTag> stop,
		MutableComponent description,
		BiFunction<Player, CompoundTag, List<MutableComponent>> status) {
	
	public static class Builder {
		BiPredicate<Player, CompoundTag> conditions = (p,n) -> true;
		CompoundTag propertyDefaults = new CompoundTag();
		BiFunction<Player, CompoundTag, CompoundTag> start = (p,c) -> new CompoundTag();
		TriFunction<Player, CompoundTag, Integer, CompoundTag> tick = (p,c,i) -> new CompoundTag();
		BiFunction<Player, CompoundTag, CompoundTag> stop = (p,c) -> new CompoundTag();
		MutableComponent description = Component.empty();
		BiFunction<Player, CompoundTag, List<MutableComponent>> status = (p,s) -> List.of();
		
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
		public Builder setDescription(MutableComponent description) {
			this.description = description;
			return this;
		}
		public Builder setStatus(BiFunction<Player, CompoundTag, List<MutableComponent>> status) {
			this.status = status;
			return this;
		}
		public Perk build() {
			return new Perk(conditions, propertyDefaults, start, tick, stop, description, status);
		}
	}
	
	public static Builder begin() {return new Builder();}
	public static Perk empty() {return new Builder().build();}
	
	private boolean canActivate(Player player, CompoundTag settings) {
		return VALID_CONTEXT.test(player, settings) && conditions().test(player, settings);
	}
	
	public CompoundTag start(Player player, CompoundTag nbt) {
		return canActivate(player, nbt) ? start.apply(player, nbt) : new CompoundTag();
	}
	
	public CompoundTag tick(Player player, CompoundTag nbt, int elapsedTicks) {
		return canActivate(player, nbt) ? tick.apply(player, nbt, elapsedTicks) : new CompoundTag();
	}
	
	public CompoundTag stop(Player player, CompoundTag nbt) {
		return canActivate(player, nbt) ? stop.apply(player, nbt) : new CompoundTag();
	}
	
	public static final BiPredicate<Player, CompoundTag> VALID_CONTEXT = (player, src) -> {
		if (src.contains(APIUtils.COOLDOWN) && !Core.get(player.level()).getPerkRegistry().isPerkCooledDown(player, src))
			return false;
		if (src.contains(APIUtils.CHANCE) && src.getDouble(APIUtils.CHANCE) < player.level().random.nextDouble())
			return false;
		if (src.contains(APIUtils.SKILLNAME)) {
			if (src.contains(FireworkHandler.FIREWORK_SKILL) && !src.getString(APIUtils.SKILLNAME).equals(src.getString(FireworkHandler.FIREWORK_SKILL)))
				return false;
			int skillLevel = src.getInt(APIUtils.SKILL_LEVEL);
			if (src.contains(APIUtils.MAX_LEVEL) && skillLevel > src.getInt(APIUtils.MAX_LEVEL))
				return false;
			if (src.contains(APIUtils.MIN_LEVEL) && skillLevel < src.getInt(APIUtils.MIN_LEVEL))
				return false;
			boolean modulus = src.contains(APIUtils.MODULUS), 
					milestone = src.contains(APIUtils.MILESTONES);
			if (modulus || milestone) {
				boolean modulus_match = modulus,
						milestone_match = milestone;
				if (modulus && skillLevel % Math.max(1, src.getInt(APIUtils.MODULUS)) != 0)
					modulus_match = false;
				if (milestone && !src.getList(APIUtils.MILESTONES, Tag.TAG_DOUBLE).stream()
						.map(tag -> ((DoubleTag)tag).getAsInt()).toList().contains(skillLevel))
					milestone_match = false;
				if (!modulus_match && !milestone_match)
					return false;
			}
		}
		return true;
	};
}
