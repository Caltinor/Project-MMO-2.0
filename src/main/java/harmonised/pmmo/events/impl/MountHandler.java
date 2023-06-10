package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Messenger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityMountEvent;

public class MountHandler {

	public static void handle(EntityMountEvent event) {
		if (event.getEntityMounting() instanceof Player && event.isMounting()) {
			Player player = (Player) event.getEntityMounting();
			Entity mount = event.getEntityBeingMounted();
			Core core = Core.get(player.level());
			
			if (!core.isActionPermitted(ReqType.RIDE, mount, player)) {
				event.setCanceled(true);
				Messenger.sendDenialMsg(ReqType.RIDE, player, mount.getDisplayName());
				return;
			}
		}
	}
}
