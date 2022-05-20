package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.function.Supplier;

import harmonised.pmmo.core.Core;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_ResetXP {
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(null, new HashMap<>());
		});
		ctx.get().setPacketHandled(true);
	}
}
