package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageDealtHandler {

	@SuppressWarnings("resource")
	public static void handle(EntityInvulnerabilityCheckEvent event) {
		//Check the source entity isn't null.  This should also reduce
		//the number of events processed.
		if (event.getSource().getEntity() == null) return;

		//Execute actual logic only if the source is a player
		if (event.getSource().getEntity() instanceof Player player
				&& event.getEntity() instanceof LivingEntity target) {
			if (target.equals(player))
				return;
			Core core = Core.get(player.level());
			MsLoggy.INFO.log(LOG_CODE.EVENT,"Attack Type: "+EventType.DEAL_DAMAGE.name()+" | TargetType: "+target.getType().toString());
			
			
			//===========================DEFAULT LOGIC===================================
			if (!core.isActionPermitted(ReqType.WEAPON, player.getMainHandItem(), player)) {
				event.setInvulnerable(true);
				Messenger.sendDenialMsg(ReqType.WEAPON, player, player.getMainHandItem().getDisplayName());
				return;
			}
			if (!core.isActionPermitted(ReqType.KILL, target, player)) {
				event.setInvulnerable(true);
				Messenger.sendDenialMsg(ReqType.KILL, player, target.getDisplayName());
				return;
			}

			if (!player.level().isClientSide) {
				CompoundTag eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.DEAL_DAMAGE, event, new CompoundTag());
				if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) { 
					event.setInvulnerable(true);
				}
			}
			
		}
	}
	
	@SuppressWarnings({"resource", "unstable"})
	public static void handle(LivingDamageEvent.Pre event) {
		DamageContainer container = event.getContainer();
		DamageSource source = container.getSource();

		//Execute actual logic only if the source is a player
		if (source.getEntity() instanceof Player player) {
			LivingEntity target = event.getEntity();

			if (target.equals(player)) return;
			
			Core core = Core.get(player.level());
			String damageType = RegistryUtil.getId(source).toString();
			MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Source Type: "+damageType+" | Source Raw: "+source.getMsgId());
			//Process perks
			CompoundTag dataIn = TagBuilder.start()
					.withFloat(APIUtils.DAMAGE_IN, container.getNewDamage())
					.withFloat(APIUtils.DAMAGE_OUT, container.getNewDamage())
					.withString(APIUtils.DAMAGE_TYPE, RegistryUtil.getId(source).toString()).build();
			CompoundTag perkOutput = core.getPerkRegistry().executePerk(EventType.DEAL_DAMAGE, player, dataIn);
			MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Pre-Perk Damage:"+container.getNewDamage());
			if (perkOutput.contains(APIUtils.DAMAGE_OUT)) {
				float damageOut = perkOutput.getFloat(APIUtils.DAMAGE_OUT);
				MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Damage Modified from %s to %s".formatted(container.getNewDamage(), damageOut));
				event.getContainer().setNewDamage(damageOut);
			}
			MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Attack Type: "+damageType+" | Damage Out: "+container.getNewDamage());
			if (!player.level().isClientSide) {
				perkOutput.putString(APIUtils.DAMAGE_TYPE, damageType);
				Map<String, Long> xpAward = getExperienceAwards(core, target, container.getNewDamage(), source, player, perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
	
	private static Map<String, Long> getExperienceAwards(Core core, LivingEntity target, float damage, DamageSource source, Player player, CompoundTag dataIn) {
		Map<String, Long> mapOut = new HashMap<>();
		/* if the target entity is in the no xp tag, skip all calculations, including
		 * those for the item and projectile.  This is for a special use case involving
		 * entities that cannot die being a source of infinite XP.*/
		if (target.getType().is(Reference.NO_XP_DAMAGE_DEALT))
			return mapOut;
		float ultimateDamage = Math.min(damage, target.getHealth());
		ItemStack weapon = player.getMainHandItem();
		Entity srcEntity = source.getDirectEntity() != null ? source.getDirectEntity() : player;
		//get data from object configurations
		Functions.mergeMaps(
				core.getExperienceAwards(EventType.DEAL_DAMAGE, weapon, player, dataIn),
				core.getExperienceAwards(EventType.DEAL_DAMAGE, srcEntity, player, dataIn),
				core.getExperienceAwards(EventType.DEAL_DAMAGE, target, player, dataIn))
			.forEach((skill, xp) -> mapOut.put(skill, (long)(xp.floatValue() * ultimateDamage)));
		//get and supplement object data with fallback configs.
		//the fallback should only fill in where the configs do not have values
		Map<String, Map<String, Long>> config = Config.server().xpGains().dealtDamage();
		List<String> tags = config.keySet().stream()
				.filter(str -> {
					if (!str.contains("#"))
						return false;
					var registry = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
					var tag = registry.getTag(TagKey.create(Registries.DAMAGE_TYPE, Reference.of(str.substring(1))));
					return tag.map(type -> type.contains(source.typeHolder())).orElse(false);
				}).toList();
		Map<String, Long> tagXp = tags.stream().map(str -> config.get(str)).reduce((mapA, mapB) -> Functions.mergeMaps(mapA, mapB)).orElse(new HashMap<>());
		Functions.mergeMaps(config.getOrDefault(RegistryUtil.getId(source).toString(), new HashMap<>()), tagXp)
				.forEach((skill, xp) -> mapOut.putIfAbsent(skill, (long)(xp.floatValue() * ultimateDamage)));
		CoreUtils.applyXpModifiers(mapOut, core.getConsolidatedModifierMap(player));
		return mapOut;
	}
}
