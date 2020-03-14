package harmonised.pmmo.network;

import harmonised.pmmo.gui.XPOverlayGUI;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageXp
{
	private float xp;
	private int skill;
	private float gainedXp;
	private boolean skip;
	
	public MessageXp( float xp, int skill, float gainedXp, boolean skip )
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
		packet.xp = buf.readFloat();
		packet.gainedXp = buf.readFloat();
		packet.skill = buf.readInt();
		packet.skip = buf.readBoolean();

		return packet;
	}

	public static void encode( MessageXp packet, PacketBuffer buf )
	{
		buf.writeFloat( packet.xp );
		buf.writeFloat( packet.gainedXp );
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
				XPOverlayGUI.makeXpDrop( packet.xp, packet.skill, 10000, packet.gainedXp, packet.skip );
		});
		ctx.get().setPacketHandled(true);
	}
}
