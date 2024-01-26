package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SP_SetVeinLimit(int limit) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "c2s_set_vein_limit");
	@Override public ResourceLocation id() {return ID;}
	public SP_SetVeinLimit(FriendlyByteBuf buf) {this(buf.readInt());}
	@Override
	public void write(FriendlyByteBuf buf) {buf.writeInt(limit);}
	public static void handle(SP_SetVeinLimit packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			VeinMiningLogic.maxBlocksPerPlayer.put(ctx.player().get().getUUID(), packet.limit());
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "VeinLimit updated for: "+ctx.player().get().getScoreboardName()+" with: "+packet.limit());
		});
	}
}
