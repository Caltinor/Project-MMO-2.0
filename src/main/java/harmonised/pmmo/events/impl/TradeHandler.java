package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TradeHandler {
    public static void handle(TradeWithVillagerEvent event) {
        ItemStack tradeA = event.getMerchantOffer().getCostA();
        ItemStack tradeB = event.getMerchantOffer().getCostB();
        ItemStack result = event.getMerchantOffer().getResult();

        Core core = Core.get(event.getEntity().level());

        CompoundTag eventHookOutput = new CompoundTag();
        boolean serverSide = core.getSide() == LogicalSide.SERVER;
        if (serverSide) {
            eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.GIVEN_AS_TRADE, event, new CompoundTag());
            eventHookOutput = TagUtils.mergeTags(eventHookOutput, core.getEventTriggerRegistry().executeEventListeners(EventType.RECEIVED_AS_TRADE, event, new CompoundTag()));
        }
        //Process perks
        CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.GIVEN_AS_TRADE, event.getEntity(), eventHookOutput));
        perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.RECEIVED_AS_TRADE, event.getEntity(), perkOutput));
        if (serverSide) {
            var costA = core.getExperienceAwards(EventType.GIVEN_AS_TRADE, tradeA, event.getEntity(), perkOutput).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * tradeA.getCount()));
            var costB = core.getExperienceAwards(EventType.GIVEN_AS_TRADE, tradeB, event.getEntity(), perkOutput).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * tradeB.getCount()));
            var outItem = core.getExperienceAwards(EventType.RECEIVED_AS_TRADE, result, event.getEntity(), perkOutput).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * result.getCount()));
            Map<String, Long> xpAward = Functions.mergeMaps(costA, costB, outItem);

            List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
            core.awardXP(partyMembersInRange, xpAward);
        }
    }
}
