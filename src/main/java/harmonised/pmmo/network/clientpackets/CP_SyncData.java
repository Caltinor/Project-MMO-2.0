package harmonised.pmmo.network.clientpackets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record CP_SyncData(ObjectType oType, Map<Identifier, ? extends DataSource<?>> data) implements CustomPacketPayload {
	public static final Type<CP_SyncData> TYPE = new Type<>(Reference.rl("s2c_sync_data"));
	public static final StreamCodec<FriendlyByteBuf, CP_SyncData> STREAM_CODEC = StreamCodec.of(CP_SyncData::write, CP_SyncData::decode);
	private static final Codec<DataSource<?>> CODEC = Codec.lazyInitialized(() -> ObjectType.CODEC.dispatch("type",
			s -> switch (s){
				case ObjectData data -> ObjectType.ITEM;
				case LocationData data -> ObjectType.BIOME;
				case EnhancementsData data -> ObjectType.EFFECT;
				default -> ObjectType.PLAYER;
			}, x -> switch (x) {
				case ITEM -> ObjectData.CODEC;
				case BIOME -> LocationData.CODEC;
				case EFFECT -> EnhancementsData.CODEC;
				default -> PlayerData.CODEC;
			}));
	
	@SuppressWarnings("unchecked")
	private static final Codec<CP_SyncData> MAPPER = RecordCodecBuilder.create(instance -> instance.group(
			ObjectType.CODEC.fieldOf("type").forGetter(CP_SyncData::oType),
			Codec.unboundedMap(Identifier.CODEC, CODEC).fieldOf("data").forGetter(pkt -> (Map<Identifier, DataSource<?>>)pkt.data())
			).apply(instance, CP_SyncData::new));
	
	public static CP_SyncData decode(FriendlyByteBuf buf) {
		return MAPPER.parse(NbtOps.INSTANCE, buf.readNbt(NbtAccounter.unlimitedHeap())).result().orElse(new CP_SyncData(ObjectType.ITEM, new HashMap<>()));
	}
	public static void write(FriendlyByteBuf buf, CP_SyncData packet) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, packet).result().orElse(new CompoundTag())));
		MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Payload for {}/{} is {}", packet.getClass().getSimpleName(), packet.oType().name(), buf.readableBytes());
	}
	
	public static void handle(CP_SyncData packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			@SuppressWarnings("unchecked")
			Map<Identifier, DataSource<?>> map = (Map<Identifier, DataSource<?>>) Core.get(LogicalSide.CLIENT).getLoader().getLoader(packet.oType()).getData();
			map.putAll(packet.data());
		});
	}

	@Override
	public Type<CP_SyncData> type() {return TYPE;}
}
