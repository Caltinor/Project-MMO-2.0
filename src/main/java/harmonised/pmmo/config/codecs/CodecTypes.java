package harmonised.pmmo.config.codecs;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.ModifierDataType;
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
	
	public static record NBTReqData() {/*TODO Generate*/}
	public static final Codec<NBTReqData> NBT_REQ_CODEC = null;
	
	public static record NBTXpGainData() {/*TODO Generate*/}
	public static final Codec<NBTXpGainData> NBT_XPGAIN_CODEC = null;
	
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
	
	public static record GlobalsData (Map<String, String> paths, Map<String, String> constants) {}
	public static final Codec<GlobalsData> GLOBALS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("paths").forGetter(GlobalsData::paths),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("constants").forGetter(GlobalsData::constants)
			).apply(instance, GlobalsData::new));
}
