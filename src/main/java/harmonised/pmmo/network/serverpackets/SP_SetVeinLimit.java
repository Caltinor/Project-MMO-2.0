package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SP_SetVeinLimit(int limit) implements CustomPacketPayload {
	public static final Type<SP_SetVeinLimit> TYPE = new Type(Reference.rl("c2s_set_vein_limit"));
	@Override public Type<SP_SetVeinLimit> type() {return TYPE;}
	public static final StreamCodec<FriendlyByteBuf, SP_SetVeinLimit> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, SP_SetVeinLimit::limit, SP_SetVeinLimit::new
	);
	public static void handle(SP_SetVeinLimit packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			VeinMiningLogic.maxBlocksPerPlayer.put(ctx.player().getUUID(), packet.limit());
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "VeinLimit updated for: "+ctx.player().getScoreboardName()+" with: "+packet.limit());
		});
	}
}
