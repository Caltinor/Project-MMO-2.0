package harmonised.pmmo.network.clientpackets;

import com.mojang.serialization.Codec;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record CP_SetOtherExperience(Map<String, Experience> map) implements CustomPacketPayload {
	public static final Type<CP_SetOtherExperience> TYPE = new Type<>(Reference.rl("s2c_set_other_xp"));
	public static final StreamCodec<FriendlyByteBuf, CP_SetOtherExperience> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, Experience.STREAM_CODEC), CP_SetOtherExperience::map,
			CP_SetOtherExperience::new
	);
	@Override
	public Type<CP_SetOtherExperience> type() {return TYPE;}
	
	public static void handle(CP_SetOtherExperience packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(UUID.randomUUID(), packet.map());
			MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Client Packet Handled for getting Other Experience");
		});
	}
}
