package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import harmonised.pmmo.config.codecs.CodecMapPlayer;
import harmonised.pmmo.config.codecs.CodecMapPlayer.PlayerData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_Players {
	private final Map<ResourceLocation, PlayerData> data;
	
	private static final Codec<Map<ResourceLocation, PlayerData>> MAPPER =
			Codec.unboundedMap(ResourceLocation.CODEC, CodecMapPlayer.PlayerData.CODEC);
	
	public CP_SyncData_Players(Map<ResourceLocation, PlayerData> data) {this.data = data;}
	public static CP_SyncData_Players decode(FriendlyByteBuf buf) {
		return new CP_SyncData_Players(MAPPER.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(new HashMap<>()));
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, data).result().orElse(new CompoundTag())));
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			data.forEach((rl, pd) -> {
				MsLoggy.INFO.log(LOG_CODE.DATA, "PLAYER: ID:"+rl.getPath()+pd.toString());
				Core.get(LogicalSide.CLIENT).getDataConfig().setPlayerSpecificData(UUID.fromString(rl.getPath()), pd);
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
