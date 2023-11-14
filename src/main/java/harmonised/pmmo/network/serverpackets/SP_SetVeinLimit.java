package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SP_SetVeinLimit {
	int limit;
	public SP_SetVeinLimit(int limit) {this.limit = limit;}
	public SP_SetVeinLimit(FriendlyByteBuf buf) {this.limit = buf.readInt();}
	public void encode(FriendlyByteBuf buf) {buf.writeInt(limit);}
	public void handle(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			VeinMiningLogic.maxBlocksPerPlayer.put(ctx.getSender().getUUID(), limit);
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "VeinLimit updated for: "+ctx.getSender().getScoreboardName()+" with: "+limit);
		});
		ctx.setPacketHandled(true);
	}
}
