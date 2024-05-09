package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SP_UpdateVeinTarget(BlockPos pos) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, SP_UpdateVeinTarget> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SP_UpdateVeinTarget::pos, SP_UpdateVeinTarget::new
	);
	public static final Type<SP_UpdateVeinTarget> TYPE = new Type(new ResourceLocation(Reference.MOD_ID, "c2s_update_vein_target"));
	@Override public Type<SP_UpdateVeinTarget> type() {return TYPE;}
	
	public static void handle(SP_UpdateVeinTarget packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Target sent to Server for pos: "+packet.pos().toString());
			Core.get(LogicalSide.SERVER).setMarkedPos(ctx.player().getUUID(), packet.pos());
		});
	}
}
