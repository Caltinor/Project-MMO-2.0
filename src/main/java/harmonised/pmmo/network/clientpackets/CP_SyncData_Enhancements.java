package harmonised.pmmo.network.clientpackets;

import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_Enhancements {
	private final boolean isEnchants;
	private final Map<ResourceLocation, Map<Integer, Map<String, Integer>>> data;
	
	private static final Codec<CP_SyncData_Enhancements> MAPPER = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("isEnchants").forGetter(pkt -> pkt.isEnchants),
			Codec.unboundedMap(ResourceLocation.CODEC, 
					Codec.unboundedMap(Codec.STRING.xmap(a -> Integer.valueOf(a), b -> String.valueOf(b)), CodecTypes.INTEGER_CODEC)).fieldOf("data").forGetter(pkt -> pkt.data)
			).apply(instance, CP_SyncData_Enhancements::new));
	
	public CP_SyncData_Enhancements(boolean isEnchants, Map<ResourceLocation, Map<Integer, Map<String, Integer>>> data) {this.isEnchants = isEnchants; this.data = data;}
	public static CP_SyncData_Enhancements decode(FriendlyByteBuf buf) {
		return MAPPER.parse(NbtOps.INSTANCE, buf.readNbt(NbtAccounter.UNLIMITED)).result().get();
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, this).result().orElse(new CompoundTag())));
		MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Payload for {} is {}", this.getClass().getSimpleName(), buf.readableBytes());
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			data.forEach((rl, map) -> {
				map.forEach((k, v) -> MsLoggy.INFO.log(LOG_CODE.DATA, (isEnchants ? "ENCHANTMENT:" : "EFFECT:")+rl.toString()+" Level:"+k+MsLoggy.mapToString(v)));
				if (isEnchants)
					Core.get(LogicalSide.CLIENT).getSkillGates().setEnchantmentReqs(rl, map);
				else
					Core.get(LogicalSide.CLIENT).getXpUtils().setEffectMap(rl, map);
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
