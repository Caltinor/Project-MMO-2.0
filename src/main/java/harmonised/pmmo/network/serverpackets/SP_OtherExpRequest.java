package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SetOtherExperience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.UUID;

public record SP_OtherExpRequest(UUID pid) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "c2s_other_xp_request");
	public SP_OtherExpRequest(FriendlyByteBuf buf) {this(buf.readUUID());}
	public void write(FriendlyByteBuf buf) {buf.writeUUID(pid);}
	
	public static void handle(SP_OtherExpRequest packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			Networking.sendToClient(new CP_SetOtherExperience(Core.get(LogicalSide.SERVER).getData().getXpMap(packet.pid())) , (ServerPlayer) ctx.player().get());
			MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Client request for Other Experience handled");
		});
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
