package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MitigatedDamageHandler {

    public static void handle(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            float mitigatedDamage = Arrays.stream(DamageContainer.Reduction.values()).map(event::getReduction).reduce(Float::sum).orElse(0f);
            DamageSource source = event.getSource();

            if (player.equals(source.getEntity())) return;

            Core core = Core.get(player.level());
            String damageType = RegistryUtil.getId(player.level().registryAccess(), Registries.DAMAGE_TYPE, source.type()).toString();
            MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.EVENT, "Source Type: "+damageType+" | Source Raw: "+source.getMsgId());

            boolean serverSide = player instanceof ServerPlayer;
            CompoundTag eventHookOutput = new CompoundTag();
            if (serverSide)  eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.MITIGATE_DAMAGE, event, new CompoundTag());

            CompoundTag perkDataIn = eventHookOutput.copy();
            perkDataIn.putString(APIUtils.DAMAGE_TYPE, damageType);
            perkDataIn.putFloat(APIUtils.DAMAGE_IN, mitigatedDamage);
            CompoundTag perkOutput = TagUtils.mergeTags(perkDataIn, core.getPerkRegistry().executePerk(EventType.MITIGATE_DAMAGE,  player, perkDataIn));

            if (serverSide) {
                perkOutput.putString(APIUtils.DAMAGE_TYPE, damageType);
                Map<String, Long> xpAward = getExperienceAwards(core, source, mitigatedDamage, player, perkOutput);
                List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
                core.awardXP(partyMembersInRange, xpAward);
            }
        }
    }

    private static Map<String, Long> getExperienceAwards(Core core, DamageSource source, float mitigation, Player player, CompoundTag dataIn) {
        Map<String, Long> mapOut = new HashMap<>();
        if (source.getEntity() != null)
            core.getExperienceAwards(EventType.MITIGATE_DAMAGE, source.getEntity(), player, dataIn)
                    .forEach((skill, value) -> mapOut.put(skill, (long)(value.floatValue() * mitigation)));
        Map<String, Map<String, Long>> config = Config.server().xpGains().mitigatedDamage();
        List<String> tags = config.keySet().stream()
                .filter(str -> {
                    if (!str.contains("#"))
                        return false;
                    var registry = player.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
                    var tag = registry.get(TagKey.create(Registries.DAMAGE_TYPE, Reference.of(str.substring(1))));
                    return tag.map(type -> type.contains(source.typeHolder())).orElse(false);
                }).toList();
        Map<String, Long> tagXp = tags.stream().map(config::get).reduce(Functions::mergeMaps).orElse(new HashMap<>());
        Functions.mergeMaps(config.getOrDefault(RegistryUtil.getId(player.level().registryAccess(), Registries.DAMAGE_TYPE ,source.type()).toString(), new HashMap<>()), tagXp)
                .forEach((skill, xp) -> mapOut.putIfAbsent(skill, (long)(xp.floatValue() * mitigation)));
        CoreUtils.applyXpModifiers(mapOut, core.getConsolidatedModifierMap(player));
        return mapOut;
    }
}
