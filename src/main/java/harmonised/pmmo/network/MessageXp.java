package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.XPOverlayGUI;

import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageXp
{
	public double xp, gainedXp;
	public String skill;
	public boolean skip;
	
	public MessageXp( double xp, String skill, double gainedXp, boolean skip )
	{
		this.xp = xp;
		this.gainedXp = gainedXp;
		this.skill = skill;
		this.skip = skip;
	}
	
	public MessageXp()
	{
		
	}

	public static void encode( MessageXp packet, PacketBuffer buf )
	{
		buf.writeDouble( packet.xp );
		buf.writeDouble( packet.gainedXp );
		buf.writeString( packet.skill );
		buf.writeBoolean( packet.skip );
	}

	public static MessageXp decode( PacketBuffer buf )
	{
		MessageXp packet = new MessageXp();
		packet.xp = buf.readDouble();
		packet.gainedXp = buf.readDouble();
		packet.skill = buf.readString();
		packet.skip = buf.readBoolean();

		return packet;
	}

	public static void handlePacket( MessageXp packet, Supplier<NetworkEvent.Context> ctx )
	{
		packetHandler.handleXpPacket( packet, ctx );
	}
}
