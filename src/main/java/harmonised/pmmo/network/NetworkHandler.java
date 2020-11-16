package harmonised.pmmo.network;

import harmonised.pmmo.ProjectMMOMod;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.network.NetworkDirection;

public class NetworkHandler
{
	public static void registerPackets()
	{
		int index = 0;
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageXp.class, MessageXp::encode, MessageXp::decode, MessageXp::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageKeypress.class, MessageKeypress::encode, MessageKeypress::decode, MessageKeypress::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageDoubleTranslation.class, MessageDoubleTranslation::encode, MessageDoubleTranslation::decode, MessageDoubleTranslation::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageTripleTranslation.class, MessageTripleTranslation::encode, MessageTripleTranslation::decode, MessageTripleTranslation::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageUpdatePlayerNBT.class, MessageUpdatePlayerNBT::encode, MessageUpdatePlayerNBT::decode, MessageUpdatePlayerNBT::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageGrow.class, MessageGrow::encode, MessageGrow::decode, MessageGrow::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageLevelUp.class, MessageLevelUp::encode, MessageLevelUp::decode, MessageLevelUp::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageUpdateBoolean.class, MessageUpdateBoolean::encode, MessageUpdateBoolean::decode, MessageUpdateBoolean::handlePacket );
	}

	public static void sendToPlayer( MessageXp packet, EntityPlayerMP player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer( MessageDoubleTranslation packet, EntityPlayerMP player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer( MessageTripleTranslation packet, EntityPlayerMP player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer(MessageUpdatePlayerNBT packet, EntityPlayerMP player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToServer( MessageUpdatePlayerNBT packet )
	{
		ProjectMMOMod.HANDLER.sendToServer( packet );
	}

	public static void sendToPlayer( MessageGrow packet, EntityPlayerMP player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer( MessageUpdateBoolean packet, EntityPlayerMP player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToServer( MessageKeypress packet )
	{
		ProjectMMOMod.HANDLER.sendToServer( packet );
	}

	public static void sendToServer( MessageLevelUp packet )
	{
		ProjectMMOMod.HANDLER.sendToServer( packet );
	}
}