package harmonised.pmmo.network;

import harmonised.pmmo.ProjectMMOMod;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkDirection;
import org.apache.logging.log4j.*;

public class NetworkHandler
{
	public static final Logger LOGGER = LogManager.getLogger();

	public static void registerPackets()
	{
		int index = 0;
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageXp.class, MessageXp::encode, MessageXp::decode, MessageXp::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageWorldXp.class, MessageWorldXp::encode, MessageWorldXp::decode, MessageWorldXp::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageWorldText.class, MessageWorldText::encode, MessageWorldText::decode, MessageWorldText::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageKeypress.class, MessageKeypress::encode, MessageKeypress::decode, MessageKeypress::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageDoubleTranslation.class, MessageDoubleTranslation::encode, MessageDoubleTranslation::decode, MessageDoubleTranslation::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageTripleTranslation.class, MessageTripleTranslation::encode, MessageTripleTranslation::decode, MessageTripleTranslation::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageUpdatePlayerNBT.class, MessageUpdatePlayerNBT::encode, MessageUpdatePlayerNBT::decode, MessageUpdatePlayerNBT::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageGrow.class, MessageGrow::encode, MessageGrow::decode, MessageGrow::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageTrigger.class, MessageTrigger::encode, MessageTrigger::decode, MessageTrigger::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageLevelUp.class, MessageLevelUp::encode, MessageLevelUp::decode, MessageLevelUp::handlePacket);
		ProjectMMOMod.HANDLER.registerMessage(index++, MessageUpdateBoolean.class, MessageUpdateBoolean::encode, MessageUpdateBoolean::decode, MessageUpdateBoolean::handlePacket);
	}

	public static void sendToPlayer(MessageXp packet, ServerPlayer player)
	{
		ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToPlayer(MessageWorldXp packet, ServerPlayer player)
	{
		try
		{
			ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
		}
		catch(Exception e)
		{
			LOGGER.debug(e);
		}
	}

	public static void sendToPlayer(MessageWorldText packet, ServerPlayer player)
	{
		try
		{
			ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
		}
		catch(Exception e)
		{
			LOGGER.debug(e);
		}
	}

	public static void sendToPlayer(MessageDoubleTranslation packet, ServerPlayer player)
	{
		ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToPlayer(MessageTripleTranslation packet, ServerPlayer player)
	{
		ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToPlayer(MessageUpdatePlayerNBT packet, ServerPlayer player)
	{
		ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToServer(MessageUpdatePlayerNBT packet)
	{
		ProjectMMOMod.HANDLER.sendToServer(packet);
	}

	public static void sendToPlayer(MessageGrow packet, ServerPlayer player)
	{
		ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToPlayer(MessageTrigger packet, ServerPlayer player)
	{
		ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToPlayer(MessageUpdateBoolean packet, ServerPlayer player)
	{
		ProjectMMOMod.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToServer(MessageKeypress packet)
	{
		ProjectMMOMod.HANDLER.sendToServer(packet);
	}

	public static void sendToServer(MessageLevelUp packet)
	{
		ProjectMMOMod.HANDLER.sendToServer(packet);
	}
}