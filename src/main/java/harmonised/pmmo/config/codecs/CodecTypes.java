package harmonised.pmmo.config.codecs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.ModifierDataType;
import harmonised.pmmo.core.nbt.LogicEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public class CodecTypes {
	public static final Codec<Map<String, Double>> DOUBLE_CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);
	public static final Codec<Map<String, Long>> LONG_CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG);
	public static final Codec<Map<String, Integer>> INTEGER_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);	
	public static final Codec<Map<String, Map<String, Integer>>> ENCHANTMENT_CODEC = Codec.unboundedMap(Codec.STRING, INTEGER_CODEC);
	
	public static record ModifierData(Map<ModifierDataType, Map<String, Double>> obj) {}
	public static final Codec<ModifierData> MODIFIER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ModifierDataType.CODEC, DOUBLE_CODEC, StringRepresentable.keys(ModifierDataType.values())).forGetter(ModifierData::obj)
			).apply(instance, ModifierData::new));
	
	public static record MobMultiplierData(Map<ResourceLocation, Map<String, Double>> obj) {}
	public static final Codec<MobMultiplierData> MOB_MULTIPLIER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(ResourceLocation.CODEC, DOUBLE_CODEC).fieldOf("mob_multiplier").forGetter(MobMultiplierData::obj)
			).apply(instance, MobMultiplierData::new));
	
	public static record EventData(Map<EventType, Map<String, Long>> obj) {}
	public static final Codec<EventData> EVENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(EventType.CODEC, LONG_CODEC, StringRepresentable.keys(EventType.values())).forGetter(EventData::obj)
			).apply(instance, EventData::new));
	
	public static record ReqData(Map<ReqType, Map<String, Integer>> obj) {}
	public static final Codec<ReqData> REQ_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ReqType.CODEC, INTEGER_CODEC, StringRepresentable.keys(ReqType.values())).forGetter(ReqData::obj)
			).apply(instance, ReqData::new));
	
	public static record NBTReqData(Map<ReqType, List<LogicEntry>> logic) {
		public NBTReqData() {this(new HashMap<>());}
	}
	public static final Codec<NBTReqData> NBT_REQ_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ReqType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(ReqType.values())).forGetter(NBTReqData::logic) 
			).apply(instance, NBTReqData::new));
	
	public static record NBTXpGainData(Map<EventType, List<LogicEntry>> logic) {
		public NBTXpGainData() {this(new HashMap<>());}
	}
	public static final Codec<NBTXpGainData> NBT_XPGAIN_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(EventType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(EventType.values())).forGetter(NBTXpGainData::logic) 
			).apply(instance, NBTXpGainData::new));
	
	public static record NBTBonusData(Map<ModifierDataType, List<LogicEntry>> logic) {
		public NBTBonusData() {this(new HashMap<>());}
	}
	public static final Codec<NBTBonusData> NBT_BONUS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ModifierDataType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(ModifierDataType.values())).forGetter(NBTBonusData::logic) 
			).apply(instance, NBTBonusData::new));
	
	public record SalvageData (
			Map<String, Double> chancePerLevel,
			Map<String, Integer> levelReq,
			Map<String, Long> xpAward,
			int salvageMax,
			double baseChance, 
			double maxChance){}
	public static final Codec<SalvageData> SALVAGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("chancePerLevel").forGetter(SalvageData::chancePerLevel),
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("levelReq").forGetter(SalvageData::levelReq),
			Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("xpPerItem").forGetter(SalvageData::xpAward),
			Codec.INT.fieldOf("salvageMax").forGetter(SalvageData::salvageMax),			
			Codec.DOUBLE.fieldOf("baseChance").forGetter(SalvageData::baseChance),			
			Codec.DOUBLE.fieldOf("maxChance").forGetter(SalvageData::maxChance)			
			).apply(instance, SalvageData::new));
	
	public static final PrimitiveCodec<UUID> UUID_CODEC = new PrimitiveCodec<>() {
		@Override
		public <T> DataResult<UUID> read(DynamicOps<T> ops, T input) {
			return DataResult.success(UUID.fromString(ops.getStringValue(input).getOrThrow(false, null)));
		}
		@Override
		public <T> T write(DynamicOps<T> ops, UUID value) {
			return ops.createString(value.toString());
		}
		@Override
		public String toString() { return "uuid";}
	};
}
