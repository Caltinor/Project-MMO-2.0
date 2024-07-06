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

		if (!healthLast.containsKey(player.getUUID()))
			healthLast.put(player.getUUID(), player.getHealth());

		float healthDiff = player.getHealth() - healthLast.getOrDefault(player.getUUID(), 0f);
		if (Math.abs(healthDiff) >= 0.01) {
			processEvent(healthDiff > 0 ? EventType.HEALTH_INCREASE : EventType.HEALTH_DECREASE, core, event);
		}
		if (player.isPassenger())
			processEvent(EventType.RIDING, core, event);
		if (!player.getActiveEffects().isEmpty())
			processEvent(EventType.EFFECT, core, event);
		
		if (player.isUnderWater()) {
			processEvent(EventType.SUBMERGED, core, event);
			Vec3 vec = player.getDeltaMovement();
			if (player.isSprinting())
				processEvent(EventType.SWIM_SPRINTING, core, event); 
			//The value of -0.005 is a constant "sinking" rate that entities have even when
			//standing on blocks underwater. This variable captures the threshold above that
			//so that the Submerged event can actually fire.
			double sinkingRate = 0.01;
			if (vec.y() > sinkingRate)
				processEvent(EventType.SURFACING, core, event);
			else if (vec.y() < -sinkingRate)
				processEvent(EventType.DIVING, core, event);
		}
		else if (player.isInWater())
			processEvent(EventType.SWIMMING, core, event); 
		else if (player.isSprinting())
			processEvent(EventType.SPRINTING, core, event);
		else if (player.isCrouching())
			processEvent(EventType.CROUCH, core, event);
		
		//update tracker variables
		healthLast.put(player.getUUID(), player.getHealth());
		moveLast.put(player.getUUID(), player.position());
	}
	
	private static void processEvent(EventType type, Core core, PlayerTickEvent event) {
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = core.getSide().equals(LogicalSide.SERVER);
		if (serverSide){			
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
		}
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, event.getEntity(), eventHookOutput));
		if (serverSide) {
			Map<String, Double> ratio = Config.server().xpGains().playerXp(type);
			ResourceLocation source = Reference.mc("player");
			final Map<String, Long> xpAward = perkOutput.contains(APIUtils.SERIALIZED_AWARD_MAP) 
					? CoreUtils.deserializeAwardMap(perkOutput.getCompound(APIUtils.SERIALIZED_AWARD_MAP))
					: new HashMap<>();
			switch (type) {
			case HEALTH_INCREASE, HEALTH_DECREASE -> {
				processHealthChange(ratio, core, event.getEntity(), xpAward);
			}
			case RIDING -> {
				source = RegistryUtil.getId(event.getEntity().getVehicle());
				xpAward.putAll(core.getExperienceAwards(type, event.getEntity().getVehicle(), event.getEntity(), perkOutput));
			}
			case EFFECT -> {
				for (MobEffectInstance mei : event.getEntity().getActiveEffects()) {	
					source = mei.getEffect().unwrapKey().get().location();
					xpAward.putAll(core.getExperienceAwards(mei, event.getEntity(), perkOutput));
				}
			}
			case SPRINTING -> {
				Vec3 vec = event.getEntity().position();
				Vec3 old = moveLast.getOrDefault(event.getEntity().getUUID(), vec);
				double magnitude = Math.sqrt(
						Math.pow(Math.abs(vec.x()-old.x()), 2) +
						Math.pow(Math.abs(vec.y()-old.y()), 2) +
						Math.pow(Math.abs(vec.z()-old.z()), 2));
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * magnitude * core.getConsolidatedModifierMap(event.getEntity()).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
			case SUBMERGED -> {
				ratio.keySet().forEach((skill) -> {
					xpAward.put(skill, ratio.getOrDefault(skill, 0d).longValue());
				});
			}
			case SWIMMING, DIVING, SURFACING, SWIM_SPRINTING -> {
				Vec3 vec = event.getEntity().getDeltaMovement();
				double magnitude = Math.sqrt(Math.pow(vec.x(), 2)+Math.pow(vec.y(), 2)+Math.pow(vec.z(), 2));
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * magnitude * core.getConsolidatedModifierMap(event.getEntity()).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
                default -> {}
			}
			
			CheeseTracker.applyAntiCheese(type, source, event.getEntity(), xpAward);
			
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}

	private static void processHealthChange(Map<String, Double> ratio, Core core, Player player, Map<String, Long> xpAward) {
		float diff = Math.abs(healthLast.getOrDefault(player.getUUID(), 0f) - player.getHealth());
		ratio.keySet().forEach((skill) -> {
			Double value = ratio.getOrDefault(skill, 0d) * diff * core.getConsolidatedModifierMap(player).getOrDefault(skill, 1d);
			xpAward.put(skill, value.longValue());
		});
	}
}
