package harmonised.pmmo.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageXp extends MessageBase<MessageXp>
{
	public double xp;
	public double gainedXp;
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

	@Override
	public void fromBytes( ByteBuf buf )
	{
		xp = buf.readDouble();
		gainedXp = buf.readDouble();
		skill = ByteBufUtils.readUTF8String( buf );
		skip = buf.readBoolean();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeDouble( xp );
		buf.writeDouble( gainedXp );
		ByteBufUtils.writeUTF8String( buf, skill );
		buf.writeBoolean( skip );
	}

	@Override
	public void handleClientSide( MessageXp packet, EntityPlayer onlinePlayer )
	{
		packetHandler.handleXpPacket( packet );
	}

	@Override
	public void handleServerSide( MessageXp packet, EntityPlayer player )
	{
//		player.sendStatusMessage( new TextComponentString( "SERVER" ), false );
//		System.out.println( "SERVER RECEIVED PACKET" );
//		NetworkHandler.sendToPlayer( new MessageXp( 500.0f, "mining" ), player);
	}
}
