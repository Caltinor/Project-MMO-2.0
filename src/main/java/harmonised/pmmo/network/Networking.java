package harmonised.pmmo.network;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.readers.CoreLoader;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.clientpackets.CP_ClearData;
import harmonised.pmmo.network.clientpackets.CP_ResetXP;
import harmonised.pmmo.network.clientpackets.CP_SetOtherExperience;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Enhancements;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Locations;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Objects;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Players;
import harmonised.pmmo.network.clientpackets.CP_SyncVein;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.network.serverpackets.SP_OtherExpRequest;
import harmonised.pmmo.network.serverpackets.SP_SetVeinLimit;
import harmonised.pmmo.network.serverpackets.SP_SetVeinShape;
import harmonised.pmmo.network.serverpackets.SP_UpdateVeinTarget;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {
	private static SimpleChannel INSTANCE;

	public static void registerMessages() { 
		INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Reference.MOD_ID, "net"),
				() -> "1.1", 
				s -> true, 
				s -> true);
		
		int ID = 0;
		//CLIENT BOUND PACKETS
		INSTANCE.messageBuilder(CP_UpdateLevelCache.class, ID++)
			.encoder(CP_UpdateLevelCache::toBytes)
			.decoder(CP_UpdateLevelCache::new)
			.consumerNetworkThread(CP_UpdateLevelCache::handle)
			.add();
		INSTANCE.messageBuilder(CP_UpdateExperience.class, ID++)
			.encoder(CP_UpdateExperience::toBytes)
			.decoder(CP_UpdateExperience::new)
			.consumerNetworkThread(CP_UpdateExperience::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Objects.class, ID++)
			.encoder(CP_SyncData_Objects::encode)
			.decoder(CP_SyncData_Objects::decode)
			.consumerNetworkThread(CP_SyncData_Objects::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Locations.class, ID++)
			.encoder(CP_SyncData_Locations::encode)
			.decoder(CP_SyncData_Locations::decode)
			.consumerNetworkThread(CP_SyncData_Locations::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Enhancements.class, ID++)
			.encoder(CP_SyncData_Enhancements::encode)
			.decoder(CP_SyncData_Enhancements::decode)
			.consumerNetworkThread(CP_SyncData_Enhancements::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Players.class, ID++)
			.encoder(CP_SyncData_Players::encode)
			.decoder(CP_SyncData_Players::decode)
			.consumerNetworkThread(CP_SyncData_Players::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_ClearXp.class, ID++)
			.encoder((packet, buf) -> {})
			.decoder(buf -> new CP_SyncData_ClearXp())
			.consumerNetworkThread(CP_SyncData_ClearXp::handle)
			.add();
		INSTANCE.messageBuilder(CP_ClearData.class, ID++)
			.encoder((packet, buf) -> {})
			.decoder(buf -> new CP_ClearData())
			.consumerNetworkThread(CP_ClearData::handle)
			.add();
		INSTANCE.messageBuilder(CP_SetOtherExperience.class, ID++)
			.encoder(CP_SetOtherExperience::toBytes)
			.decoder(CP_SetOtherExperience::new)
			.consumerNetworkThread(CP_SetOtherExperience::handle)
			.add();
		INSTANCE.messageBuilder(CP_ResetXP.class, ID++)
			.encoder((packet, buf) -> {})
			.decoder(buf -> new CP_ResetXP())
			.consumerNetworkThread(CP_ResetXP::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncVein.class, ID++)
			.encoder(CP_SyncVein::encode)
			.decoder(CP_SyncVein::new)
			.consumerNetworkThread(CP_SyncVein::handle)
			.add();
		//SERVER BOUND PACKETS
		INSTANCE.messageBuilder(SP_UpdateVeinTarget.class, ID++)
			.encoder(SP_UpdateVeinTarget::toBytes)
			.decoder(SP_UpdateVeinTarget::new)
			.consumerNetworkThread(SP_UpdateVeinTarget::handle)
			.add();
		INSTANCE.messageBuilder(SP_OtherExpRequest.class, ID++)
			.encoder(SP_OtherExpRequest::toBytes)
			.decoder(SP_OtherExpRequest::new)
			.consumerNetworkThread(SP_OtherExpRequest::handle)
			.add();
		INSTANCE.messageBuilder(SP_SetVeinLimit.class, ID++)
			.encoder(SP_SetVeinLimit::encode)
			.decoder(SP_SetVeinLimit::new)
			.consumerNetworkThread(SP_SetVeinLimit::handle)
			.add();
		INSTANCE.messageBuilder(SP_SetVeinShape.class, ID++)
			.encoder(SP_SetVeinShape::encode)
			.decoder(SP_SetVeinShape::new)
			.consumerNetworkThread(SP_SetVeinShape::handle)
			.add();
		MsLoggy.INFO.log(LOG_CODE.NETWORK, "Messages Registered");
	}
	
	public static void registerDataSyncPackets() {
		CoreLoader.RELOADER.subscribeAsSyncable(INSTANCE, () -> new CP_ClearData());
		Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(ObjectType.ITEM, o));
		Core.get(LogicalSide.SERVER).getLoader().BLOCK_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(ObjectType.BLOCK, o));
		Core.get(LogicalSide.SERVER).getLoader().ENTITY_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(ObjectType.ENTITY, o));
		Core.get(LogicalSide.SERVER).getLoader().BIOME_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Locations(ObjectType.BIOME, o));
		Core.get(LogicalSide.SERVER).getLoader().DIMENSION_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Locations(ObjectType.DIMENSION, o));
		Core.get(LogicalSide.SERVER).getLoader().ENCHANTMENT_LOADER.subscribeAsSyncable(INSTANCE, o -> new CP_SyncData_Enhancements(ObjectType.ENCHANTMENT, o));
		Core.get(LogicalSide.SERVER).getLoader().EFFECT_LOADER.subscribeAsSyncable(INSTANCE, o -> new CP_SyncData_Enhancements(ObjectType.EFFECT, o));
		Core.get(LogicalSide.SERVER).getLoader().PLAYER_LOADER.subscribeAsSyncable(INSTANCE, CP_SyncData_Players::new);
	}

	public static void sendToClient(Object packet, ServerPlayer player) {
		INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	public static void sendToServer(Object packet) {
		INSTANCE.sendToServer(packet);
	}

}
