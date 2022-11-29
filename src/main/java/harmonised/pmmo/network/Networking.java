package harmonised.pmmo.network;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.readers.CoreParser;
import harmonised.pmmo.core.NBTUtils;
import harmonised.pmmo.network.clientpackets.CP_ApplyConfigRegistry;
import harmonised.pmmo.network.clientpackets.CP_ClearData;
import harmonised.pmmo.network.clientpackets.CP_RegisterNBT;
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
import harmonised.pmmo.registry.ConfigurationRegistry;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
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
			.consumer(CP_UpdateLevelCache::handle)
			.add();
		INSTANCE.messageBuilder(CP_UpdateExperience.class, ID++)
			.encoder(CP_UpdateExperience::toBytes)
			.decoder(CP_UpdateExperience::new)
			.consumer(CP_UpdateExperience::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Objects.class, ID++)
			.encoder(CP_SyncData_Objects::encode)
			.decoder(CP_SyncData_Objects::decode)
			.consumer(CP_SyncData_Objects::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Locations.class, ID++)
			.encoder(CP_SyncData_Locations::encode)
			.decoder(CP_SyncData_Locations::decode)
			.consumer(CP_SyncData_Locations::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Enhancements.class, ID++)
			.encoder(CP_SyncData_Enhancements::encode)
			.decoder(CP_SyncData_Enhancements::decode)
			.consumer(CP_SyncData_Enhancements::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_Players.class, ID++)
			.encoder(CP_SyncData_Players::encode)
			.decoder(CP_SyncData_Players::decode)
			.consumer(CP_SyncData_Players::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncData_ClearXp.class, ID++)
			.encoder((packet, buf) -> {})
			.decoder(buf -> new CP_SyncData_ClearXp())
			.consumer(CP_SyncData_ClearXp::handle)
			.add();
		INSTANCE.messageBuilder(CP_ClearData.class, ID++)
			.encoder((packet, buf) -> {})
			.decoder(buf -> new CP_ClearData())
			.consumer(CP_ClearData::handle)
			.add();
		INSTANCE.messageBuilder(CP_SetOtherExperience.class, ID++)
			.encoder(CP_SetOtherExperience::toBytes)
			.decoder(CP_SetOtherExperience::new)
			.consumer(CP_SetOtherExperience::handle)
			.add();
		INSTANCE.messageBuilder(CP_ResetXP.class, ID++)
			.encoder((packet, buf) -> {})
			.decoder(buf -> new CP_ResetXP())
			.consumer(CP_ResetXP::handle)
			.add();
		INSTANCE.messageBuilder(CP_SyncVein.class, ID++)
			.encoder(CP_SyncVein::encode)
			.decoder(CP_SyncVein::new)
			.consumer(CP_SyncVein::handle)
			.add();
		INSTANCE.messageBuilder(CP_ApplyConfigRegistry.class, ID++)
			.encoder(CP_ApplyConfigRegistry::encode)
			.decoder(CP_ApplyConfigRegistry::new)
			.consumer(CP_ApplyConfigRegistry::handle)
			.add();
		INSTANCE.messageBuilder(CP_RegisterNBT.class, ID++)
			.encoder((packet, buf) -> {})
			.decoder(buf -> new CP_RegisterNBT())
			.consumer(CP_RegisterNBT::handle)
			.add();
		//SERVER BOUND PACKETS
		INSTANCE.messageBuilder(SP_UpdateVeinTarget.class, ID++)
			.encoder(SP_UpdateVeinTarget::toBytes)
			.decoder(SP_UpdateVeinTarget::new)
			.consumer(SP_UpdateVeinTarget::handle)
			.add();
		INSTANCE.messageBuilder(SP_OtherExpRequest.class, ID++)
			.encoder(SP_OtherExpRequest::toBytes)
			.decoder(SP_OtherExpRequest::new)
			.consumer(SP_OtherExpRequest::handle)
			.add();
		INSTANCE.messageBuilder(SP_SetVeinLimit.class, ID++)
			.encoder(SP_SetVeinLimit::encode)
			.decoder(SP_SetVeinLimit::new)
			.consumer(SP_SetVeinLimit::handle)
			.add();
		INSTANCE.messageBuilder(SP_SetVeinShape.class, ID++)
			.encoder(SP_SetVeinShape::encode)
			.decoder(SP_SetVeinShape::new)
			.consumer(SP_SetVeinShape::handle)
			.add();
		MsLoggy.INFO.log(LOG_CODE.NETWORK, "Messages Registered");
	}
	
	public static void registerDataSyncPackets() {
		CoreParser.RELOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_ClearData());
		ConfigurationRegistry.addSyncPacket(INSTANCE, false);
		CoreParser.ITEM_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.ITEM, o)));
		CoreParser.BLOCK_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.BLOCK, o)));
		CoreParser.ENTITY_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.ENTITY, o)));
		CoreParser.BIOME_LOADER.subscribeAsSyncable(INSTANCE, CP_SyncData_Locations::new);
		CoreParser.DIMENSION_LOADER.subscribeAsSyncable(INSTANCE, CP_SyncData_Locations::new);
		CoreParser.ENCHANTMENT_LOADER.subscribeAsSyncable(INSTANCE, o -> new CP_SyncData_Enhancements(true, o));
		CoreParser.EFFECT_LOADER.subscribeAsSyncable(INSTANCE, o -> new CP_SyncData_Enhancements(false, o));
		CoreParser.PLAYER_LOADER.subscribeAsSyncable(INSTANCE, CP_SyncData_Players::new);
		ConfigurationRegistry.addSyncPacket(INSTANCE, true);
		MinecraftForge.EVENT_BUS.addListener(NBTUtils.onDataReload(INSTANCE));
	}

	public static void sendToClient(Object packet, ServerPlayer player) {
		INSTANCE.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	public static void sendToServer(Object packet) {
		INSTANCE.sendToServer(packet);
	}

}
