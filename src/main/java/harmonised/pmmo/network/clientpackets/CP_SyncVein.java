package harmonised.pmmo.network.clientpackets;

import java.util.function.Supplier;

import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;


public class CP_SyncVein {
	double charge;
	public CP_SyncVein(double charge) {this.charge = charge;}
	public CP_SyncVein(FriendlyByteBuf buf) {this(buf.readDouble());}
	public void encode(FriendlyByteBuf buf) {buf.writeDouble(charge);}
	public void handle(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "Vein Update on Client: "+charge);
			VeinTracker.currentCharge = charge;
		});
		ctx.setPacketHandled(true);
	}
}
