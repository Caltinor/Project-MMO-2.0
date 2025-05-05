package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CP_GLMTreasureSync(TreasureLootModifier modifier)  implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, CP_GLMTreasureSync> STREAM_COODEC = StreamCodec.composite(
            StreamCodec.of(CP_GLMTreasureSync::write, CP_GLMTreasureSync::decode), CP_GLMTreasureSync::modifier, CP_GLMTreasureSync::new
    );
    public static void write(FriendlyByteBuf buf, TreasureLootModifier modifier) {
        buf.writeNbt(TreasureLootModifier.CODEC.codec().encodeStart(NbtOps.INSTANCE, modifier).getOrThrow());
    }
    public static TreasureLootModifier decode(FriendlyByteBuf buf) {
        return TreasureLootModifier.CODEC.codec().parse(NbtOps.INSTANCE, buf.readNbt()).getOrThrow();
    }

    public static final Type<CP_GLMTreasureSync> TYPE = new Type(Reference.rl("cp_glm_treasure_sync"));
    @Override public Type<CP_GLMTreasureSync> type() {return TYPE;}
    public static void handle(CP_GLMTreasureSync packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> DataMirror.GLM.add(packet.modifier));
    }
}
