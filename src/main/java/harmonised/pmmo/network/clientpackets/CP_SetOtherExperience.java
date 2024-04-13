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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record CP_SetOtherExperience(Map<String, Experience> map) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "s2c_set_other_xp");
	@Override
	public ResourceLocation id() {return ID;}
	public CP_SetOtherExperience(FriendlyByteBuf buf) {
		this(Codec.unboundedMap(Codec.STRING, Experience.CODEC)
				.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(new HashMap<>()));
	}
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeNbt(Codec.unboundedMap(Codec.STRING, Experience.CODEC)
				.encodeStart(NbtOps.INSTANCE, map).result().orElse(new CompoundTag()));
	}
	
	public static void handle(CP_SetOtherExperience packet, PlayPayloadContext ctx) {
		ctx.workHandler().execute(() -> {
			Core.get(LogicalSide.CLIENT).getData().setXpMap(UUID.randomUUID(), packet.map());
			MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Client Packet Handled for getting Other Experience");
		});
	}
}
