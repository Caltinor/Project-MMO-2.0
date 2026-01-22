package harmonised.pmmo.network.clientpackets;

import harmonised.pmmo.client.events.ClientTickHandler;
import harmonised.pmmo.client.utils.ClientUtils;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CP_UpdateExperience(String skill, Experience xp, long change) implements CustomPacketPayload{
	public static final Type<CP_UpdateExperience> TYPE = new Type<>(Reference.rl("s2c_update_xp"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CP_UpdateExperience> CODEC = StreamCodec
			.composite(ByteBufCodecs.STRING_UTF8, CP_UpdateExperience::skill,
					Experience.STREAM_CODEC, CP_UpdateExperience::xp,
					ByteBufCodecs.VAR_LONG, CP_UpdateExperience::change,
					CP_UpdateExperience::new);

	public static void handle(CP_UpdateExperience packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
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

	@Override
	public Type<? extends CustomPacketPayload> type() {return TYPE;}
}
