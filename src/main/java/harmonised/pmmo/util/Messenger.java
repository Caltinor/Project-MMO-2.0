package harmonised.pmmo.util;

import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class Messenger {
	
	public static void sendDenialMsg(ReqType type, Player player, Object... name) {
		MutableComponent text = Component.translatable("pmmo.msg.denial."+type.name().toLowerCase(), name);
		send(text, player);		
	}
	
	private static void send(Component text, Player player) {
		player.sendSystemMessage(text);
	}
}
