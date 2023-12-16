package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.StatAwardEvent;

import java.util.List;
import java.util.Map;

public class StatsHandler {
    public static void handle(StatAwardEvent event) {
        Player player = event.getEntity();
        Core core = Core.get(player.level());
        if (core.getSide() != LogicalSide.SERVER) return;

        if (event.getStat().getType().equals(Stats.ITEM_BROKEN)) {
            CompoundTag eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.TOOL_BREAKING, event, new CompoundTag());
            //Process perks
            CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.TOOL_BREAKING, event.getEntity(), eventHookOutput));
            Item item = (Item) event.getStat().getValue();
            Map<String, Long> xpAward = core.getExperienceAwards(EventType.TOOL_BREAKING, new ItemStack(item), player, perkOutput);
            List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
            core.awardXP(partyMembersInRange, xpAward);
        }
    }
}
