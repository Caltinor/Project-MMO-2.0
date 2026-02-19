package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;


public record CP_ResetXP() implements CustomPacketPayload {
	public static final Type<CP_ResetXP> TYPE = new Type(Reference.rl("s2c_reset_xp"));
	@Override public Type<CP_ResetXP> type() {return TYPE;}
	public static void handle(CP_ResetXP packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(null, new HashMap<>());
		});
	}
}
