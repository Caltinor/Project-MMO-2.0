package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.List;
import java.util.Map;

public class FoodEatHandler {

	private static void internalHandle(Event event, Player player, ItemStack food) {
		Core core = Core.get(player.level());
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = !player.level().isClientSide();
		if (serverSide)
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.CONSUME, event, new CompoundTag());
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.CONSUME, player, eventHookOutput));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.CONSUME, food, player, perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
	public static void handle(LivingEntityUseItemEvent.Finish event) {
		if (event.getEntity() instanceof Player player && (event.getItem().has(DataComponents.FOOD) || event.getItem().getItem() instanceof PotionItem)) {
			internalHandle(event, player, event.getItem());
		}
	}
}
