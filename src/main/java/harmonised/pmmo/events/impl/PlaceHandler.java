package harmonised.pmmo.events.impl;

import harmonised.pmmo.core.XpUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;

public class PlaceHandler {
	public static void handle(EntityPlaceEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		XpUtils.setXpDiff(event.getEntity().getUUID(), "building", 20);
		long xpNow = XpUtils.getLevelFromXP(XpUtils.getPlayerXpRaw(event.getEntity().getUUID(), "building"));
		event.getEntity().sendMessage(new TextComponent("Building: " + xpNow), event.getEntity().getUUID());
	}
}
