package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;


public record CP_SyncVein(double charge) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "s2c_sync_vein");
	@Override public ResourceLocation id() {return ID;}
	public CP_SyncVein(FriendlyByteBuf buf) {this(buf.readDouble());}
	@Override
	public void write(FriendlyByteBuf buf) {buf.writeDouble(charge());}
	public static void handle(CP_SyncVein packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "Vein Update on Client: "+packet.charge());
			VeinTracker.currentCharge = packet.charge();
		});
	}
}
