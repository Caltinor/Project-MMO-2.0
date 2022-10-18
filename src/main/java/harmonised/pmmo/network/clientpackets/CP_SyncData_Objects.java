package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecMapObject;
import harmonised.pmmo.config.codecs.CodecMapObject.ObjectMapContainer;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_Objects {
	public static record DataObjectRecord(ObjectType type, Map<ResourceLocation, CodecMapObject.ObjectMapContainer> data) {}
	private static final Codec<DataObjectRecord> MAPPER = RecordCodecBuilder.create(instance -> instance.group(
			ObjectType.CODEC.fieldOf("type").forGetter(DataObjectRecord::type),
			Codec.unboundedMap(ResourceLocation.CODEC, ObjectMapContainer.CODEC).fieldOf("data").forGetter(DataObjectRecord::data)
			).apply(instance, DataObjectRecord::new));
	
	private final DataObjectRecord data;
	
	public CP_SyncData_Objects(DataObjectRecord data) {this.data = data;}
	public static CP_SyncData_Objects decode(FriendlyByteBuf buf) {
		return new CP_SyncData_Objects(MAPPER.parse(NbtOps.INSTANCE, buf.readNbt(NbtAccounter.UNLIMITED)).result().orElse(new DataObjectRecord(ObjectType.ITEM, new HashMap<>())));
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, data).result().orElse(new CompoundTag())));
		MsLoggy.DEBUG.log(LOG_CODE.NETWORK, "Payload for {}/{} is {}", this.getClass().getSimpleName(), data.type().name(), buf.readableBytes());
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			finalizeObjectMaps(data.type(), data.data());
			Core.get(LogicalSide.CLIENT).registerNBT();
		});
		ctx.get().setPacketHandled(true);
	}
	
	private static void finalizeObjectMaps(ObjectType type, Map<ResourceLocation, CodecMapObject.ObjectMapContainer> data) {
		data.forEach((rl, omc) -> {
			List<ResourceLocation> tagValues = List.of(rl);
			if (omc.tagValues().size() > 0) tagValues = omc.tagValues();
			for (ResourceLocation tag : tagValues) {
				for (Map.Entry<EventType, Map<String, Long>> xpValues : omc.xpValues().entrySet()) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "XP_VALUES: "+xpValues.getKey().toString()+": "+tag.toString()+MsLoggy.mapToString(xpValues.getValue())+" loaded from config");
					Core.get(LogicalSide.CLIENT).getXpUtils().setObjectXpGainMap(xpValues.getKey(), tag, xpValues.getValue());
				}			
				for (Map.Entry<ReqType, Map<String, Integer>> reqs : omc.reqs().entrySet()) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "REQS: "+reqs.getKey().toString()+": "+tag.toString()+MsLoggy.mapToString(reqs.getValue())+" loaded from config");
					Core.get(LogicalSide.CLIENT).getSkillGates().setObjectSkillMap(reqs.getKey(), tag, reqs.getValue());
				}
				if (!omc.veinData().equals(VeinData.EMPTY)) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "VEIN DATA: "+omc.veinData().toString());
					Core.get(LogicalSide.CLIENT).getVeinData().setVeinData(tag, omc.veinData());
				}
				switch (type) {
				case ITEM: {
					for (Map.Entry<ModifierDataType, Map<String, Double>> modifiers : omc.modifiers().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "BONUSES: "+tag.toString()+modifiers.getKey().toString()+MsLoggy.mapToString(modifiers.getValue())+" loaded from config");
						Core.get(LogicalSide.CLIENT).getXpUtils().setObjectXpModifierMap(modifiers.getKey(), tag, modifiers.getValue());
					}
					for (Map.Entry<ReqType, List<LogicEntry>> nbtReqs : omc.nbtReqs().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT REQS: "+nbtReqs.getKey().toString()+": "+tag.toString()+" loaded from config");
						Core.get(LogicalSide.CLIENT).getNBTUtils().setItemReq(nbtReqs.getKey(), tag, nbtReqs.getValue());
					}
					for (Map.Entry<EventType, List<LogicEntry>> nbtGains : omc.nbtXpGains().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT GAINS: "+nbtGains.getKey().toString()+": "+tag.toString()+" loaded from config");
						Core.get(LogicalSide.CLIENT).getNBTUtils().setItemXpGains(nbtGains.getKey(), tag, nbtGains.getValue());
					}
					for (Map.Entry<ModifierDataType, List<LogicEntry>> nbtBonus : omc.nbtBonuses().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT BONUS: "+nbtBonus.getKey().toString()+": "+tag.toString()+" loaded from config");
						Core.get(LogicalSide.CLIENT).getNBTUtils().setBonuses(nbtBonus.getKey(), tag, nbtBonus.getValue());
					}
					for (Map.Entry<ResourceLocation, SalvageData> salvage : omc.salvage().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "SALVAGE: "+tag.toString()+": "+salvage.getKey().toString()+salvage.getValue().toString());
						Core.get(LogicalSide.CLIENT).getSalvageLogic().setSalvageData(tag, salvage.getKey(), salvage.getValue());
					}
					break;
				}
				case BLOCK: {
					for (Map.Entry<ReqType, List<LogicEntry>> nbtReqs : omc.nbtReqs().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT REQS: "+nbtReqs.getKey().toString()+": "+tag.toString()+" loaded from config");
						Core.get(LogicalSide.CLIENT).getNBTUtils().setBlockReq(nbtReqs.getKey(), tag, nbtReqs.getValue());
					}
					for (Map.Entry<EventType, List<LogicEntry>> nbtGains : omc.nbtXpGains().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT GAINS: "+nbtGains.getKey().toString()+": "+tag.toString()+" loaded from config");
						Core.get(LogicalSide.CLIENT).getNBTUtils().setItemXpGains(nbtGains.getKey(), tag, nbtGains.getValue());
					}
					break;
				}
				case ENTITY: {
					for (Map.Entry<ReqType, List<LogicEntry>> nbtReqs : omc.nbtReqs().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT REQS: "+nbtReqs.getKey().toString()+": "+tag.toString()+" loaded from config");
						Core.get(LogicalSide.CLIENT).getNBTUtils().setEntityReq(nbtReqs.getKey(), tag, nbtReqs.getValue());
					}
					for (Map.Entry<EventType, List<LogicEntry>> nbtGains : omc.nbtXpGains().logic().entrySet()) {
						MsLoggy.INFO.log(LOG_CODE.DATA, "NBT GAINS: "+nbtGains.getKey().toString()+": "+tag.toString()+" loaded from config");
						Core.get(LogicalSide.CLIENT).getNBTUtils().setItemXpGains(nbtGains.getKey(), tag, nbtGains.getValue());
					}
					break;
				}
				default: {}
				}
			}
		});
	}
}
