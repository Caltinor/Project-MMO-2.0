package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent;

public class PotionHandler {
	private static final String BREWED = "brewXpAwarded";

	@SuppressWarnings("resource")
	public static void handle(PlayerBrewedPotionEvent event) {
		ItemStack stack = event.getStack();
		if (Config.BREWING_TRACKED.get() && stack.getTag() != null && stack.getTag().getBoolean(BREWED))
			return;
		Player player = event.getEntity();
		Core core = Core.get(player.level());
		boolean serverSide = !player.level().isClientSide; 
		//process perks
		CompoundTag perkOutput = core.getPerkRegistry().executePerk(EventType.BREW, player, new CompoundTag());
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.BREW, stack, event.getEntity(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
		if (Config.BREWING_TRACKED.get() && stack.getTag() != null)
			stack.getTag().putBoolean(BREWED, true);
	}
}
