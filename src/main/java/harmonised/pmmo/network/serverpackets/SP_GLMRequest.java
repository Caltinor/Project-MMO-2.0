package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.loot_modifiers.RareDropModifier;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
import harmonised.pmmo.mixin.ForgeInternalHandlerMixin;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_GLMRareSync;
import harmonised.pmmo.network.clientpackets.CP_GLMTreasureSync;
import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SP_GLMRequest {
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            for (IGlobalLootModifier modifier : ForgeInternalHandlerMixin.getINSTANCE().getAllLootMods()) {
                if (modifier instanceof RareDropModifier rareMod) Networking.sendToClient(new CP_GLMRareSync(rareMod), ctx.get().getSender());
                if (modifier instanceof TreasureLootModifier treasureMod) Networking.sendToClient(new CP_GLMTreasureSync(treasureMod), ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
