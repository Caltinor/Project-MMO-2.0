package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CP_SyncConfig(ConfigListener.ServerConfigs oType, ConfigData<?> config) implements CustomPacketPayload {
    public static final Type<CP_SyncConfig> TYPE = new Type(Reference.rl("s2c_sync_config"));
    @Override public Type<CP_SyncConfig> type() {return TYPE;}

    public static final StreamCodec<FriendlyByteBuf, CP_SyncConfig> STREAM_CODEC = StreamCodec.of(
            CP_SyncConfig::write, CP_SyncConfig::decode
    );
    public static CP_SyncConfig decode(FriendlyByteBuf buf) {
        ConfigListener.ServerConfigs t = buf.readEnum(ConfigListener.ServerConfigs.class);
        ConfigData<?> c = ConfigListener.ServerConfigs.MAPPER.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElseThrow();
        return new CP_SyncConfig(t, c);
    }
    public static void write(FriendlyByteBuf buf, CP_SyncConfig packet) {
        buf.writeEnum(packet.oType());
        buf.writeNbt(ConfigListener.ServerConfigs.MAPPER.encodeStart(NbtOps.INSTANCE, packet.config()).result().orElse(new CompoundTag()));}


    public static void handle(CP_SyncConfig packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Config.CONFIG.setData(packet.oType(), packet.config());
            MsLoggy.INFO.log(MsLoggy.LOG_CODE.DATA, "Config packet received on client for {}", packet.oType().getSerializedName());
        });
    }
}
