package harmonised.pmmo.util;

import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class Messenger {
	
	public static void sendDenialMsg(ReqType type, Player player, Object... name) {
		TranslatableComponent text = new TranslatableComponent("pmmo.msg.denial."+type.name().toLowerCase(), name);
		send(text, player);		
	}
	
	private static void send(Component text, Player player) {
		player.sendMessage(text, player.getUUID());
	}
}
