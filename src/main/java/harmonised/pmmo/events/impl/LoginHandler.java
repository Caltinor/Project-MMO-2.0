package harmonised.pmmo.events.impl;

import java.util.Map;

import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

public class LoginHandler {

	public static void handle(PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		
		//===========UPDATE DATA MIRROR=======================
		for (Map.Entry<String, Long> skillMap : PmmoSavedData.get().getXpMap(player.getUUID()).entrySet()) {
			Networking
			.sendToClient(
					new CP_UpdateExperience(skillMap.getKey(), skillMap.getValue()), 
					player);
		}
		Networking.sendToClient(new CP_UpdateLevelCache(XpUtils.getLevelCache()), player);
	}
}
