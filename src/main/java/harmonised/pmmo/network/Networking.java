package harmonised.pmmo.network;

import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {
	private static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Reference.MOD_ID, "net"),
		() -> "1.0", 
		s -> true, 
		s -> true);

	public static void registerMessages() { 
		int ID = 0;
		//CLIENT BOUND PACKETS
		INSTANCE.messageBuilder(CP_UpdateLevelCache.class, ID++)
			.encoder(CP_UpdateLevelCache::toBytes)
			.decoder(CP_UpdateLevelCache::new)
			.consumer(CP_UpdateLevelCache::handle)
			.add();
		INSTANCE.messageBuilder(CP_UpdateExperience.class, ID++)
			.encoder(CP_UpdateExperience::toBytes)
			.decoder(CP_UpdateExperience::new)
			.consumer(CP_UpdateExperience::handle)
			.add();
		//SERVER BOUND PACKETS
	}

	public static void sendToClient(Object packet, ServerPlayer player) {
		INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	public static void sendToServer(Object packet) {
		INSTANCE.sendToServer(packet);
	}
}
