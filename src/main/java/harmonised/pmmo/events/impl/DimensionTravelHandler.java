package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;

public class DimensionTravelHandler {

	public static void handle(EntityTravelToDimensionEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		if (Core.get(player.level).doesPlayerMeetReq(ReqType.TRAVEL, event.getDimension().location(), player.getUUID()))
			event.setCanceled(true);
	}
}
