package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_Enchantments {
	private final Map<ResourceLocation, Map<Integer, Map<String, Integer>>> data;
	
	private static final Codec<Map<ResourceLocation, Map<Integer, Map<String, Integer>>>> MAPPER =
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.unboundedMap(Codec.INT, CodecTypes.INTEGER_CODEC));
	
	public CP_SyncData_Enchantments(Map<ResourceLocation, Map<Integer, Map<String, Integer>>> data) {this.data = data;}
	public static CP_SyncData_Enchantments decode(FriendlyByteBuf buf) {
		return new CP_SyncData_Enchantments(MAPPER.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(new HashMap<>()));
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, data).result().orElse(new CompoundTag())));
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			data.forEach((rl, map) -> {
				map.forEach((k, v) -> MsLoggy.info("ENCHANTMENT:"+rl.toString()+" Level:"+k+MsLoggy.mapToString(v)));
				Core.get(LogicalSide.CLIENT).getSkillGates().setEnchantmentReqs(rl, map);
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
