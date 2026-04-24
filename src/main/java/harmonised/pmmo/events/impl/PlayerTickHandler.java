package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.features.penalties.EffectManager;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerTickHandler {
	private static final Map<UUID, Float> healthLast = new HashMap<>();
	private static final Map<UUID, Vec3> moveLast = new HashMap<>();
	//Per-player, per-skill fractional XP carry. Small per-tick awards (e.g. 0.1 swim XP)
	//would otherwise truncate to 0 every cycle via (long)value. We accumulate the remainder
	//and flush integer portions whenever they cross a whole unit.
	private static final Map<UUID, Map<String, Double>> xpFractions = new HashMap<>();

	public static void handle(PlayerTickEvent.Post event) {
		//execute only on every 10th tick
		if ((event.getEntity().tickCount % 10) != 0) return;

		Player player = event.getEntity();
		Core core = Core.get(event.getEntity().level());

		//Recharge vein items
		if (player instanceof ServerPlayer) {
			VeinMiningLogic.regenerateVein((ServerPlayer)player);
			//Apply positive and negative effects based on biome and items worn
			EffectManager.applyEffects(core, player);
		}

		//Compute tick-scoped data once: party membership doesn't change within a tick.
		//Sample positional delta over the 10-tick window once and share via context.
		//Instantaneous player.getDeltaMovement() is unreliable for swim XP — post-drag
		//velocity jitters per-axis and the swim-sprint vector follows look direction, so
		//horizontal swims sampled 2–4 and vertical swims sampled 1000s with identical config.
		//Positional delta captures actual distance traveled on each axis.
		Vec3 pos = player.position();
		Vec3 oldPos = moveLast.getOrDefault(player.getUUID(), pos);
		double dx = pos.x() - oldPos.x();
		double dy = pos.y() - oldPos.y();
		double dz = pos.z() - oldPos.z();
		TickContext ctx = new TickContext(core, event, player instanceof ServerPlayer sp
				? PartyUtils.getPartyMembersInRange(sp) : List.of(), new HashMap<>(), dx, dy, dz);

		if (!healthLast.containsKey(player.getUUID()))
			healthLast.put(player.getUUID(), player.getHealth());

		float healthDiff = player.getHealth() - healthLast.getOrDefault(player.getUUID(), 0f);
		if (Math.abs(healthDiff) >= 0.01) {
			processEvent(healthDiff > 0 ? EventType.HEALTH_INCREASE : EventType.HEALTH_DECREASE, ctx);
		}
		if (player.isPassenger())
			processEvent(EventType.RIDING, ctx);
		if (!player.getActiveEffects().isEmpty())
			processEvent(EventType.EFFECT, ctx);

		if (player.isUnderWater()) {
			processEvent(EventType.SUBMERGED, ctx);
			//Threshold is positional distance over 10 ticks, not instantaneous velocity.
			//At 10 ticks the player has moved measurably; anything below 0.01 blocks is noise.
			double threshold = 0.01;
			//SURFACING/DIVING fire on sustained vertical motion. Positional dy smooths out
			//the single-tick sign flips that caused DIVING to keep firing while swimming up.
			if (dy > threshold)
				processEvent(EventType.SURFACING, ctx);
			else if (dy < -threshold)
				processEvent(EventType.DIVING, ctx);
			//Swim/swim-sprint fire in addition to SURFACING/DIVING — axis stacking so a
			//diagonal swim credits both vertical and horizontal motion.
			if (player.isSprinting())
				processEvent(EventType.SWIM_SPRINTING, ctx);
			else if (dx * dx + dz * dz > threshold * threshold)
				processEvent(EventType.SWIMMING, ctx);
		}
		else if (player.isInWater())
			processEvent(EventType.SWIMMING, ctx);
		else if (player.isSprinting())
			processEvent(EventType.SPRINTING, ctx);
		else if (player.isCrouching())
			processEvent(EventType.CROUCH, ctx);

		//update tracker variables. Gate to server-side: these static maps are shared
		//across sides on an integrated server, and letting the client thread write to
		//them clobbers the snapshot the server's next 10-tick cycle reads, yielding
		//magnitude=0 / healthDiff=0 on the next server tick for this player.
		if (player instanceof ServerPlayer) {
			//Batch all XP awarded this tick into a single awardXP call.
			if (!ctx.batchedAwards().isEmpty())
				core.awardXP(ctx.partyMembers(), ctx.batchedAwards());
			healthLast.put(player.getUUID(), player.getHealth());
			moveLast.put(player.getUUID(), player.position());
		}
	}

	private record TickContext(Core core, PlayerTickEvent event, List<ServerPlayer> partyMembers,
							   Map<String, Long> batchedAwards, double dx, double dy, double dz) {
		Player player() {return event.getEntity();}
	}

	private static void processEvent(EventType type, TickContext ctx) {
		Core core = ctx.core();
		Player player = ctx.player();
		boolean serverSide = core.getSide().equals(LogicalSide.SERVER);
		//Early-out: if no XP config, no listeners, and no perks are interested
		//in this event, skip the entire allocation-heavy pipeline.
		Map<String, Double> ratio = serverSide ? Config.server().xpGains().playerXp(type) : Map.of();
		if (serverSide
				&& (ratio == null || ratio.isEmpty())
				&& !core.getEventTriggerRegistry().hasListener(type)) {
			return;
		}
		CompoundTag eventHookOutput = new CompoundTag();
		if (serverSide){
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, ctx.event(), new CompoundTag());
		}
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, player, eventHookOutput));
		if (serverSide) {
			ResourceLocation source = Reference.mc("player");
			final Map<String, Long> xpAward = perkOutput.contains(APIUtils.SERIALIZED_AWARD_MAP)
					? CoreUtils.deserializeAwardMap(perkOutput.getCompound(APIUtils.SERIALIZED_AWARD_MAP))
					: new HashMap<>();
			//Hoist the modifier map out of the per-skill loop. The Core cache ensures repeat calls
			//across events in the same tick share one computation, but fetching once per event also
			//avoids the per-skill lookup cost.
			final Map<String, Double> modifiers = core.getConsolidatedModifierMap(player);
			switch (type) {
			case HEALTH_INCREASE, HEALTH_DECREASE -> {
				processHealthChange(ratio, modifiers, player, xpAward);
			}
			case RIDING -> {
				source = RegistryUtil.getId(player.getVehicle());
				xpAward.putAll(core.getExperienceAwards(type, player.getVehicle(), player, perkOutput));
			}
			case EFFECT -> {
				for (MobEffectInstance mei : player.getActiveEffects()) {
					source = mei.getEffect().unwrapKey().get().location();
					xpAward.putAll(core.getExperienceAwards(mei, player, perkOutput));
				}
			}
			case SPRINTING, SWIM_SPRINTING -> {
			double magnitude = Math.sqrt(ctx.dx() * ctx.dx() + ctx.dy() * ctx.dy() + ctx.dz() * ctx.dz());
			scaleByMagnitude(ratio, modifiers, magnitude, xpAward, player.getUUID());
			}
			case SUBMERGED -> {
				scaleByMagnitude(ratio, modifiers, 1d, xpAward, player.getUUID());
			}
			//Axis-semantic magnitudes: each event credits only the motion it represents.
			//Horizontal swim uses 2D horizontal distance; SURFACING/DIVING use signed
			//vertical distance (clamped to 0 so a downward wobble during surfacing can't
			//negate the award). SWIM_SPRINTING uses full 3D magnitude to preserve the
			//"sprint is faster so awards more" feel regardless of look direction.
			case SWIMMING -> {
				double magnitude = Math.sqrt(ctx.dx() * ctx.dx() + ctx.dz() * ctx.dz());
				scaleByMagnitude(ratio, modifiers, magnitude, xpAward, player.getUUID());
			}
			case SURFACING -> {
				scaleByMagnitude(ratio, modifiers, Math.max(0d, ctx.dy()), xpAward, player.getUUID());
			}
			case DIVING -> {
				scaleByMagnitude(ratio, modifiers, Math.max(0d, -ctx.dy()), xpAward, player.getUUID());
			}
			default -> {}
			}

			CheeseTracker.applyAntiCheese(type, source, player, xpAward);
			//Merge into the tick-scoped batch rather than calling awardXP per event.
			xpAward.forEach((skill, value) -> ctx.batchedAwards().merge(skill, value, Long::sum));
		}
	}

	private static void scaleByMagnitude(Map<String, Double> ratio, Map<String, Double> modifiers, double magnitude, Map<String, Long> xpAward, UUID uuid) {
		if (ratio == null || ratio.isEmpty()) return;
		Map<String, Double> frac = xpFractions.computeIfAbsent(uuid, k -> new HashMap<>());
		ratio.forEach((skill, r) -> {
			double value = r * magnitude * modifiers.getOrDefault(skill, 1d);
			double acc = frac.getOrDefault(skill, 0d) + value;
			long whole = (long) acc;
			frac.put(skill, acc - whole);
			if (whole != 0L)
				xpAward.merge(skill, whole, Long::sum);
		});
	}

	private static void processHealthChange(Map<String, Double> ratio, Map<String, Double> modifiers, Player player, Map<String, Long> xpAward) {
		float diff = Math.abs(healthLast.getOrDefault(player.getUUID(), 0f) - player.getHealth());
		scaleByMagnitude(ratio, modifiers, diff, xpAward, player.getUUID());
	}
}
