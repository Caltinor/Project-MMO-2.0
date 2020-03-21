package harmonised.pmmo.network;

import harmonised.pmmo.gui.XPOverlayGUI;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageXp extends MessageBase<MessageXp>
{
	private float xp;
	private String skill;
	private float gainedXp;
	private boolean skip;
	
	public MessageXp( float xp, String skill, float gainedXp, boolean skip )
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
		skill = ByteBufUtils.readUTF8String( buf );
		skip = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeFloat( xp );
		buf.writeFloat( gainedXp );
		ByteBufUtils.writeUTF8String( buf,  skill );
		buf.writeBoolean( skip );
	}

	@Override
	public void handleClientSide(MessageXp message, EntityPlayer onlinePlayer)
	{
		if( message.skill.equals( "CLEAR" ) )
			XPOverlayGUI.clearXP();
		else
			XPOverlayGUI.makeXpDrop( message.xp, message.skill, 10000, message.gainedXp, message.skip );
	}

	@Override
	public void handleServerSide(MessageXp message, EntityPlayer player)
	{
//		player.sendStatusMessage( new TextComponentString( "SERVER" ), false );
//		System.out.println( "SERVER RECEIVED PACKET" );
//		NetworkHandler.sendToPlayer( new MessageXp( 500.0f, "mining" ), player);
	}

}
