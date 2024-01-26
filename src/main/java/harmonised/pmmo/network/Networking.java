package harmonised.pmmo.network;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.readers.CoreLoader;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.clientpackets.CP_ClearData;
import harmonised.pmmo.network.clientpackets.CP_ResetXP;
import harmonised.pmmo.network.clientpackets.CP_SetOtherExperience;
import harmonised.pmmo.network.clientpackets.CP_SyncData;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import harmonised.pmmo.network.clientpackets.CP_SyncVein;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.serverpackets.SP_OtherExpRequest;
import harmonised.pmmo.network.serverpackets.SP_SetVeinLimit;
import harmonised.pmmo.network.serverpackets.SP_SetVeinShape;
import harmonised.pmmo.network.serverpackets.SP_UpdateVeinTarget;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class Networking {
	@SubscribeEvent
	public static void registerMessages(RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(Reference.MOD_ID);

		registrar
		//CLIENT BOUND PACKETS
		.play(CP_UpdateExperience.ID, CP_UpdateExperience::new, h -> h.client(CP_UpdateExperience::handle))
		.play(CP_SyncData.ID, CP_SyncData::decode, h -> h.client(CP_SyncData::handle))
		.play(CP_SyncData_ClearXp.ID, CP_SyncData_ClearXp::new, h -> h.client(CP_SyncData_ClearXp::handle))
		.play(CP_ClearData.ID, CP_ClearData::new, h -> h.client(CP_ClearData::handle))
		.play(CP_SetOtherExperience.ID, CP_SetOtherExperience::new, h -> h.client(CP_SetOtherExperience::handle))
		.play(CP_ResetXP.ID, CP_ResetXP::new, h -> h.client(CP_ResetXP::handle))
		.play(CP_SyncVein.ID, CP_SyncVein::new, h -> h.client(CP_SyncVein::handle))
		//SERVER BOUND PACKETS
		.play(SP_UpdateVeinTarget.ID, SP_UpdateVeinTarget::new, h -> h.server(SP_UpdateVeinTarget::handle))
		.play(SP_OtherExpRequest.ID, SP_OtherExpRequest::new, h -> h.server(SP_OtherExpRequest::handle))
		.play(SP_SetVeinLimit.ID, SP_SetVeinLimit::new, h -> h.server(SP_SetVeinLimit::handle))
		.play(SP_SetVeinShape.ID, SP_SetVeinShape::new, h -> h.server(SP_SetVeinShape::handle));
		MsLoggy.INFO.log(LOG_CODE.NETWORK, "Messages Registered");
	}
	
	public static void registerDataSyncPackets() {
		CoreLoader.RELOADER.subscribeAsSyncable(CP_ClearData::new);
		Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER.subscribeAsSyncable((o) -> new CP_SyncData(ObjectType.ITEM, o));
		Core.get(LogicalSide.SERVER).getLoader().BLOCK_LOADER.subscribeAsSyncable((o) -> new CP_SyncData(ObjectType.BLOCK, o));
		Core.get(LogicalSide.SERVER).getLoader().ENTITY_LOADER.subscribeAsSyncable((o) -> new CP_SyncData(ObjectType.ENTITY, o));
		Core.get(LogicalSide.SERVER).getLoader().BIOME_LOADER.subscribeAsSyncable((o) -> new CP_SyncData(ObjectType.BIOME, o));
		Core.get(LogicalSide.SERVER).getLoader().DIMENSION_LOADER.subscribeAsSyncable((o) -> new CP_SyncData(ObjectType.DIMENSION, o));
		Core.get(LogicalSide.SERVER).getLoader().ENCHANTMENT_LOADER.subscribeAsSyncable(o -> new CP_SyncData(ObjectType.ENCHANTMENT, o));
		Core.get(LogicalSide.SERVER).getLoader().EFFECT_LOADER.subscribeAsSyncable(o -> new CP_SyncData(ObjectType.EFFECT, o));
		Core.get(LogicalSide.SERVER).getLoader().PLAYER_LOADER.subscribeAsSyncable(o -> new CP_SyncData(ObjectType.PLAYER, o));
	}

	public static void sendToClient(CustomPacketPayload packet, ServerPlayer player) {
		PacketDistributor.PLAYER.with(player).send(packet);
	}
	public static void sendToServer(CustomPacketPayload packet) {
		PacketDistributor.SERVER.with(null).send(packet);
	}

}
