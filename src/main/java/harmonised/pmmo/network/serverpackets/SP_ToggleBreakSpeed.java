package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.core.perks.PerksImpl;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SP_ToggleBreakSpeed(boolean enabled) implements CustomPacketPayload {
    public static final Type<SP_ToggleBreakSpeed> TYPE = new Type(Reference.rl("c2s_toggle_break_speed"));
    @Override public Type<SP_ToggleBreakSpeed> type() {return TYPE;}
    public static final StreamCodec<FriendlyByteBuf, SP_ToggleBreakSpeed> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SP_ToggleBreakSpeed::enabled, SP_ToggleBreakSpeed::new
    );
    public static void handle(SP_ToggleBreakSpeed packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> PerksImpl.breakSpeedEnabled.put(ctx.player(), packet.enabled()));
    }
}
