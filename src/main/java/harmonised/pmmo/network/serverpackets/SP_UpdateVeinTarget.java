package harmonised.pmmo.network.serverpackets;

import java.util.function.Supplier;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class SP_UpdateVeinTarget {
	BlockPos pos;
	
	public SP_UpdateVeinTarget(BlockPos pos) {this.pos = pos;}
	public SP_UpdateVeinTarget(FriendlyByteBuf buf) {pos = BlockPos.of(buf.readLong());}
	public void toBytes(FriendlyByteBuf buf) {buf.writeLong(pos.asLong());}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Target sent to Server for pos: "+pos.toString());
			Core.get(LogicalSide.SERVER).getVeinData().setMarkedPos(ctx.get().getSender().getUUID(), pos);
		});
		ctx.get().setPacketHandled(true);
	}
}
