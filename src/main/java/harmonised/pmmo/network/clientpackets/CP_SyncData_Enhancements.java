package harmonised.pmmo.network.clientpackets;

import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.codecs.EnhancementsData;
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

public record CP_SyncData_Enhancements(ObjectType type, Map<ResourceLocation, EnhancementsData> data) {
	
	private static final Codec<CP_SyncData_Enhancements> MAPPER = RecordCodecBuilder.create(instance -> instance.group(
			ObjectType.CODEC.fieldOf("type").forGetter(CP_SyncData_Enhancements::type),
			Codec.unboundedMap(ResourceLocation.CODEC, EnhancementsData.CODEC).fieldOf("data").forGetter(CP_SyncData_Enhancements::data)
			).apply(instance, CP_SyncData_Enhancements::new));
	
	public static CP_SyncData_Enhancements decode(FriendlyByteBuf buf) {
		return MAPPER.parse(NbtOps.INSTANCE, buf.readNbt(NbtAccounter.UNLIMITED)).result().get();
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, this).result().orElse(new CompoundTag())));
		MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Payload for {}/{} is {}", this.getClass().getSimpleName(), this.type().name(), buf.readableBytes());
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getLoader().applyData(this.type(), this.data());
		});
		ctx.get().setPacketHandled(true);
	}
}
