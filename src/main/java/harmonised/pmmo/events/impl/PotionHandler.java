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

	public static void handle(PlayerBrewedPotionEvent event) {
		Player player = event.getPlayer();
		Core core = Core.get(player.getLevel());
		boolean serverSide = !player.level.isClientSide; 
		//proecess perks
		CompoundTag perkDataIn = new CompoundTag();
		//if break data is needed by perks, we can add it here.  this is just default implementation.
		CompoundTag perkOutput = core.getPerkRegistry().executePerk(EventType.BREW, player, perkDataIn, core.getSide());
		if (serverSide) {
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.BREW, event.getStack(), event.getPlayer(), perkOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
