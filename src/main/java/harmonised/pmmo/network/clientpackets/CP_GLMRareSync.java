package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.features.loot_modifiers.RareDropModifier;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CP_GLMRareSync(RareDropModifier modifier) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, CP_GLMRareSync> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of(CP_GLMRareSync::write, CP_GLMRareSync::decode) , CP_GLMRareSync::modifier, CP_GLMRareSync::new
    );
    public static void write(FriendlyByteBuf buf, RareDropModifier modifier) {
        buf.writeNbt(RareDropModifier.CODEC.codec().encodeStart(NbtOps.INSTANCE, modifier).getOrThrow());
    }
    public static RareDropModifier decode(FriendlyByteBuf buf) {
        return RareDropModifier.CODEC.codec().parse(NbtOps.INSTANCE, buf.readNbt()).getOrThrow();
    }

    public static final Type<CP_GLMRareSync> TYPE = new Type(Reference.rl("cp_glm_rare_sync"));
    @Override public Type<? extends CustomPacketPayload> type() {return TYPE;}
    public static void handle(CP_GLMRareSync packet, IPayloadContext ctx) {
        ctx.enqueueWork(() ->  DataMirror.GLM.add(packet.modifier));
    }
}
