package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CP_SyncData_ClearXp() implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "s2c_syncdata_clear_xp");

	public CP_SyncData_ClearXp(FriendlyByteBuf buf) {this();}
	@Override
	public void write(FriendlyByteBuf buf) {}

	@Override
	public ResourceLocation id() {return ID;}
	public static void handle(CP_SyncData_ClearXp packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(null, new HashMap<>());
		});
	}
}
