package harmonised.pmmo.network.clientpackets;

import java.util.function.Supplier;

import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CP_UpdateExperience {
	String skill;
	long xp;
	
	public CP_UpdateExperience(String skill, long xp) {
		this.skill = skill;
		this.xp = xp;
	}
	public CP_UpdateExperience(FriendlyByteBuf buf) {
		xp = buf.readLong();
		skill = buf.readUtf();
	}
	public void toBytes(FriendlyByteBuf buf) {
		buf.writeLong(xp);
		buf.writeUtf(skill);
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx ) {
		DataMirror.setExperience(skill, xp);
		MsLoggy.debug("Client Packet Handled for updating experience of "+skill+"["+xp+"]");
		ctx.get().setPacketHandled(true);
	}
}
