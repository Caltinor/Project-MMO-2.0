package harmonised.pmmo.config.datapack.codecs;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CodecTypeSalvage (
	Map<String, Double> chancePerLevel,
	Map<String, Integer> levelReq,
	Map<String, Long> xpAward,
	int salvageMax,
	double baseChance, 
	double maxChance){
	
	public static final Codec<CodecTypeSalvage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("chancePerLevel").forGetter(CodecTypeSalvage::chancePerLevel),
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("levelReq").forGetter(CodecTypeSalvage::levelReq),
			Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("xpPerItem").forGetter(CodecTypeSalvage::xpAward),
			Codec.INT.fieldOf("salvageMax").forGetter(CodecTypeSalvage::salvageMax),			
			Codec.DOUBLE.fieldOf("baseChance").forGetter(CodecTypeSalvage::baseChance),			
			Codec.DOUBLE.fieldOf("maxChance").forGetter(CodecTypeSalvage::maxChance)			
			).apply(instance, CodecTypeSalvage::new));
}
