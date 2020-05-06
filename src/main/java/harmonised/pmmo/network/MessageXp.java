package harmonised.pmmo.network;

import harmonised.pmmo.gui.XPOverlayGUI;

import harmonised.pmmo.skills.Skill;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageXp
{
	private double xp, gainedXp;
	private int skill;
	private boolean skip;
	
	public MessageXp( double xp, int skill, double gainedXp, boolean skip )
	{
		this.xp = xp;
		this.gainedXp = gainedXp;
		this.skill = skill;
		this.skip = skip;
	}
	
	public MessageXp()
	{
		
	}

	public static MessageXp decode( PacketBuffer buf )
	{
		MessageXp packet = new MessageXp();
		packet.xp = buf.readDouble();
		packet.gainedXp = buf.readDouble();
		packet.skill = buf.readInt();
		packet.skip = buf.readBoolean();

		return packet;
	}

	public static void encode( MessageXp packet, PacketBuffer buf )
	{
		buf.writeDouble( packet.xp );
		buf.writeDouble( packet.gainedXp );
		buf.writeInt( packet.skill );
		buf.writeBoolean( packet.skip );
	}

	public static void handlePacket( MessageXp packet, Supplier<NetworkEvent.Context> ctx )
	{
		ctx.get().enqueueWork(() ->
		{
			if( packet.skill == 42069 )
				XPOverlayGUI.clearXP();
			else
				XPOverlayGUI.makeXpDrop( packet.xp, Skill.getSkill( packet.skill ), 10000, packet.gainedXp, packet.skip );
		});
		ctx.get().setPacketHandled(true);
	}
}
