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

	public static void clearPlayer(UUID uuid) {
		healthLast.remove(uuid);
		moveLast.remove(uuid);
		xpFractions.remove(uuid);
	}

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

		//Compute tick-scoped data once: party membership doesn't change within a tick, and
		//Core.getConsolidatedModifierMap is cached per (uuid, tickCount) so repeat calls are O(1).
		TickContext ctx = new TickContext(core, event, player instanceof ServerPlayer sp
				? PartyUtils.getPartyMembersInRange(sp) : List.of(), new HashMap<>());

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
			Vec3 vec = player.getDeltaMovement();
			if (player.isSprinting())
				processEvent(EventType.SWIM_SPRINTING, ctx);
			//The value of -0.005 is a constant "sinking" rate that entities have even when
			//standing on blocks underwater. This variable captures the threshold above that
			//so that the Submerged event can actually fire.
			double sinkingRate = 0.01;
			if (vec.y() > sinkingRate)
				processEvent(EventType.SURFACING, ctx);
			else if (vec.y() < -sinkingRate)
				processEvent(EventType.DIVING, ctx);
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

	private record TickContext(Core core, PlayerTickEvent event, List<ServerPlayer> partyMembers, Map<String, Long> batchedAwards) {
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
				&& !core.getEventTriggerRegistry().hasListener(type)
				&& !core.getPerkRegistry().hasPerk(type)) {
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
			case SPRINTING -> {
				Vec3 vec = player.position();
				Vec3 old = moveLast.getOrDefault(player.getUUID(), vec);
				double dx = vec.x() - old.x(), dy = vec.y() - old.y(), dz = vec.z() - old.z();
				double magnitude = Math.sqrt(dx * dx + dy * dy + dz * dz);
				scaleByMagnitude(ratio, modifiers, magnitude, xpAward, player.getUUID());
			}
			case SUBMERGED -> {
				scaleByMagnitude(ratio, modifiers, 1d, xpAward, player.getUUID());
			}
			case SWIMMING, DIVING, SURFACING, SWIM_SPRINTING -> {
				Vec3 vec = player.getDeltaMovement();
				double magnitude = Math.sqrt(vec.x() * vec.x() + vec.y() * vec.y() + vec.z() * vec.z());
				scaleByMagnitude(ratio, modifiers, magnitude, xpAward, player.getUUID());
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
