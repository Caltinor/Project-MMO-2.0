package harmonised.pmmo.network;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.clientpackets.CP_ClearData;
import harmonised.pmmo.network.clientpackets.CP_ResetXP;
import harmonised.pmmo.network.clientpackets.CP_SetOtherExperience;
import harmonised.pmmo.network.clientpackets.CP_SyncConfig;
import harmonised.pmmo.network.clientpackets.CP_SyncData;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.serverpackets.SP_OtherExpRequest;
import harmonised.pmmo.network.serverpackets.SP_SetVeinLimit;
import harmonised.pmmo.network.serverpackets.SP_SetVeinShape;
import harmonised.pmmo.network.serverpackets.SP_ToggleBreakSpeed;
import harmonised.pmmo.network.serverpackets.SP_UpdateVeinTarget;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Networking {
	@SubscribeEvent
	public static void registerMessages(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(Reference.MOD_ID);

		registrar
		//CLIENT BOUND PACKETS
		.playToClient(CP_UpdateExperience.TYPE, CP_UpdateExperience.CODEC, CP_UpdateExperience::handle)
		.playToClient(CP_SyncData.TYPE, CP_SyncData.STREAM_CODEC, CP_SyncData::handle)
		.playToClient(CP_SyncData_ClearXp.TYPE, CP_SyncData_ClearXp.CODEC, CP_SyncData_ClearXp::handle)
		.playToClient(CP_ClearData.TYPE, StreamCodec.unit(new CP_ClearData()), CP_ClearData::handle)
		.playToClient(CP_SetOtherExperience.TYPE, CP_SetOtherExperience.STREAM_CODEC, CP_SetOtherExperience::handle)
		.playToClient(CP_ResetXP.TYPE, StreamCodec.unit(new CP_ResetXP()), CP_ResetXP::handle)
		.playToClient(CP_SyncConfig.TYPE, CP_SyncConfig.STREAM_CODEC, CP_SyncConfig::handle)
		//SERVER BOUND PACKETS
		.playToServer(SP_UpdateVeinTarget.TYPE, SP_UpdateVeinTarget.STREAM_CODEC, SP_UpdateVeinTarget::handle)
		.playToServer(SP_OtherExpRequest.TYPE, SP_OtherExpRequest.STREAM_CODEC, SP_OtherExpRequest::handle)
		.playToServer(SP_SetVeinLimit.TYPE, SP_SetVeinLimit.STREAM_CODEC, SP_SetVeinLimit::handle)
		.playToServer(SP_SetVeinShape.TYPE, SP_SetVeinShape.STREAM_CODEC, SP_SetVeinShape::handle)
		.playToServer(SP_ToggleBreakSpeed.TYPE, SP_ToggleBreakSpeed.STREAM_CODEC, SP_ToggleBreakSpeed::handle);
		MsLoggy.INFO.log(LOG_CODE.NETWORK, "Messages Registered");
	}
	
	public static void registerDataSyncPackets() {
		Core.get(LogicalSide.SERVER).getLoader().RELOADER.subscribeAsSyncable(CP_ClearData::new);
		Config.CONFIG.subscribeAsSyncable();
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
		PacketDistributor.sendToPlayer(player, packet);
	}
	public static void sendToServer(CustomPacketPayload packet) {
		PacketDistributor.sendToServer(packet);
	}

}
