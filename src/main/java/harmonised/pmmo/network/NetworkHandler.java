package harmonised.pmmo.network;

import harmonised.pmmo.ProjectMMOMod;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;

public class NetworkHandler
{
	public static void registerChannel()
	{
//		HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(Reference.MOD_ID, "main_channel" ), () -> "1.0", s -> true, s -> true);
	}

	public static void registerPackets()
	{
		ProjectMMOMod.HANDLER.registerMessage( 0, MessageXp.class, MessageXp::encode, MessageXp::decode, MessageXp::handlePacket );
	}

	public static void sendToPlayer( MessageXp packet, ServerPlayerEntity player )
	{
		ProjectMMOMod.HANDLER.sendTo( packet, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT );
	}
}
