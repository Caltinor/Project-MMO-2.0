package harmonised.pmmo.network.clientpackets;


import harmonised.pmmo.core.Core;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;

public class CP_ClearData {
	public void handle(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).resetDataForReload();
			Core.get(LogicalSide.CLIENT).getLoader().resetData();
			Core.get(LogicalSide.CLIENT).getTooltipRegistry().clearRegistry();
		});
		ctx.setPacketHandled(true);
	}
}
