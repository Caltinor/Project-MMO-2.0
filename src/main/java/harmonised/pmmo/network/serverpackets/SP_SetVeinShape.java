package harmonised.pmmo.network.serverpackets;

import java.util.function.Supplier;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.features.veinmining.VeinShapeData.ShapeType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record SP_SetVeinShape(ShapeType mode) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "c2s_set_vein_shape");
	@Override public ResourceLocation id() {return ID;}
	public SP_SetVeinShape(FriendlyByteBuf buf) {this(buf.readEnum(ShapeType.class));}
	@Override
	public void write(FriendlyByteBuf buf) {buf.writeEnum(mode);}
	public static void handle(SP_SetVeinShape packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			VeinMiningLogic.shapePerPlayer.put(ctx.player().get().getUUID(), packet.mode());
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Shape Updated for: {} with {}", ctx.player().get().getScoreboardName(), packet.mode().name());
		});
	}
}
