package harmonised.pmmo.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageXp extends MessageBase<MessageXp>
{
	public double xp;
	public int skill;
	public double gainedXp;
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
	public void fromBytes(ByteBuf buf)
	{
		xp = buf.readFloat();
		gainedXp = buf.readFloat();
		skill = buf.readInt();
		skip = buf.readBoolean();
	}

	@Override
	public void toBytes( ByteBuf buf)
	{
		buf.writeFloat( xp );
		buf.writeFloat( gainedXp );
		buf.writeInt( skill );
		buf.writeBoolean( skip );
	}

	@Override
	public void handleClientSide( MessageXp message, EntityPlayer onlinePlayer )
	{
		packetHandler.handleXpPacket( message, onlinePlayer );
	}

	@Override
	public void handleServerSide( MessageXp message, EntityPlayer player )
	{
//		player.sendStatusMessage( new TextComponentString( "SERVER" ), false );
//		System.out.println( "SERVER RECEIVED PACKET" );
//		NetworkHandler.sendToPlayer( new MessageXp( 500.0f, "mining" ), player);
	}
}
