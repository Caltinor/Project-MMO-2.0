package harmonised.pmmo.network.serverpackets;

import java.util.function.Supplier;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SP_SetVeinLimit {
	int limit;
	public SP_SetVeinLimit(int limit) {this.limit = limit;}
	public SP_SetVeinLimit(FriendlyByteBuf buf) {this.limit = buf.readInt();}
	public void encode(FriendlyByteBuf buf) {buf.writeInt(limit);}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			VeinMiningLogic.maxBlocksPerPlayer.put(ctx.get().getSender().getUUID(), limit);
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "VeinLimit updated for: "+ctx.get().getSender().getScoreboardName()+" with: "+limit);
		});
		ctx.get().setPacketHandled(true);
	}
}
