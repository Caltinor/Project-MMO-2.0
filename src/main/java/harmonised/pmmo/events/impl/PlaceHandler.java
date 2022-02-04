package harmonised.pmmo.events.impl;

import harmonised.pmmo.setup.Core;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;

public class PlaceHandler {
	public static void handle(EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Core.get((Level)event.getWorld()).getXpUtils().setXpDiff(event.getEntity().getUUID(), "building", 20);
		long xpNow = PmmoSavedData.get().getXpRaw(event.getEntity().getUUID(), "building");
		event.getEntity().sendMessage(new TextComponent("Building: " + xpNow), event.getEntity().getUUID());
	}
}
