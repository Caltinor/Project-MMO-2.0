package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record SkillData (
	Optional<Integer> color,
	Optional<Boolean> afkExempt,
	Optional<Boolean> displayGroupName,
	Optional<Boolean> useTotalLevels,
	Optional<Map<String, Double>> groupedSkills,
	Optional<Long> maxLevel,
	Optional<ResourceLocation> icon,
	Optional<Integer> iconSize) {
	
	public static Codec<SkillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("color").forGetter(SkillData::color),
			Codec.BOOL.optionalFieldOf("noAfkPenalty").forGetter(SkillData::afkExempt),
			Codec.BOOL.optionalFieldOf("displayGroupName").forGetter(SkillData::displayGroupName),
			Codec.BOOL.optionalFieldOf("useTotalLevels").forGetter(SkillData::useTotalLevels),
			CodecTypes.DOUBLE_CODEC.optionalFieldOf("groupFor").forGetter(SkillData::groupedSkills),
			Codec.LONG.optionalFieldOf("maxLevel").forGetter(SkillData::maxLevel),
			ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(SkillData::icon),
			Codec.INT.optionalFieldOf("iconSize").forGetter(SkillData::iconSize)
			).apply(instance, SkillData::new));
	
	public int getColor() { return color.orElse(16777215); }
	public boolean getAfkExempt() { return afkExempt.orElse(false); }
	public boolean getDisplayGroupName() { return displayGroupName.orElse(false); }
	public boolean getUseTotalLevels() { return useTotalLevels.orElse(false); }
	public long getMaxLevel() { return maxLevel.orElse(Config.server().levels().maxLevel()); }
	public ResourceLocation getIcon() { return icon.orElse(new ResourceLocation(Reference.MOD_ID, "textures/skills/missing_icon.png")); }
	public int getIconSize() { return iconSize.orElse(18); }
	
	public boolean isSkillGroup() { return !getGroup().isEmpty(); }
	public Map<String, Double> getGroup() { return groupedSkills.orElse(new HashMap<>()); }
	
	public Map<String, Long> getGroupXP(long xp) {
		Map<String, Long> outMap = new HashMap<>();
		double denominator = getGroup().values().stream().mapToDouble(value -> value).sum();
		getGroup().forEach((skill, ratio) -> {
			outMap.put(skill, Double.valueOf((ratio / denominator) * xp).longValue());
		});
		//iterate over the map for groups within the member map
		new HashMap<>(outMap).forEach((skill, value) -> {
			SkillData skillCheck = Config.skills().get(skill);
			if (skillCheck.isSkillGroup()) {
				outMap.remove(skill);
				skillCheck.getGroupXP(value).forEach((s, x) -> {
					outMap.merge(s, x, Long::sum);
				});
			}
		});
		return outMap;
	}
	
	public Map<String, Long> getGroupReq(long level) {
		Map<String, Long> outMap = new HashMap<>();
		double denominator = getGroup().values().stream().mapToDouble(value -> value).sum();
		getGroup().forEach((skill, ratio) -> {
			outMap.put(skill, (long)((ratio / denominator) * (double)level));
		});
		new HashMap<>(outMap).forEach((skill, value) -> {
			SkillData skillCheck = Config.skills().get(skill);
			if (skillCheck.isSkillGroup()) {
				outMap.remove(skill);
				skillCheck.getGroupReq(value).forEach((s, x) -> {
					outMap.merge(s, x, Long::sum);
				});
			}
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
		new HashMap<>(outMap).forEach((skill, value) -> {
			SkillData skillCheck = Config.skills().get(skill);
			if (skillCheck.isSkillGroup()) {
				outMap.remove(skill);
				skillCheck.getGroupBonus(value).forEach((s, x) -> {
					outMap.merge(s, x, Double::sum);
				});
			}
		});
		return outMap;
	}
	
	public static class Builder {
		int color, iconSize;
		long maxLevel;
		boolean afkExempt, displayName, useTotal;
		ResourceLocation icon;
		Map<String, Double> groupOf;
		
		private Builder() {
			color = 16777215;
			maxLevel = Integer.MAX_VALUE;
			afkExempt = false;
			displayName = false;
			useTotal = false;
			icon = new ResourceLocation(Reference.MOD_ID, "textures/skills/missing_icon.png");
			iconSize = 18;
			groupOf = new HashMap<>();
		}
		public static SkillData getDefault() {
			return new SkillData(
				Optional.of(16777215), 
				Optional.of(false), 
				Optional.of(false),
				Optional.of(false),
				Optional.empty(), 
				Optional.of(Config.server().levels().maxLevel()),
				Optional.of(new ResourceLocation(Reference.MOD_ID, "textures/skills/missing_icon.png")),
				Optional.of(18));
		}
		
		public static Builder start() {
			return new Builder();
		}
		public Builder withColor(int color) {
			this.color = color;
			return this;
		}
		public Builder withIcon(ResourceLocation icon) {
			this.icon = icon;
			return this;
		}
		public Builder withIconSize(int size) {
			this.iconSize = size;
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
				Optional.of(maxLevel),
				Optional.of(icon),
				Optional.of(iconSize)
			);
		}
	}
}
