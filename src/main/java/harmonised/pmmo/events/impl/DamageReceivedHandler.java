package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DamageReceivedHandler {

	@SuppressWarnings("resource")
	public static void handle(LivingHurtEvent event) {
		if (event.getEntity() instanceof Player) {			
			Player player = (Player) event.getEntity();
			if (player.equals(event.getSource().getEntity()))
				return;
			Core core = Core.get(player.level());
			String damageType = RegistryUtil.getId(event.getSource()).toString();
			MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Source Type: "+damageType+" | Source Raw: "+event.getSource().getMsgId());
			
			boolean serverSide = !player.level().isClientSide;
			CompoundTag eventHookOutput = new CompoundTag();
			if (serverSide){
				eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.RECEIVE_DAMAGE, event, new CompoundTag());
				if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) { 
					event.setCanceled(true);
					return;
				}
			}
			//Process perks
			CompoundTag perkDataIn = eventHookOutput.copy();
			perkDataIn.putString(APIUtils.DAMAGE_TYPE, damageType);
			perkDataIn.putFloat(APIUtils.DAMAGE_IN, event.getAmount());
			perkDataIn.putFloat(APIUtils.DAMAGE_OUT, event.getAmount());
			CompoundTag perkOutput = TagUtils.mergeTags(perkDataIn, core.getPerkRegistry().executePerk(EventType.RECEIVE_DAMAGE,  player, perkDataIn));
			if (perkOutput.contains(APIUtils.DAMAGE_OUT)) {
				float damageOut = perkOutput.getFloat(APIUtils.DAMAGE_OUT);
				MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Damage Modified from %s to %s".formatted(event.getAmount(), damageOut));
				event.setAmount(damageOut);
			}
			if (serverSide) {
				perkOutput.putString(APIUtils.DAMAGE_TYPE, damageType);
				Map<String, Long> xpAward = getExperienceAwards(core, event.getSource(), event.getAmount(), player, perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
	
	private static Map<String, Long> getExperienceAwards(Core core, DamageSource source, float damage, Player player, CompoundTag dataIn) {
		Map<String, Long> mapOut = new HashMap<>();
		float ultimateDamage = Mth.clamp(damage, 0, player.getHealth());
		if (source.getEntity() != null)
			core.getExperienceAwards(EventType.RECEIVE_DAMAGE, source.getEntity(), player, dataIn)
					.forEach((skill, value) -> mapOut.put(skill, (long)(value.floatValue() * ultimateDamage)));
		Map<String, Map<String, Long>> config = Config.server().xpGains().receivedDamage();
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
