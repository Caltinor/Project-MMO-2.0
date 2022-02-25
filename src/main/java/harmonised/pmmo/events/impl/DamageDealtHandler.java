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
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class DamageDealtHandler {

	public static void handle(LivingAttackEvent event) {
		//Check the source entity isn't null.  This should also reduce
		//the number of events processed.
		if (event.getSource().getEntity() == null) return;
		
		//Execute actual logic only if the source is a player
		if (event.getSource().getEntity() instanceof Player) {
			LivingEntity target = event.getEntityLiving();
			//Confirm our target is a living entity
			if (target == null) return;
			
			Player player = (Player) event.getSource().getEntity();
			if (target.equals(player))
				return;
			Core core = Core.get(player.level);
			EventType type = getEventCategory(event.getSource().isProjectile(), event.getEntityLiving());
			MsLoggy.info("Attack Type: "+type.name()+" | TargetType: "+target.getType().toString());
			
			
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
				if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
					event.setCanceled(true);
			}
			//Process perks
			CompoundTag perkDataIn = eventHookOutput;
			perkDataIn.putFloat(APIUtils.DAMAGE_IN, event.getAmount());
			//TODO add the WEAPON_TYPE parameter using "item_specific" settings from the AutoValuesConfig
			CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, player, perkDataIn, core.getSide()));
			if (serverSide) {
				Map<String, Long> xpAward = getExperienceAwards(core, type, target, event.getAmount(), player, perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
	
	private static Map<String, Long> getExperienceAwards(Core core, EventType type, LivingEntity target, float damage, Player player, CompoundTag dataIn) {
		Map<String, Long> mapOut = new HashMap<>();
		float ultimateDamage = Math.min(damage, target.getHealth());
		switch (type) {
		case MELEE_TO_MOBS: case MELEE_TO_ANIMALS: case MELEE_TO_PLAYERS: case RANGED_TO_MOBS: case RANGED_TO_ANIMALS: case RANGED_TO_PLAYERS: {
			core.getExperienceAwards(type, target, player, dataIn).forEach((skill, value) -> {
				mapOut.put(skill, (long)((float)value * ultimateDamage));
			});
			break;
		}
		case DEAL_MELEE_DAMAGE: {
			Double value = ultimateDamage * Config.DEAL_MELEE_DAMAGE_MODIFIER.get();
			Config.DEAL_MELEE_DAMAGE_SKILLS.get().forEach((skill) -> {
				mapOut.put(skill, value.longValue());
			});
			break;
		}
		case DEAL_RANGED_DAMAGE: {
			Double value = ultimateDamage * Config.DEAL_RANGED_DAMAGE_MODIFIER.get();
			Config.DEAL_RANGED_DAMAGE_SKILLS.get().forEach((skill) -> {
				mapOut.put(skill, value.longValue());
			});
			break;
		}
		default: {return new HashMap<>();}
		}
		return mapOut;
	}
	
	private static final ResourceLocation MOB_TAG = new ResourceLocation("pmmo:mobs");
	private static final ResourceLocation ANIMAL_TAG = new ResourceLocation("pmmo:animals");
	private static EventType getEventCategory(boolean projectileSource, LivingEntity target) {
		if (projectileSource) {
			if (EntityTypeTags.getAllTags().getTag(MOB_TAG).contains(target.getType()))
				return EventType.RANGED_TO_MOBS;
			if (EntityTypeTags.getAllTags().getTag(ANIMAL_TAG).contains(target.getType()))
				return EventType.RANGED_TO_ANIMALS;
			if (target.getType().equals(EntityType.PLAYER))
				return EventType.RANGED_TO_PLAYERS;
			else
				return EventType.DEAL_RANGED_DAMAGE;
		}
		else {
			if (EntityTypeTags.getAllTags().getTag(MOB_TAG).contains(target.getType()))
				return EventType.MELEE_TO_MOBS;
			if (EntityTypeTags.getAllTags().getTag(ANIMAL_TAG).contains(target.getType()))
				return EventType.MELEE_TO_ANIMALS;
			if (target.getType().equals(EntityType.PLAYER))
				return EventType.MELEE_TO_PLAYERS;
			else
				return EventType.DEAL_MELEE_DAMAGE;
		}
	}
}
