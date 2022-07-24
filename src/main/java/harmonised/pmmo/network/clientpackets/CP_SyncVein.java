package harmonised.pmmo.network.clientpackets;

import java.util.function.Supplier;

import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncVein {
	double charge;
	public CP_SyncVein(double charge) {this.charge = charge;}
	public CP_SyncVein(FriendlyByteBuf buf) {this(buf.readDouble());}
	public void encode(FriendlyByteBuf buf) {buf.writeDouble(charge);}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {			
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "Vein Update on Client: "+charge);
			VeinTracker.currentCharge = charge;
		});
		ctx.get().setPacketHandled(true);
	}
}
