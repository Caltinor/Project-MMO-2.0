package harmonised.pmmo.events.impl;

import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.LogicalSide;

public class LoginHandler {

	public static void handle(PlayerLoggedInEvent event) {
		Player player = event.getPlayer();
		Core core = Core.get(player.level);
		
		core.getPerkRegistry().terminatePerk(EventType.DISABLE_PERK, player, core.getSide());
		
		if (core.getSide().equals(LogicalSide.SERVER)) {
			//===========UPDATE DATA MIRROR=======================
			for (Map.Entry<String, Long> skillMap : core.getData().getXpMap(player.getUUID()).entrySet()) {
				Networking.sendToClient(new CP_UpdateExperience(skillMap.getKey(), skillMap.getValue()), (ServerPlayer) player);
			}
			Networking.sendToClient(new CP_UpdateLevelCache(((PmmoSavedData)core.getData()).getLevelCache()), (ServerPlayer) player);
			
			//===========EXECUTE FEATURE LOGIC====================
			((PmmoSavedData)core.getData()).awardScheduledXP(player.getUUID());
		}
	}
}
