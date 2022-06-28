package harmonised.pmmo.config.codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.config.Config;

public record SkillData (
	Optional<Integer> color,
	Optional<Boolean> afkExempt,
	Optional<Boolean> displayGroupName,
	Optional<Boolean> useTotalLevels,
	Optional<Map<String, Double>> groupedSkills,
	Optional<Integer> maxLevel) {
	
	public static Codec<SkillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("color").forGetter(SkillData::color),
			Codec.BOOL.optionalFieldOf("noAfkPenalty").forGetter(SkillData::afkExempt),
			Codec.BOOL.optionalFieldOf("displayGroupName").forGetter(SkillData::displayGroupName),
			Codec.BOOL.optionalFieldOf("useTotalLevels").forGetter(SkillData::useTotalLevels),
			CodecTypes.DOUBLE_CODEC.optionalFieldOf("groupFor").forGetter(SkillData::groupedSkills),
			Codec.INT.optionalFieldOf("maxLevel").forGetter(SkillData::maxLevel)
			).apply(instance, SkillData::new));

	public int getColor() {return color.orElse(16777215);}
	public boolean getAfkExempt() {return afkExempt.orElse(false);}
	public boolean getDisplayGroupName() {return displayGroupName.orElse(false);}
	public boolean getUseTotalLevels() {return useTotalLevels.orElse(false);}
	public int getMaxLevel() {return maxLevel.orElse(Config.MAX_LEVEL.get());}
	
	public boolean isSkillGroup() {return !getGroup().isEmpty();}
	public Map<String, Double> getGroup() {return groupedSkills.orElse(new HashMap<>());}
	
	public Map<String, Long> getGroupXP(long xp) {
		Map<String, Long> outMap = new HashMap<>();
		double denominator = getGroup().values().stream().mapToDouble(value -> value).sum();
		getGroup().forEach((skill, ratio) -> {
			outMap.put(skill, Double.valueOf((ratio / denominator) * xp).longValue());
		});
		return outMap;
	}
	
	public Map<String, Integer> getGroupReq(int level) {
		Map<String, Integer> outMap = new HashMap<>();
		double denominator = getGroup().values().stream().mapToDouble(value -> value).sum();
		getGroup().forEach((skill, ratio) -> {
			outMap.put(skill, (int)((ratio / denominator) * (double)level));
		});
		return outMap;
	}
	
	public Map<String, Double> getGroupBonus(double bonus) {
		Map<String, Double> outMap = new HashMap<>();
		double denominator = getGroup().values().stream().mapToDouble(value -> value).sum();
		double gainLossModifier = bonus >= 1d ? 1d : 0d;
		getGroup().forEach((skill, ratio) -> {
			outMap.put(skill, gainLossModifier + (ratio / denominator) * bonus);
		});
		return outMap;
	}
	
	public static class Builder {
		int color, maxLevel;
		boolean afkExempt, displayName, useTotal;
		Map<String, Double> groupOf;
		
		private Builder() {
			color = 16777215;
			maxLevel = Integer.MAX_VALUE;
			afkExempt = false;
			displayName = false;
			useTotal = false;
			groupOf = new HashMap<>();
		}
		public static SkillData getDefault() {return new SkillData(
				Optional.of(16777215), 
				Optional.of(false), 
				Optional.of(false),
				Optional.of(false),
				Optional.empty(), 
				Optional.of(Config.MAX_LEVEL.get()));}
		
		public static Builder start() {
			return new Builder();
		}
		public Builder withColor(int color) {
			this.color = color;
			return this;
		}
		public Builder withMaxLevel(int maxLevel) {
			this.maxLevel = maxLevel;
			return this;
		}
		public Builder withAfkExempt(boolean afkExempt) {
			this.afkExempt = afkExempt;
			return this;
		}
		public Builder withDisplayName(boolean displayGroupName) {
			this.displayName = displayGroupName;
			return this;
		}
		public Builder withUseTotal(boolean useTotalLevels) {
			this.useTotal = useTotalLevels;
			return this;
		}
		public Builder setGroupOf(Map<String, Double> group) {
			this.groupOf = group;
			return this;
		}
		public SkillData build() {
			return new SkillData(
					Optional.of(color), 
					Optional.of(afkExempt),
					Optional.of(displayName),
					Optional.of(useTotal),
					groupOf.isEmpty() ? Optional.empty() : Optional.of(groupOf),
					Optional.of(maxLevel));			
		}
	}
}
