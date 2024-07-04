package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.function.Supplier;

import harmonised.pmmo.core.Core;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_ClearXp {
	public final String skill;
	public CP_SyncData_ClearXp() {this.skill = "all";}
	public CP_SyncData_ClearXp(String skill) {this.skill = skill;}

	public CP_SyncData_ClearXp(FriendlyByteBuf buf) {
		this(buf.readUtf());
	}
	public static void encode(CP_SyncData_ClearXp packet, FriendlyByteBuf buf) {buf.writeUtf(packet.skill);}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			var data = Core.get(LogicalSide.CLIENT).getData();
			if (this.skill.equals("all"))
				data.setXpMap(null, new HashMap<>());
			else
				data.getXpMap(null).remove(this.skill);
		});
		ctx.get().setPacketHandled(true);
	}
}
