package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreathHandler {
    public static void handle(LivingBreatheEvent event) {
        if (event.getEntity() instanceof Player player && player.tickCount % 10 == 0) {
            int diff = event.canBreathe() ? event.getRefillAirAmount() : event.getConsumeAirAmount();
            if (diff == 0) return;
            Core core = Core.get(player.level());
            boolean serverSide = core.getSide() == LogicalSide.SERVER;
            CompoundTag eventHookOutput = new CompoundTag();
            if (serverSide)
                eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.BREATH_CHANGE, event, new CompoundTag());
            CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.BREATH_CHANGE, player, eventHookOutput));
            if (serverSide) {
                Map<String, Double> ratio = Config.server().xpGains().playerXp(EventType.BREATH_CHANGE);
                Identifier source = Reference.mc("player");
                final Map<String, Long> xpAward = perkOutput.contains(APIUtils.SERIALIZED_AWARD_MAP)
                        ? CoreUtils.deserializeAwardMap(perkOutput.getCompound(APIUtils.SERIALIZED_AWARD_MAP).get())
                        : new HashMap<>();
                ratio.keySet().forEach((skill) -> {
                    Double value = ratio.getOrDefault(skill, 0d) * diff;
                    xpAward.put(skill, value.longValue());
                });
                CoreUtils.applyXpModifiers(xpAward, core.getConsolidatedModifierMap(player));
                CheeseTracker.applyAntiCheese(EventType.BREATH_CHANGE, source, player, xpAward);

                List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
                core.awardXP(partyMembersInRange, xpAward);
            }
        }
    }
}
