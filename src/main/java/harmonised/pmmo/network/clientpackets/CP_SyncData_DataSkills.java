package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import harmonised.pmmo.config.DataConfig;
import harmonised.pmmo.config.codecs.CodecMapSkills.SkillData;
import harmonised.pmmo.core.Core;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_DataSkills {
	private static final Codec<Map<String, SkillData>> MAPPER = Codec.unboundedMap(Codec.STRING, SkillData.CODEC);
	
	private final Map<String, SkillData> map;
	
	public CP_SyncData_DataSkills(Map<String, SkillData> map) {this.map = map;}
	public static CP_SyncData_DataSkills decode(FriendlyByteBuf buf) {
		return new CP_SyncData_DataSkills(MAPPER.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(new HashMap<>()));
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, map).result().orElse(new CompoundTag())));
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			DataConfig dc = Core.get(LogicalSide.CLIENT).getDataConfig();
			for (Map.Entry<String, SkillData> entry : map.entrySet()) {
				dc.setSkillData(entry.getKey(), entry.getValue());
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
