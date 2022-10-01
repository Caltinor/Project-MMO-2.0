package harmonised.pmmo.network;

import java.util.ArrayList;
import java.util.List;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.readers.CoreParser;
import harmonised.pmmo.network.clientpackets.CP_ClearData;
import harmonised.pmmo.network.clientpackets.CP_ResetXP;
import harmonised.pmmo.network.clientpackets.CP_SetOtherExperience;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Enchantments;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Locations;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Objects;
import harmonised.pmmo.network.clientpackets.CP_SyncData_Players;
import harmonised.pmmo.network.clientpackets.CP_SyncVein;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.network.serverpackets.SP_OtherExpRequest;
import harmonised.pmmo.network.serverpackets.SP_SetVeinLimit;
import harmonised.pmmo.network.serverpackets.SP_UpdateVeinTarget;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.filters.VanillaPacketSplitter;
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
		INSTANCE.messageBuilder(CP_SyncData_Enchantments.class, ID++)
			.encoder(CP_SyncData_Enchantments::encode)
			.decoder(CP_SyncData_Enchantments::decode)
			.consumerNetworkThread(CP_SyncData_Enchantments::handle)
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
		MsLoggy.INFO.log(LOG_CODE.NETWORK, "Messages Registered");
	}
	
	public static void registerDataSyncPackets() {
		CoreParser.RELOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_ClearData());
		CoreParser.ITEM_LOADER.subscribeAsSplitSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.ITEM, o)));
		CoreParser.BLOCK_LOADER.subscribeAsSplitSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.BLOCK, o)));
		CoreParser.ENTITY_LOADER.subscribeAsSplitSyncable(INSTANCE, (o) -> new CP_SyncData_Objects(new CP_SyncData_Objects.DataObjectRecord(ObjectType.ENTITY, o)));
		CoreParser.BIOME_LOADER.subscribeAsSplitSyncable(INSTANCE, (o) -> new CP_SyncData_Locations(o));
		CoreParser.DIMENSION_LOADER.subscribeAsSplitSyncable(INSTANCE, (o) -> new CP_SyncData_Locations(o));
		CoreParser.ENCHANTMENT_LOADER.subscribeAsSplitSyncable(INSTANCE, (o) -> new CP_SyncData_Enchantments(o));
		CoreParser.PLAYER_LOADER.subscribeAsSyncable(INSTANCE, (o) -> new CP_SyncData_Players(o));
	}

	public static void sendToClient(Object packet, ServerPlayer player) {
		splitToClient(packet, player);
	}
	public static void sendToServer(Object packet) {
		INSTANCE.sendToServer(packet);
	}
	
	public static void splitToClient(Object packet, ServerPlayer player) {
		//this might just be a check needed for Dev, but it's in here now.
		if (player == null) return;
		
		Packet<?> vanillaPacket = INSTANCE.toVanillaPacket(packet, NetworkDirection.PLAY_TO_CLIENT);
        List<Packet<?>> packets = new ArrayList<>();
        VanillaPacketSplitter.appendPackets(ConnectionProtocol.PLAY, PacketFlow.CLIENTBOUND, vanillaPacket, packets);
        Connection connection = player.connection.getConnection();
        packets.forEach(connection::send);
	}
}
