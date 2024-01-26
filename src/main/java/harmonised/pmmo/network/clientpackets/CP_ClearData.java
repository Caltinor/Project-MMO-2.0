package harmonised.pmmo.network.clientpackets;


import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CP_ClearData() implements CustomPacketPayload {
	public CP_ClearData(FriendlyByteBuf buf) {this();}
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "s2c_clear_data");
	@Override
	public ResourceLocation id() {return ID;}
	@Override
	public void write(FriendlyByteBuf buf) {}

	public static void handle(CP_ClearData packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			Core.get(LogicalSide.CLIENT).resetDataForReload();
			Core.get(LogicalSide.CLIENT).getLoader().resetData();
			Core.get(LogicalSide.CLIENT).getTooltipRegistry().clearRegistry();
		});
	}
}
