package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.features.penalties.EffectManager;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.TagUtils;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.LogicalSide;

public class PlayerTickHandler {
	private static final Map<UUID, Integer> airLast = new HashMap<>();
	private static final Map<UUID, Float> healthLast = new HashMap<>();
	private static final Map<UUID, Vec3> moveLast = new HashMap<>();

	public static void handle(PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END && event.player.tickCount % 10 != 0) return;

		Player player = event.player;
		Core core = Core.get(event.side);

		//Recharge vein items
		if (player instanceof ServerPlayer) {
			VeinMiningLogic.regenerateVein((ServerPlayer)player);
			//Apply positive and negative effects based on biome and items worn
			EffectManager.applyEffects(core, player);
		}
		
		if (!airLast.containsKey(player.getUUID()))
			airLast.put(player.getUUID(), player.getAirSupply());
		if (!healthLast.containsKey(player.getUUID()))
			healthLast.put(player.getUUID(), player.getHealth());
		
		if (player.getAirSupply() != airLast.getOrDefault(player.getUUID(), 0))
			processEvent(EventType.BREATH_CHANGE, core, event);
		float healthDiff = player.getHealth() - healthLast.getOrDefault(player.getUUID(), 0f);
		if (Math.abs(healthDiff) >= 0.01) {
			processEvent(EventType.HEALTH_CHANGE, core, event);
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
		airLast.put(player.getUUID(), player.getAirSupply());
		healthLast.put(player.getUUID(), player.getHealth());
		moveLast.put(player.getUUID(), player.position());
	}
	
	private static void processEvent(EventType type, Core core, PlayerTickEvent event) {
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = core.getSide().equals(LogicalSide.SERVER); 
		if (serverSide){			
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
		}
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, event.player, eventHookOutput));
		if (serverSide) {
			ResourceLocation source = ResourceLocation.withDefaultNamespace("player");
			final Map<String, Long> xpAward = perkOutput.contains(APIUtils.SERIALIZED_AWARD_MAP) 
					? CoreUtils.deserializeAwardMap(perkOutput.getCompound(APIUtils.SERIALIZED_AWARD_MAP))
					: new HashMap<>();
			switch (type) {
			case BREATH_CHANGE -> {
				int diff = Math.abs(airLast.getOrDefault(event.player.getUUID(), 0) - event.player.getAirSupply());
				Map<String, Double> ratio = Config.BREATH_CHANGE_XP.get();
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * diff * core.getConsolidatedModifierMap(event.player).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
			case HEALTH_CHANGE -> {
				processHealthChange(Config.HEALTH_CHANGE_XP.get(), core, event.player, xpAward);
			}
			case HEALTH_INCREASE -> {
				processHealthChange(Config.HEALTH_INCREASE_XP.get(), core, event.player, xpAward);
			}
			case HEALTH_DECREASE -> {
				processHealthChange(Config.HEALTH_DECREASE_XP.get(), core, event.player, xpAward);
			}
			case RIDING -> {
				source = RegistryUtil.getId(event.player.getVehicle());
                xpAward.putAll(core.getExperienceAwards(type, event.player.getVehicle(), event.player, perkOutput));
                ;
			}
			case EFFECT -> {
				for (MobEffectInstance mei : event.player.getActiveEffects()) {	
					source = RegistryUtil.getId(mei.getEffect());
                    xpAward.putAll(core.getExperienceAwards(mei, event.player, perkOutput));
				}
			}
			case SPRINTING -> {
				Vec3 vec = event.player.position();
				Vec3 old = moveLast.getOrDefault(event.player.getUUID(), vec);
				double magnitude = Math.sqrt(
						Math.pow(Math.abs(vec.x()-old.x()), 2) +
						Math.pow(Math.abs(vec.y()-old.y()), 2) +
						Math.pow(Math.abs(vec.z()-old.z()), 2));
				Map<String, Double> ratio = Config.SPRINTING_XP.get();
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * magnitude * core.getConsolidatedModifierMap(event.player).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
			case SUBMERGED -> {
				Map<String, Double> ratio = Config.SUBMERGED_XP.get();
				ratio.keySet().forEach((skill) -> {
					xpAward.put(skill, ratio.getOrDefault(skill, 0d).longValue());
				});
			}
			case SWIMMING -> {
				Vec3 vec = event.player.getDeltaMovement();
				double magnitude = Math.sqrt(Math.pow(vec.x(), 2)+Math.pow(vec.y(), 2)+Math.pow(vec.z(), 2));
				Map<String, Double> ratio = Config.SWIMMING_XP.get();
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * magnitude * core.getConsolidatedModifierMap(event.player).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
			case DIVING -> {
				Vec3 vec = event.player.getDeltaMovement();
				double magnitude = Math.sqrt(Math.pow(vec.x(), 2)+Math.pow(vec.y(), 2)+Math.pow(vec.z(), 2));
				Map<String, Double> ratio = Config.DIVING_XP.get();
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * magnitude * core.getConsolidatedModifierMap(event.player).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
			case SURFACING -> {
				Vec3 vec = event.player.getDeltaMovement();
				double magnitude = Math.sqrt(Math.pow(vec.x(), 2)+Math.pow(vec.y(), 2)+Math.pow(vec.z(), 2));
				Map<String, Double> ratio = Config.SURFACING_XP.get();
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * magnitude * core.getConsolidatedModifierMap(event.player).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
			case SWIM_SPRINTING -> {
				Vec3 vec = event.player.getDeltaMovement();
				double magnitude = Math.sqrt(Math.pow(vec.x(), 2)+Math.pow(vec.y(), 2)+Math.pow(vec.z(), 2));
				Map<String, Double> ratio = Config.SWIM_SPRINTING_XP.get();
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * magnitude * core.getConsolidatedModifierMap(event.player).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
			default -> {}
			}
			
			CheeseTracker.applyAntiCheese(type, source, event.player, xpAward);
			
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.player);
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
