package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent;

public class PotionHandler {
	private static final String BREWED = "brewXpAwarded";

	public static void handle(PlayerBrewedPotionEvent event) {
		if (event.getStack().getTag().getBoolean(BREWED))
			return;
		Player player = event.getEntity();
		Core core = Core.get(player.getLevel());
		boolean serverSide = !player.level.isClientSide; 
		//proecess perks
		CompoundTag perkOutput = core.getPerkRegistry().executePerk(EventType.BREW, player, new CompoundTag());
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.BREW, event.getStack(), event.getEntity(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
		event.getStack().getTag().putBoolean(BREWED, true);
	}
}
