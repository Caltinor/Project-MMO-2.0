package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.events.ClientTickHandler;
import harmonised.pmmo.client.utils.ClientUtils;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record CP_UpdateExperience(String skill, Experience xp, long change) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "s2c_update_xp");

	public CP_UpdateExperience(FriendlyByteBuf buf) {
		this(buf.readUtf(), new Experience(new Experience.XpLevel(buf.readLong()), buf.readLong()), buf.readLong());
	}
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(skill);
		buf.writeLong(xp.getLevel().getLevel());
		buf.writeLong(xp.getXp());
		buf.writeLong(change);
	}

	@Override
	public ResourceLocation id() {return ID;}

	public static void handle(CP_UpdateExperience packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			Core.get(LogicalSide.CLIENT).getData().getXpMap(null).put(packet.skill(), packet.xp());
			if (packet.change() > 0) {
				ClientTickHandler.addToGainList(packet.skill(), packet.change());
				Experience previousXp = new Experience(new Experience.XpLevel(packet.xp().getLevel().getLevel()), packet.xp().getXp());
				previousXp.addXp(-packet.change());
				if (packet.xp().getLevel().getLevel() > previousXp.getLevel().getLevel())
					ClientUtils.sendLevelUpUnlocks(packet.skill(), previousXp.getLevel().getLevel(), packet.xp().getLevel().getLevel());
			}
			MsLoggy.DEBUG.log(LOG_CODE.XP, "Client Packet Handled for updating experience of "+packet.skill()+"["+packet.xp()+"]");
		});
	}
}
