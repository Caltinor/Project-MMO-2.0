package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record CP_SyncVein(double charge) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, CP_SyncVein> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, CP_SyncVein::charge, CP_SyncVein::new
	);
	public static final Type<CP_SyncVein> TYPE = new Type(Reference.rl("s2c_sync_vein"));
	@Override public Type<CP_SyncVein> type() {return TYPE;}
	public static void handle(CP_SyncVein packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "Vein Update on Client: "+packet.charge());
			VeinTracker.currentCharge = packet.charge();
		});
	}
}
