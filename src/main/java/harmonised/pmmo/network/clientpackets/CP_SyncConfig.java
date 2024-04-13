package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CP_SyncConfig(ConfigListener.ServerConfigs type, ConfigData<?> config) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "s2c_sync_config");

    public static CP_SyncConfig decode(FriendlyByteBuf buf) {
        ConfigListener.ServerConfigs t = buf.readEnum(ConfigListener.ServerConfigs.class);
        ConfigData<?> c = ConfigListener.ServerConfigs.MAPPER.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElseThrow();
        return new CP_SyncConfig(t, c);
    }
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.type());
        buf.writeNbt(ConfigListener.ServerConfigs.MAPPER.encodeStart(NbtOps.INSTANCE, this.config()).result().orElse(new CompoundTag()));}

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(CP_SyncConfig packet, PlayPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            Config.CONFIG.setData(packet.type(), packet.config());
            MsLoggy.INFO.log(MsLoggy.LOG_CODE.DATA, "Config packet received on client for {}", packet.type().getSerializedName());
        });
    }
}
