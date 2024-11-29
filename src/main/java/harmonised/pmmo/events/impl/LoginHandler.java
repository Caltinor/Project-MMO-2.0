package harmonised.pmmo.events.impl;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_ResetXP;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.serverpackets.SP_SetVeinLimit;
import harmonised.pmmo.network.serverpackets.SP_ToggleBreakSpeed;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;

public class LoginHandler {

	public static void handle(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		Core core = Core.get(player.level());
		
		if (core.getSide().equals(LogicalSide.SERVER)) {
			//===========UPDATE DATA MIRROR=======================
			Networking.sendToClient(new CP_ResetXP(), (ServerPlayer) player);
			for (Map.Entry<String, Experience> skillMap : core.getData().getXpMap(player.getUUID()).entrySet()) {
				Networking.sendToClient(new CP_UpdateExperience(skillMap.getKey(), skillMap.getValue(), 0), (ServerPlayer) player);
			}
			//===========EXECUTE FEATURE LOGIC====================
			((PmmoSavedData)core.getData()).awardScheduledXP(player.getUUID());
		}
		else {
			Networking.sendToServer(new SP_SetVeinLimit(Config.VEIN_LIMIT.get()));
			Networking.sendToServer(new SP_ToggleBreakSpeed(Config.BREAK_SPEED_PERKS.get()));
		}
	}
}
