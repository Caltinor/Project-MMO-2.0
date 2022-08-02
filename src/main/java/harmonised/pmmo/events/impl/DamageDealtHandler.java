package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class DamageDealtHandler {

	public static void handle(LivingAttackEvent event) {
		//Check the source entity isn't null.  This should also reduce
		//the number of events processed.
		if (event.getSource().getEntity() == null) return;
		
		//Execute actual logic only if the source is a player
		if (event.getSource().getEntity() instanceof Player) {
			LivingEntity target = event.getEntity();
			//Confirm our target is a living entity
			if (target == null) return;
			
			Player player = (Player) event.getSource().getEntity();
			if (target.equals(player))
				return;
			Core core = Core.get(player.level);
			EventType type = getEventCategory(event.getSource().isProjectile(), event.getEntity());
			MsLoggy.INFO.log(LOG_CODE.EVENT,"Attack Type: "+type.name()+" | TargetType: "+target.getType().toString());
			
			
			//===========================DEFAULT LOGIC===================================
			if (!core.isActionPermitted(ReqType.WEAPON, player.getMainHandItem(), player)) {
				event.setCanceled(true);
				Messenger.sendDenialMsg(ReqType.WEAPON, player, player.getMainHandItem().getDisplayName());
				return;
			}
			if (!core.isActionPermitted(ReqType.KILL, target, player)) {
				event.setCanceled(true);
				Messenger.sendDenialMsg(ReqType.KILL, player, target.getDisplayName());
				return;
			}
			boolean serverSide = !player.level.isClientSide;
			CompoundTag eventHookOutput = new CompoundTag();
			if (serverSide) {
				eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
				if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) { 
					event.setCanceled(true);
					return;
				}
			}
			
		}
	}
	
	public static void handle(LivingDamageEvent event) {
		if (event.getSource().getEntity() == null) return;
		//Execute actual logic only if the source is a player
		if (event.getSource().getEntity() instanceof Player) {
			LivingEntity target = event.getEntity();
			if (target == null) return;
			
			Player player = (Player) event.getSource().getEntity();
			if (target.equals(player)) return;
			
			Core core = Core.get(player.level);
			EventType type = getEventCategory(event.getSource().isProjectile(), event.getEntity());
			//Process perks
			CompoundTag perkOutput = core.getPerkRegistry().executePerk(type, player, TagBuilder.start().withFloat(APIUtils.DAMAGE_IN, event.getAmount()).build(), core.getSide());
			MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Pre-Perk Damage:"+event.getAmount());
			if (perkOutput.contains(APIUtils.DAMAGE_OUT)) {
				event.setAmount(perkOutput.getFloat(APIUtils.DAMAGE_OUT));
			}
			MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Attack Type: "+type.name()+" | Damage Out: "+event.getAmount());
			if (!player.level.isClientSide) { 
				Map<String, Long> xpAward = getExperienceAwards(core, type, target, event.getAmount(), event.getSource(), player, perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
	
	private static Map<String, Long> getExperienceAwards(Core core, EventType type, LivingEntity target, float damage, DamageSource source, Player player, CompoundTag dataIn) {
		Map<String, Long> mapOut = new HashMap<>();
		float ultimateDamage = Math.min(damage, target.getHealth());
		ItemStack weapon = player.getMainHandItem();
		Entity srcEntity = source.isProjectile() ? source.getDirectEntity() : player;
		switch (type) {
		case MELEE_TO_MOBS: case MELEE_TO_ANIMALS: case MELEE_TO_PLAYERS: case RANGED_TO_MOBS: case RANGED_TO_ANIMALS: case RANGED_TO_PLAYERS: {
			Functions.mergeMaps(core.getExperienceAwards(type, weapon, player, dataIn),
								core.getExperienceAwards(type, srcEntity, player, dataIn),
								core.getExperienceAwards(type, target, player, dataIn))
			.forEach((skill, value) -> {
				mapOut.put(skill, (long)((float)value * ultimateDamage));
			});
			break;
		}
		case DEAL_MELEE_DAMAGE: {
			Config.DEAL_MELEE_DAMAGE_XP.get().keySet().forEach((skill) -> {
				Double value = ultimateDamage * Config.DEAL_MELEE_DAMAGE_XP.get().getOrDefault(skill, 0d) * core.getConsolidatedModifierMap(player).getOrDefault(skill, 1d);
				mapOut.put(skill, value.longValue());
			});
			break;
		}
		case DEAL_RANGED_DAMAGE: {
			Config.DEAL_RANGED_DAMAGE_XP.get().keySet().forEach((skill) -> {
				Double value = ultimateDamage * Config.DEAL_RANGED_DAMAGE_XP.get().getOrDefault(skill, 0d) * core.getConsolidatedModifierMap(player).getOrDefault(skill, 1d);
				mapOut.put(skill, value.longValue());
			});
			break;
		}
		default: {return new HashMap<>();}
		}
		return mapOut;
	}
	
	private static EventType getEventCategory(boolean projectileSource, LivingEntity target) {
		if (projectileSource) {
			if (target.getType().is(Reference.MOB_TAG))
				return EventType.RANGED_TO_MOBS;
			if (target.getType().is(Reference.ANIMAL_TAG))
				return EventType.RANGED_TO_ANIMALS;
			if (target.getType().equals(EntityType.PLAYER))
				return EventType.RANGED_TO_PLAYERS;
			else
				return EventType.DEAL_RANGED_DAMAGE;
		}
		else {
			if (target.getType().is(Reference.MOB_TAG))
				return EventType.MELEE_TO_MOBS;
			if (target.getType().is(Reference.ANIMAL_TAG))
				return EventType.MELEE_TO_ANIMALS;
			if (target.getType().equals(EntityType.PLAYER))
				return EventType.MELEE_TO_PLAYERS;
			else
				return EventType.DEAL_MELEE_DAMAGE;
		}
	}
}
