package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.utils.DataMirror;
import harmonised.pmmo.features.loot_modifiers.RareDropModifier;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CP_GLMTreasureSync {
    TreasureLootModifier glm;
    public CP_GLMTreasureSync(TreasureLootModifier glm) {this.glm = glm;}
    public CP_GLMTreasureSync(FriendlyByteBuf buf) {this(TreasureLootModifier.CODEC.parse(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(false, err -> MsLoggy.ERROR.log(MsLoggy.LOG_CODE.NETWORK, err)));}
    public void encode(FriendlyByteBuf buf) {buf.writeNbt((CompoundTag) TreasureLootModifier.CODEC.encodeStart(NbtOps.INSTANCE, this.glm).getOrThrow(false, err -> MsLoggy.ERROR.log(MsLoggy.LOG_CODE.NETWORK, err)));}
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->  DataMirror.GLM.add(this.glm));
        ctx.get().setPacketHandled(true);
    }
}
