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
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.LogicalSide;

public class PlayerTickHandler {
	private static final Map<UUID, Integer> airLast = new HashMap<>();
	private static final Map<UUID, Float> healthLast = new HashMap<>();
	private static short ticksIgnoredSinceLastProcess = 0;

	public static void handle(PlayerTickEvent event) {
		ticksIgnoredSinceLastProcess++;
		if (ticksIgnoredSinceLastProcess < 10) return;
		
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
		if (player.getHealth() != healthLast.getOrDefault(player.getUUID(), 0f))
			processEvent(EventType.HEALTH_CHANGE, core, event);
		if (player.isPassenger())
			processEvent(EventType.RIDING, core, event);
		if (!player.getActiveEffects().isEmpty())
			processEvent(EventType.EFFECT, core, event);
		
		if (player.isUnderWater()) {
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
			else
				processEvent(EventType.SUBMERGED, core, event);
		}
		else if (player.isInWater())
			processEvent(EventType.SWIMMING, core, event); 
		else if (player.isSprinting())
			processEvent(EventType.SPRINTING, core, event);
		
		//update tracker variables
		airLast.put(player.getUUID(), player.getAirSupply());
		healthLast.put(player.getUUID(), player.getHealth());
		ticksIgnoredSinceLastProcess = 0;
	}
	
	private static void processEvent(EventType type, Core core, PlayerTickEvent event) {
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = core.getSide().equals(LogicalSide.SERVER); 
		if (serverSide){			
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
		}
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, event.player, eventHookOutput, core.getSide()));
		if (serverSide) {
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
				float diff = Math.abs(healthLast.getOrDefault(event.player.getUUID(), 0f) - event.player.getHealth());
				Map<String, Double> ratio = Config.HEALTH_CHANGE_XP.get();
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * diff * core.getConsolidatedModifierMap(event.player).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});
			}
			case RIDING -> {
				core.getExperienceAwards(type, event.player.getVehicle(), event.player, perkOutput).forEach((skill, value) -> {
					xpAward.put(skill, value);
				});;
			}
			case EFFECT -> {
				for (MobEffectInstance mei : event.player.getActiveEffects()) {	
					core.getExperienceAwards(mei, event.player, perkOutput).forEach((skill, value) -> {
						xpAward.put(skill, value);
					});
				}
			}
			case SPRINTING -> {
				Vec3 vec = event.player.getDeltaMovement();
				double magnitude = Math.sqrt(Math.pow(vec.x(), 2)+Math.pow(vec.y(), 2)+Math.pow(vec.z(), 2));
				Map<String, Double> ratio = Config.SPRINTING_XP.get();
				ratio.keySet().forEach((skill) -> {
					Double value = ratio.getOrDefault(skill, 0d) * magnitude * core.getConsolidatedModifierMap(event.player).getOrDefault(skill, 1d);
					xpAward.put(skill, value.longValue());
				});;
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
			
			CheeseTracker.applyAntiCheese(type, null, event.player, xpAward);
			MsLoggy.INFO.log(LOG_CODE.XP, "XpGains (afterCheese): "+MsLoggy.mapToString(xpAward));
			
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.player);
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
