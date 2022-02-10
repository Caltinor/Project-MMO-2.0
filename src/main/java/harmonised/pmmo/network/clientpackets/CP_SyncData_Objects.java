package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecMapObject;
import harmonised.pmmo.config.codecs.CodecMapObject.ObjectMapContainer;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.config.readers.ModifierDataType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_Objects {
	public static record DataObjectRecord(boolean isItem, Map<ResourceLocation, CodecMapObject.ObjectMapContainer> data) {}
	private static final Codec<DataObjectRecord> MAPPER = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("isItem").forGetter(DataObjectRecord::isItem),
			Codec.unboundedMap(ResourceLocation.CODEC, ObjectMapContainer.CODEC).fieldOf("data").forGetter(DataObjectRecord::data)
			).apply(instance, DataObjectRecord::new));
	
	private final DataObjectRecord data;
	
	public CP_SyncData_Objects(DataObjectRecord data) {this.data = data;}
	public static CP_SyncData_Objects decode(FriendlyByteBuf buf) {
		return new CP_SyncData_Objects(MAPPER.parse(NbtOps.INSTANCE, buf.readNbt()).result().orElse(new DataObjectRecord(false, new HashMap<>())));
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, data).result().orElse(new CompoundTag())));
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			finalizeObjectMaps(data.isItem(), data.data());
		});
		ctx.get().setPacketHandled(true);
	}
	
	private static void finalizeObjectMaps(boolean isItem, Map<ResourceLocation, CodecMapObject.ObjectMapContainer> data) {
		data.forEach((rl, omc) -> {
			List<ResourceLocation> tagValues = List.of(rl);
			if (omc.tagValues().size() > 0) tagValues = omc.tagValues();
			for (ResourceLocation tag : tagValues) {
				for (Map.Entry<EventType, Map<String, Long>> xpValues : omc.xpValues().entrySet()) {
					MsLoggy.info("XP_VALUES: "+xpValues.getKey().toString()+": "+tag.toString()+MsLoggy.mapToString(xpValues.getValue())+" loaded from config");
					Core.get(LogicalSide.CLIENT).getXpUtils().setObjectXpGainMap(xpValues.getKey(), tag, xpValues.getValue());
				}			
				for (Map.Entry<ReqType, Map<String, Integer>> reqs : omc.reqs().entrySet()) {
					MsLoggy.info("REQS: "+reqs.getKey().toString()+": "+tag.toString()+MsLoggy.mapToString(reqs.getValue())+" loaded from config");
					Core.get(LogicalSide.CLIENT).getSkillGates().setObjectSkillMap(reqs.getKey(), tag, reqs.getValue());
				}
				if (isItem) {
					for (Map.Entry<ModifierDataType, Map<String, Double>> modifiers : omc.modifiers().entrySet()) {
						MsLoggy.info("BONUSES: "+tag.toString()+modifiers.getKey().toString()+MsLoggy.mapToString(modifiers.getValue())+" loaded from config");
						Core.get(LogicalSide.CLIENT).getXpUtils().setObjectXpModifierMap(modifiers.getKey(), tag, modifiers.getValue());
					}
					for (Map.Entry<ResourceLocation, SalvageData> salvage : omc.salvage().entrySet()) {
						MsLoggy.info("SALVAGE: "+tag.toString()+": "+salvage.getKey().toString()+salvage.getValue().toString());
						Core.get(LogicalSide.CLIENT).getSalvageLogic().setSalvageData(tag, salvage.getKey(), salvage.getValue());
					}
				}
			}
		});
	}
}
