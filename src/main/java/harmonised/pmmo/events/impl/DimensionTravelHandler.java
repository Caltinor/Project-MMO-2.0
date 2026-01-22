package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

import java.util.Map;

public class DimensionTravelHandler {

	public static void handle(EntityTravelToDimensionEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		if (!Core.get(player.level()).isActionPermitted(ReqType.TRAVEL, event.getDimension(), player)) {
			Map<String, Long> req = Core.get(player.level()).getObjectSkillMap(ObjectType.DIMENSION, event.getDimension().identifier(), ReqType.TRAVEL, new CompoundTag());
			Messenger.sendDenialMsg(ReqType.TRAVEL, player, event.getDimension().identifier().toString(), MsLoggy.mapToString(req));
			event.setCanceled(true);
		}
	}
}
