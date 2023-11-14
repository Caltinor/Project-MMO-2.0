package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;

import harmonised.pmmo.core.Core;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;


public class CP_ResetXP {
	public void handle(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(null, new HashMap<>());
		});
		ctx.setPacketHandled(true);
	}
}
