package harmonised.pmmo.network.clientpackets;

import java.util.function.Supplier;

import harmonised.pmmo.core.Core;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_ClearData {
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).resetDataForReload();
		});
		ctx.get().setPacketHandled(true);
	}
}
