package harmonised.pmmo.network.clientpackets;

import java.util.function.Supplier;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.registry.ConfigurationRegistry;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_ApplyConfigRegistry {
	boolean isOverride;
	public CP_ApplyConfigRegistry(boolean isOverride) {this.isOverride = isOverride;}
	public CP_ApplyConfigRegistry(FriendlyByteBuf buf) {this.isOverride = buf.readBoolean();}
	public void encode(FriendlyByteBuf buf) {buf.writeBoolean(isOverride);}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (isOverride) {
				MsLoggy.INFO.log(LOG_CODE.DATA, "Configuration Overrides from API Applied on Client");
				ConfigurationRegistry.get().applyOverrides(Core.get(LogicalSide.CLIENT));
			}
			else {
				MsLoggy.INFO.log(LOG_CODE.DATA, "Configuration Defaults from API Applied on Client");
				ConfigurationRegistry.get().applyDefaults(Core.get(LogicalSide.CLIENT));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
