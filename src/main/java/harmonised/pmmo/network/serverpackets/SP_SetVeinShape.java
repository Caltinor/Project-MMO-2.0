package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.features.veinmining.VeinShapeData.ShapeType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SP_SetVeinShape(ShapeType mode) implements CustomPacketPayload {
	public static final Type<SP_SetVeinShape> TYPE = new Type(Reference.rl("c2s_set_vein_shape"));
	@Override public Type<SP_SetVeinShape> type() {return TYPE;}
	public static final StreamCodec<FriendlyByteBuf, SP_SetVeinShape> STREAM_CODEC = StreamCodec.composite(
			NeoForgeStreamCodecs.enumCodec(ShapeType.class), SP_SetVeinShape::mode, SP_SetVeinShape::new
	);
	public static void handle(SP_SetVeinShape packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			VeinMiningLogic.shapePerPlayer.put(ctx.player().getUUID(), packet.mode());
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Shape Updated for: {} with {}", ctx.player().getScoreboardName(), packet.mode().name());
		});
	}
}
