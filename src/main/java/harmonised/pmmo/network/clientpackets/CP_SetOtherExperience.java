package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;

public class CP_SetOtherExperience {
	Map<String, Long> map;
	
	public CP_SetOtherExperience(Map<String, Long> map) {
		this.map = map;
	}
	public CP_SetOtherExperience(FriendlyByteBuf buf) {
		this(CodecTypes.LONG_CODEC.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(new HashMap<>()));
	}
	public void toBytes(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)CodecTypes.LONG_CODEC.encodeStart(NbtOps.INSTANCE, map).result().orElse(new CompoundTag()));
	}
	
	public void handle(NetworkEvent.Context ctx ) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(UUID.randomUUID(), map);
			MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Client Packet Handled for getting Other Experience");
		});		
		ctx.setPacketHandled(true);
	}
}
