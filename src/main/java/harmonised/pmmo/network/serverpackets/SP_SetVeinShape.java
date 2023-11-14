package harmonised.pmmo.network.serverpackets;

import java.util.function.Supplier;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.features.veinmining.VeinShapeData.ShapeType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public class SP_SetVeinShape {
	ShapeType mode;
	public SP_SetVeinShape(ShapeType mode) {this.mode = mode;}
	public SP_SetVeinShape(FriendlyByteBuf buf) {this.mode = buf.readEnum(ShapeType.class);}
	public void encode(FriendlyByteBuf buf) {buf.writeEnum(mode);}
	public void handle(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			VeinMiningLogic.shapePerPlayer.put(ctx.getSender().getUUID(), mode);
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Shape Updated for: {} with {}", ctx.getSender().getScoreboardName(), mode.name());
		});
		ctx.setPacketHandled(true);
	}
}
