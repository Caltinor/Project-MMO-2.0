package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.events.ClientTickHandler;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;

public class CP_UpdateExperience {
	String skill;
	Experience xp;
long change;

	
	public CP_UpdateExperience(String skill, Experience xp, long change) {
		this.skill = skill;
		this.xp = xp;
		this.change = change;
	}
	public CP_UpdateExperience(FriendlyByteBuf buf) {
		skill = buf.readUtf();
		long rawXP = buf.readLong();
		long level = buf.readLong();
		change = buf.readLong();
		xp = new Experience(new Experience.XpLevel(level), rawXP);
	}
	public void toBytes(FriendlyByteBuf buf) {
		buf.writeUtf(skill);
		buf.writeLong(xp.getXp());
		buf.writeLong(xp.getLevel().getLevel());
		buf.writeLong(change);
	}
	
	public void handle(NetworkEvent.Context ctx ) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getData().getXpMap(null).put(skill, xp);
			if (change > 0)
				ClientTickHandler.addToGainList(skill, change); //TODO maybe update this to display level increase + xp gained
			MsLoggy.DEBUG.log(LOG_CODE.XP, "Client Packet Handled for updating experience of "+skill+"["+xp+"]");
		});		
		ctx.setPacketHandled(true);
	}
}
