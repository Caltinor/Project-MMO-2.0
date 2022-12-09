package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.core.Core;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public record CP_SyncData_Players(Map<ResourceLocation, PlayerData> data) {
	
	private static final Codec<Map<ResourceLocation, PlayerData>> MAPPER =
			Codec.unboundedMap(ResourceLocation.CODEC, PlayerData.CODEC);
	
	public static CP_SyncData_Players decode(FriendlyByteBuf buf) {
		return new CP_SyncData_Players(MAPPER.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(new HashMap<>()));
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, data).result().orElse(new CompoundTag())));
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getLoader().applyData(ObjectType.PLAYER, this.data());
		});
		ctx.get().setPacketHandled(true);
	}
}
