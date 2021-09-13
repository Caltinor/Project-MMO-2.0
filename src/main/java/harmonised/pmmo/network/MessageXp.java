package harmonised.pmmo.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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

	public static void encode( MessageXp packet, FriendlyByteBuf buf )
	{
		buf.writeDouble( packet.xp );
		buf.writeDouble( packet.gainedXp );
		buf.writeUtf( packet.skill );
		buf.writeBoolean( packet.skip );
	}

	public static MessageXp decode( FriendlyByteBuf buf )
	{
		MessageXp packet = new MessageXp();
		packet.xp = buf.readDouble();
		packet.gainedXp = buf.readDouble();
		packet.skill = buf.readUtf();
		packet.skip = buf.readBoolean();

		return packet;
	}

	public static void handlePacket( MessageXp packet, Supplier<NetworkEvent.Context> ctx )
	{
		packetHandler.handleXpPacket( packet, ctx );
	}
}
