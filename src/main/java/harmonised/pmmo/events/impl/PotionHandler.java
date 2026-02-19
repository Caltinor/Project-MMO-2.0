package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.setup.CommonSetup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;

import java.util.List;
import java.util.Map;

public class PotionHandler {
	private static final String BREWED = "brewXpAwarded";

	@SuppressWarnings("resource")
	public static void handle(PlayerBrewedPotionEvent event) {
		ItemStack stack = event.getStack();
		if (Config.server().general().brewingTracked() && stack.getOrDefault(CommonSetup.BREWED, false))
			return;
		Player player = event.getEntity();
		Core core = Core.get(player.level());
		boolean serverSide = !player.level().isClientSide();
		//process perks
		CompoundTag perkOutput = core.getPerkRegistry().executePerk(EventType.BREW, player, new CompoundTag());
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.BREW, stack, event.getEntity(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
		if (Config.server().general().brewingTracked())
			stack.set(CommonSetup.BREWED, true);
	}
}
