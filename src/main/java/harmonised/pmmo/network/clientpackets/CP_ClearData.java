package harmonised.pmmo.network.clientpackets;


import harmonised.pmmo.client.utils.ClientUtils;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CP_ClearData() implements CustomPacketPayload {
	public static void handle(CP_ClearData packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getLoader().resetData();
			Core.get(LogicalSide.CLIENT).getTooltipRegistry().clearRegistry();
			ClientUtils.glossary = null;
		});
	}

	public static final Type<CP_ClearData> TYPE = new Type<>(Reference.rl("s2c_clear_data"));
	@Override
	public Type<? extends CustomPacketPayload> type() {return TYPE;}
}
