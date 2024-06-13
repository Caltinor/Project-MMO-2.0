package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SetOtherExperience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SP_OtherExpRequest(UUID pid) implements CustomPacketPayload {
	public static final Type<SP_OtherExpRequest> TYPE = new Type(Reference.rl("c2s_other_xp_request"));
	@Override public Type<SP_OtherExpRequest> type() {return TYPE;}
	public static final StreamCodec<FriendlyByteBuf, SP_OtherExpRequest> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, SP_OtherExpRequest::pid, SP_OtherExpRequest::new
	);
	
	public static void handle(SP_OtherExpRequest packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Networking.sendToClient(new CP_SetOtherExperience(Core.get(LogicalSide.SERVER).getData().getXpMap(packet.pid())) , (ServerPlayer) ctx.player());
			MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Client request for Other Experience handled");
		});
	}
}
