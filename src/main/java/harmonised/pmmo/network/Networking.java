package harmonised.pmmo.network;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.readers.CoreParser;
import harmonised.pmmo.network.clientpackets.CP_SyncData_DataSkills;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Objects;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {
	private static SimpleChannel INSTANCE;

	public static void registerMessages() { 
		INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Reference.MOD_ID, "net"),
				() -> "1.0", 
				s -> true, 
				s -> true);
		
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
		INSTANCE.messageBuilder(CP_SyncData_DataSkills.class, ID++)
			.encoder(CP_SyncData_DataSkills::encode)
			.decoder(CP_SyncData_DataSkills::decode)
			.consumer(CP_SyncData_DataSkills::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Objects.class, ID++)
			.encoder(CP_SyncData_Objects::encode)
			.decoder(CP_SyncData_Objects::decode)
			.consumer(CP_SyncData_Objects::handle)
			.add();
		//SERVER BOUND PACKETS
		MsLoggy.info("Messages Registered");
	}
	
	public static void registerDataSyncPackets() {
		CoreParser.ITEM_LOADER.subscribeAsSyncable(INSTANCE, (o) -> {return new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.ITEM, o));});
		CoreParser.BLOCK_LOADER.subscribeAsSyncable(INSTANCE, (o) -> {return new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.BLOCK, o));});
		CoreParser.ENTITY_LOADER.subscribeAsSyncable(INSTANCE, (o) -> {return new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.ENTITY, o));});
		//TODO BIOME and DIMENSION synchronizers
		//TODO ENCHANTMENT synchronizer
	}

	public static void sendToClient(Object packet, ServerPlayer player) {
		INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	public static void sendToServer(Object packet) {
		INSTANCE.sendToServer(packet);
	}
}
