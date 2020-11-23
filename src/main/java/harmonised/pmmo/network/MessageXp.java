package harmonised.pmmo.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class MessageXp extends MessageBase<MessageXp>
{
	public double xp;
	public double gainedXp;
	public int skill;
	public boolean skip;

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

	@Override
	public void fromBytes( ByteBuf buf )
	{
		xp = buf.readDouble();
		gainedXp = buf.readDouble();
		skill = buf.readInt();
		skip = buf.readBoolean();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeDouble( xp );
		buf.writeDouble( gainedXp );
		buf.writeInt( skill );
		buf.writeBoolean( skip );
	}

	@Override
	public void handleClientSide( MessageXp packet, EntityPlayer onlinePlayer )
	{
		packetHandler.handleXpPacket( packet, onlinePlayer );
	}

	@Override
	public void handleServerSide( MessageXp packet, EntityPlayer player )
	{
//		player.sendStatusMessage( new TextComponentString( "SERVER" ), false );
//		System.out.println( "SERVER RECEIVED PACKET" );
//		NetworkHandler.sendToPlayer( new MessageXp( 500.0f, "mining" ), player);
	}
}
