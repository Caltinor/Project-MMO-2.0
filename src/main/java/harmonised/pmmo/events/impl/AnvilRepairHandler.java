package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

import java.util.List;
import java.util.Map;

public class AnvilRepairHandler {

	@SuppressWarnings("resource")
	public static void handle(AnvilRepairEvent event) {
		Core core = Core.get(event.getEntity().level());
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = !event.getEntity().level().isClientSide;
		EventType type = isEnchantBook(event.getLeft()) || isEnchantBook(event.getRight()) ? EventType.ENCHANT : EventType.ANVIL_REPAIR;
		if (serverSide)		
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, event.getEntity(), eventHookOutput));
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(type, event.getOutput(), event.getEntity(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}

	private static boolean isEnchantBook(ItemStack stack) {
		return stack.getItem() instanceof EnchantedBookItem;
	}
}
