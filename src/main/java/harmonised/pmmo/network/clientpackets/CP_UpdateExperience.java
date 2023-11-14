package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.events.ClientTickHandler;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;

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
	
	public void handle(NetworkEvent.Context ctx ) {
		ctx.enqueueWork(() -> {
			long currentXPraw = Core.get(LogicalSide.CLIENT).getData().getXpRaw(null, skill);
			//fixes #18: do not add a skill to the player map with zero xp
			//this causes a skill to display with zero xp and clutters the screen.
			if(currentXPraw != xp) {
				Core.get(LogicalSide.CLIENT).getData().setXpRaw(null, skill, xp);
				ClientTickHandler.addToGainList(skill, xp-currentXPraw);
				MsLoggy.DEBUG.log(LOG_CODE.XP, "Client Packet Handled for updating experience of "+skill+"["+xp+"]");
			}
		});		
		ctx.setPacketHandled(true);
	}
}
