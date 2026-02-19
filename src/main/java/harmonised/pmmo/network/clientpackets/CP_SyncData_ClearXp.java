package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public record CP_SyncData_ClearXp(String skill) implements CustomPacketPayload {
	public static final Type<CP_SyncData_ClearXp> TYPE = new Type<>(Reference.rl("s2c_syncdata_clear_xp"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CP_SyncData_ClearXp> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, CP_SyncData_ClearXp::skill,
			CP_SyncData_ClearXp::new
	);
	public static void handle(CP_SyncData_ClearXp packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			var data = Core.get(LogicalSide.CLIENT).getData();
			if (packet.skill.isEmpty())
				data.setXpMap(null, new HashMap<>());
			else
				data.getXpMap(null).remove(packet.skill);
		});
	}
	@Override
	public Type<CP_SyncData_ClearXp> type() {return TYPE;}
}
