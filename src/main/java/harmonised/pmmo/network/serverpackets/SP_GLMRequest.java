package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.loot_modifiers.RareDropModifier;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
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
import net.neoforged.neoforge.resource.NeoForgeReloadListeners;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Objects;

public record SP_GLMRequest() implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, SP_GLMRequest> STREAM_CODEC = StreamCodec.unit(new SP_GLMRequest());
    public static final Type<SP_GLMRequest> TYPE = new Type(Reference.rl("sp_glm_request"));
    @Override public Type<SP_GLMRequest> type() {return TYPE;}

    public static void handle(SP_GLMRequest packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            for (IGlobalLootModifier modifier : Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer())
                    .getServerResources()
                    .managers()
                    .getListener(NeoForgeReloadListeners.LOOT_MODIFIERS_KEY).getSortedModifiers()) {
                if (modifier instanceof RareDropModifier rareMod) Networking.sendToClient(new CP_GLMRareSync(rareMod), (ServerPlayer) ctx.player());
                else if (modifier instanceof TreasureLootModifier treasureMod) Networking.sendToClient(new CP_GLMTreasureSync(treasureMod), (ServerPlayer) ctx.player());
            }
        });
    }
}
