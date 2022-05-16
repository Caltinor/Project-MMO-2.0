package harmonised.pmmo.network.serverpackets;

import java.util.UUID;
import java.util.function.Supplier;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SetOtherExperience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class SP_OtherExpRequest {
	UUID pid;
	
	public SP_OtherExpRequest(UUID playerID) {pid = playerID;}
	public SP_OtherExpRequest(FriendlyByteBuf buf) {pid = buf.readUUID();}
	public void toBytes(FriendlyByteBuf buf) {buf.writeUUID(pid);}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Networking.sendToClient(new CP_SetOtherExperience(Core.get(LogicalSide.SERVER).getData().getXpMap(pid)) ,ctx.get().getSender());
			MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Client request for Other Experience handled");
		});
		ctx.get().setPacketHandled(true);
	}
}
