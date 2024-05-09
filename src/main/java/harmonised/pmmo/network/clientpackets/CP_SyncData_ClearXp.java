package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public record CP_SyncData_ClearXp() implements CustomPacketPayload {
	public static final Type<CP_SyncData_ClearXp> TYPE = new Type<>(new ResourceLocation(Reference.MOD_ID, "s2c_syncdata_clear_xp"));
	public static void handle(CP_SyncData_ClearXp packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(null, new HashMap<>());
		});
	}
	@Override
	public Type<CP_SyncData_ClearXp> type() {return TYPE;}
}
