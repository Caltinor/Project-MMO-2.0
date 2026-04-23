package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SP_SetVeinLimit(int limit) implements CustomPacketPayload {
	public static final Type<SP_SetVeinLimit> TYPE = new Type(Reference.rl("c2s_set_vein_limit"));
	@Override public Type<SP_SetVeinLimit> type() {return TYPE;}
	public static final StreamCodec<FriendlyByteBuf, SP_SetVeinLimit> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, SP_SetVeinLimit::limit, SP_SetVeinLimit::new
	);
	//Hard upper bound on per-player vein limit. Actual vein size is already gated by the
	//player's charge/cost attributes in VeinMiningLogic.applyVein, so this is just
	//defense-in-depth against a malicious client stuffing Integer.MAX_VALUE into the map.
	private static final int MAX_LIMIT = 10_000;

	public static void handle(SP_SetVeinLimit packet, IPayloadContext ctx) {
		int clamped = Math.max(0, Math.min(MAX_LIMIT, packet.limit()));
		ctx.enqueueWork(() -> {
			VeinMiningLogic.maxBlocksPerPlayer.put(ctx.player().getUUID(), clamped);
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "VeinLimit updated for: "+ctx.player().getScoreboardName()+" with: "+clamped);
		});
	}
}
