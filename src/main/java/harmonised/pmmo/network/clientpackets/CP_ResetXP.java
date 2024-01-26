package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;


public record CP_ResetXP() implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "s2c_reset_xp");
	@Override public ResourceLocation id() {return ID;}
	@Override public void write(FriendlyByteBuf buf) {}
	public CP_ResetXP(FriendlyByteBuf buf) {this();}
	public static void handle(CP_ResetXP packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(null, new HashMap<>());
		});
	}
}
