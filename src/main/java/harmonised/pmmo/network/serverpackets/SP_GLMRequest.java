package harmonised.pmmo.network.serverpackets;

import harmonised.pmmo.features.loot_modifiers.RareDropModifier;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_GLMRareSync;
import harmonised.pmmo.network.clientpackets.CP_GLMTreasureSync;
import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifierManager;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class SP_GLMRequest {
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            for (IGlobalLootModifier modifier : ForgeInternalHandler.INSTANCE.getAllLootMods()) {
                if (modifier instanceof RareDropModifier rareMod) Networking.sendToClient(new CP_GLMRareSync(rareMod), ctx.get().getSender());
                if (modifier instanceof TreasureLootModifier treasureMod) Networking.sendToClient(new CP_GLMTreasureSync(treasureMod), ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
