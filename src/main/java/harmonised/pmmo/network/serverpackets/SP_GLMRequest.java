package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.loot_modifiers.RareDropModifier;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
import harmonised.pmmo.mixin.NeoEventHandlerMixin;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_GLMRareSync;
import harmonised.pmmo.network.clientpackets.CP_GLMTreasureSync;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SP_GLMRequest() implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, SP_GLMRequest> STREAM_CODEC = StreamCodec.unit(new SP_GLMRequest());
    public static final Type<SP_GLMRequest> TYPE = new Type(Reference.rl("sp_glm_request"));
    @Override public Type<SP_GLMRequest> type() {return TYPE;}

    public static void handle(SP_GLMRequest packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            for (IGlobalLootModifier modifier : NeoEventHandlerMixin.getINSTANCE().getAllLootMods()) {
                if (modifier instanceof RareDropModifier rareMod) Networking.sendToClient(new CP_GLMRareSync(rareMod), (ServerPlayer) ctx.player());
                else if (modifier instanceof TreasureLootModifier treasureMod) Networking.sendToClient(new CP_GLMTreasureSync(treasureMod), (ServerPlayer) ctx.player());
            }
        });
    }
}
