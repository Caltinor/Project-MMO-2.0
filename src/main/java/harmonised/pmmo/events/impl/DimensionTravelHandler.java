package harmonised.pmmo.events.impl;

import java.util.Map;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;

public class DimensionTravelHandler {

	public static void handle(EntityTravelToDimensionEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		if (!Core.get(player.level).doesPlayerMeetReq(ReqType.TRAVEL, event.getDimension().location(), player.getUUID())) {
			Map<String, Integer> req = Core.get(player.level).getSkillGates().getObjectSkillMap(ReqType.TRAVEL, event.getDimension().location());
			Messenger.sendDenialMsg(ReqType.TRAVEL, player, event.getDimension().location().toString(), MsLoggy.mapToString(req));
			event.setCanceled(true);
		}
	}
}
