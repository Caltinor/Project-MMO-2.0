package harmonised.pmmo.events.impl;

import harmonised.pmmo.core.XpUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class BreakHandler {
	
	public static void handle(BreakEvent event) {
		XpUtils.setXpDiff(event.getPlayer().getUUID(), "testing", 20);
		long xpNow = XpUtils.getLevelFromXP(XpUtils.getPlayerXpRaw(event.getPlayer().getUUID(), "testing"));
		event.getPlayer().sendMessage(new TextComponent("Testing: " + xpNow), event.getPlayer().getUUID());
	}
}
