package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;

public class CP_SetOtherExperience {
	Map<String, Experience> map;
	
	public CP_SetOtherExperience(Map<String, Experience> map) {
		this.map = map;
	}
	public CP_SetOtherExperience(FriendlyByteBuf buf) {
		this(Codec.unboundedMap(Codec.STRING, Experience.CODEC)
				.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(new HashMap<>()));
	}
	public void toBytes(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)Codec.unboundedMap(Codec.STRING, Experience.CODEC)
				.encodeStart(NbtOps.INSTANCE, map).result().orElse(new CompoundTag()));
	}
	
	public void handle(NetworkEvent.Context ctx ) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(UUID.randomUUID(), map);
			MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Client Packet Handled for getting Other Experience");
		});		
		ctx.setPacketHandled(true);
	}
}
