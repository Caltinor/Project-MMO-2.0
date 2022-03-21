package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class DamageReceivedHandler {

	public static void handle(LivingHurtEvent event) {
		if (event.getEntity() instanceof Player) {			
			Player player = (Player) event.getEntity();
			EventType type = getSourceCategory(event.getSource());
			if (type.equals(EventType.FROM_PLAYERS) && player.equals(event.getSource().getEntity()))
				return;
			Core core = Core.get(player.getLevel());
			MsLoggy.info("Source Type: "+type.name()+" | Source Raw: "+event.getSource().msgId);
			
			boolean serverSide = !player.level.isClientSide;
			CompoundTag eventHookOutput = new CompoundTag();
			if (serverSide){
				eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
				if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) { 
					event.setCanceled(true);
					return;
				}
			}
			//Process perks
			CompoundTag perkDataIn = eventHookOutput;
			perkDataIn.putFloat(APIUtils.DAMAGE_IN, event.getAmount());
			CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type,  player, perkDataIn, core.getSide()));
			if (perkOutput.contains(APIUtils.DAMAGE_OUT))
				event.setAmount(perkOutput.getFloat(APIUtils.DAMAGE_OUT));
			if (serverSide) {
				Map<String, Long> xpAward = getExperienceAwards(core, type, event.getSource(), event.getAmount(), player, perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
	
	private static Map<String, Long> getExperienceAwards(Core core, EventType type, DamageSource source, float damage, Player player, CompoundTag dataIn) {	
		Map<String, Long> mapOut = new HashMap<>();
		float ultimateDamage = Math.min(damage, player.getHealth());
		switch (type) {
		case FROM_PLAYERS: case FROM_MOBS: case FROM_ANIMALS:{
			core.getExperienceAwards(type, source.getEntity(), player, dataIn).forEach((skill, value) -> {
				mapOut.put(skill, (long)((float)value * ultimateDamage));
			});			
			break;
		}
		case FROM_ENVIRONMENT: {
			Config.FROM_ENVIRONMENT_XP.get().keySet().forEach((skill) -> {	
				Double value = ultimateDamage * Config.FROM_ENVIRONMENT_XP.get().getOrDefault(skill, 0d);
				mapOut.put(skill, value.longValue());
			});
			break;
		}
		case FROM_IMPACT: {
			Config.FROM_IMPACT_XP.get().keySet().forEach((skill) -> {	
				Double value = ultimateDamage * Config.FROM_IMPACT_XP.get().getOrDefault(skill, 0d);
				mapOut.put(skill, value.longValue());
			});
			break;
		} 
		case FROM_MAGIC: {
			Config.FROM_MAGIC_XP.get().keySet().forEach((skill) -> {
				Double value = ultimateDamage * Config.FROM_MAGIC_XP.get().getOrDefault(skill, 0d);
				mapOut.put(skill, value.longValue());
			});
			break;
		}
		case RECEIVE_DAMAGE: case FROM_PROJECTILES: {
			Entity uncategorizedEntity = source.getEntity();
			if (uncategorizedEntity != null) 
				core.getExperienceAwards(type, uncategorizedEntity, player, dataIn).forEach((skill, value) -> {
					mapOut.put(skill, (long)((float)value * ultimateDamage));
				});
			else {
				Config.RECEIVE_DAMAGE_XP.get().keySet().forEach((skill) -> {	
					Double value = ultimateDamage * Config.RECEIVE_DAMAGE_XP.get().getOrDefault(skill, 0d);
					mapOut.put(skill, value.longValue());
				});
			}
			break;
		}
		default: {	return new HashMap<>();	}
		}
		return mapOut;
	}
	
	private static final List<String> environmental = List.of(
			DamageSource.IN_FIRE.msgId,
			DamageSource.LIGHTNING_BOLT.msgId,
			DamageSource.ON_FIRE.msgId,
			DamageSource.LAVA.msgId,
			DamageSource.HOT_FLOOR.msgId,
			DamageSource.IN_WALL.msgId,
			DamageSource.CRAMMING.msgId,
			DamageSource.DROWN.msgId,
			DamageSource.STARVE.msgId,
			DamageSource.CACTUS.msgId,
			DamageSource.ANVIL.msgId,
			DamageSource.FALLING_BLOCK.msgId,
			DamageSource.SWEET_BERRY_BUSH.msgId,
			DamageSource.FREEZE.msgId,
			DamageSource.FALLING_STALACTITE.msgId);
	private static final List<String> falling = List.of(
			DamageSource.FALL.msgId,
			DamageSource.STALAGMITE.msgId,
			DamageSource.FLY_INTO_WALL.msgId);
	private static final List<String> magic = List.of(
			DamageSource.MAGIC.msgId,
			"indirectMagic");
	private static final TagKey<EntityType<?>> MOB_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation("pmmo:mobs"));
	private static final TagKey<EntityType<?>> ANIMAL_TAG = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation("pmmo:animals"));
	private static EventType getSourceCategory(DamageSource source) {
		if (source.msgId.equals("player"))
			return EventType.FROM_PLAYERS;
		if (environmental.contains(source.msgId))
			return EventType.FROM_ENVIRONMENT;
		if (falling.contains(source.msgId))
			return EventType.FROM_IMPACT;
		if (magic.contains(source.msgId))
			return EventType.FROM_MAGIC;
		if (source.getEntity() != null) {
			if (source.getEntity().getType().is(MOB_TAG))
				return EventType.FROM_MOBS;
			if (source.getEntity().getType().is(ANIMAL_TAG))
				return EventType.FROM_ANIMALS;
		}
		if (source.isProjectile())
			return EventType.FROM_PROJECTILES;
		return EventType.RECEIVE_DAMAGE;
	}
}
