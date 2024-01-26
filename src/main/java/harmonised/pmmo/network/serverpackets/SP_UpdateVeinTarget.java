package harmonised.pmmo.network.serverpackets;

import java.util.function.Supplier;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SP_UpdateVeinTarget(BlockPos pos) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "c2s_update_vein_target");
	@Override public ResourceLocation id() {return ID;}

	public SP_UpdateVeinTarget(FriendlyByteBuf buf) {this(BlockPos.of(buf.readLong()));}
	@Override
	public void write(FriendlyByteBuf buf) {buf.writeLong(pos.asLong());}
	
	public static void handle(SP_UpdateVeinTarget packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Target sent to Server for pos: "+packet.pos().toString());
			Core.get(LogicalSide.SERVER).setMarkedPos(ctx.player().get().getUUID(), packet.pos());
		});
	}
}
