package harmonised.pmmo.config.datapack.codecs;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.util.MsLoggy;

public class CodecTypeSalvage {
	private final Map<String, Double> chancePerLevel;
	private final Map<String, Integer> levelReq;
	private final Map<String, Long> xpAward;
	private final int salvageMax;
	private final double baseChance, maxChance;
	
	public static final Codec<CodecTypeSalvage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("salvageMax").forGetter(CodecTypeSalvage::getSalvageMax),
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("levelReq").forGetter(CodecTypeSalvage::getLevelReq),
			Codec.DOUBLE.fieldOf("baseChance").forGetter(CodecTypeSalvage::getBaseChance),
			Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("chancePerLevel").forGetter(CodecTypeSalvage::getChancePerLevel),
			Codec.DOUBLE.fieldOf("maxChance").forGetter(CodecTypeSalvage::getMaxChance),
			Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("xpPerItem").forGetter(CodecTypeSalvage::getXpAward)
			).apply(instance, CodecTypeSalvage::new));
	
	public CodecTypeSalvage(int salvageMax
			, Map<String, Integer> levelReq
			, double baseChance
			, Map<String, Double> chancePerLevel
			, double maxChance
			, Map<String, Long> xpAward) {
		this.salvageMax = salvageMax;
		this.levelReq = levelReq;
		this.baseChance = baseChance;
		this.chancePerLevel = chancePerLevel;
		this.maxChance = maxChance;
		this.xpAward = xpAward;
	}
	public int getSalvageMax() {return salvageMax;}
	public Map<String, Integer> getLevelReq() {return levelReq;}
	public double getBaseChance() {return baseChance;}
	public Map<String, Double> getChancePerLevel() {return chancePerLevel;}
	public double getMaxChance() {return maxChance;}
	public Map<String, Long> getXpAward() {return xpAward;}
	
	public static class SalvageData {
		public Map<String, Double> chancePerLevel = new HashMap<>();
		public Map<String, Integer> levelReq = new HashMap<>();
		public Map<String, Long> xpAward = new HashMap<>();
		public int salvageMax = 0;
		public double baseChance = 0d, maxChance = 1d;
		
		public SalvageData(CodecTypeSalvage src) {
			chancePerLevel = src.chancePerLevel;
			levelReq = src.levelReq;
			xpAward = src.xpAward;
			salvageMax = src.salvageMax;
			baseChance = src.baseChance;
			maxChance = src.maxChance;
		}
		public SalvageData() {}
		
		@Override
		public String toString() {
			return "chancePerLevel:"+MsLoggy.mapToString(chancePerLevel)+
					"levelReq:"+MsLoggy.mapToString(levelReq)+
					"xpAward:"+MsLoggy.mapToString(xpAward)+
					"salvageMax:"+salvageMax+
					"baseChance:"+baseChance+
					"maxChance:"+maxChance;
		}
	}
}
