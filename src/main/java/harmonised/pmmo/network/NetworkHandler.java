package harmonised.pmmo.network;

import harmonised.pmmo.ProjectMMOMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Items;
import net.minecraftforge.fml.network.NetworkDirection;

public class NetworkHandler
{
	public static void registerChannel()
	{
//		HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(Reference.MOD_ID, "main_channel" ), () -> "1.0", s -> true, s -> true);
	}

	public static void registerPackets()
	{
		int index = 0;
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageXp.class, MessageXp::encode, MessageXp::decode, MessageXp::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageCrawling.class, MessageCrawling::encode, MessageCrawling::decode, MessageCrawling::handlePacket );
		ProjectMMOMod.HANDLER.registerMessage( index++, MessageDoubleTranslation.class, MessageDoubleTranslation::encode, MessageDoubleTranslation::decode, MessageDoubleTranslation::handlePacket );
	}

	public static void sendToPlayer( MessageXp packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToPlayer( MessageDoubleTranslation packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}

	public static void sendToServer( MessageCrawling packet )
	{
		ProjectMMOMod.HANDLER.sendToServer( packet );
	}
}