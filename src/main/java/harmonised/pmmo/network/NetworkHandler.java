package harmonised.pmmo.network;

import harmonised.pmmo.ProjectMMOMod;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;

public class NetworkHandler
{
	public static void registerPackets()
	{
		int index = 0;
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageXp.class, MessageXp::encode, MessageXp::decode, MessageXp::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageCrawling.class, MessageCrawling::encode, MessageCrawling::decode, MessageCrawling::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageDoubleTranslation.class, MessageDoubleTranslation::encode, MessageDoubleTranslation::decode, MessageDoubleTranslation::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageTripleTranslation.class, MessageTripleTranslation::encode, MessageTripleTranslation::decode, MessageTripleTranslation::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageUpdateReq.class, MessageUpdateReq::encode, MessageUpdateReq::decode, MessageUpdateReq::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageUpdateNBT.class, MessageUpdateNBT::encode, MessageUpdateNBT::decode, MessageUpdateNBT::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageGrow.class, MessageGrow::encode, MessageGrow::decode, MessageGrow::handlePacket );
	}

	public static void sendToPlayer( MessageXp packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer( MessageDoubleTranslation packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer( MessageTripleTranslation packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer(MessageUpdateReq packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer(MessageUpdateNBT packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer( MessageGrow packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToServer( MessageCrawling packet )
	{
		ProjectMMOMod.HANDLER.sendToServer( packet );
	}
}