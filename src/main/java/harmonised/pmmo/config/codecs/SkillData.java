package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.scripting.Functions;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record SkillData (
	Optional<Integer> color,
	Optional<Boolean> afkExempt,
	Optional<Boolean> displayGroupName,
	Optional<Boolean> showInList,
	Optional<Boolean> useTotalLevels,
	Optional<Map<String, Double>> groupedSkills,
	Optional<Long> maxLevel,
	Optional<ResourceLocation> icon,
	Optional<Integer> iconSize) {

	private static final String COLOR = "color";
	private static final String ICON_SIZE = "iconSize";
	private static final String ICON = "icon";
	private static final String MAX_LEVEL = "maxLevel";
	private static final String AFK_EXEMPT = "noAfkPenalty";
	private static final String DISPLAY = "displayGroupName";
	private static final String SHOW_LIST = "showInList";
	private static final String USE_TOTAL_LVL = "useTotalLevels";
	private static final String GROUP_FOR = "groupFor";

	public static Codec<SkillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf(COLOR).forGetter(SkillData::color),
			Codec.BOOL.optionalFieldOf(AFK_EXEMPT).forGetter(SkillData::afkExempt),
			Codec.BOOL.optionalFieldOf(DISPLAY).forGetter(SkillData::displayGroupName),
			Codec.BOOL.optionalFieldOf(SHOW_LIST).forGetter(SkillData::showInList),
			Codec.BOOL.optionalFieldOf(USE_TOTAL_LVL).forGetter(SkillData::useTotalLevels),
			CodecTypes.DOUBLE_CODEC.optionalFieldOf(GROUP_FOR).forGetter(SkillData::groupedSkills),
			Codec.LONG.optionalFieldOf(MAX_LEVEL).forGetter(SkillData::maxLevel),
			ResourceLocation.CODEC.optionalFieldOf(ICON).forGetter(SkillData::icon),
			Codec.INT.optionalFieldOf(ICON_SIZE).forGetter(SkillData::iconSize)
			).apply(instance, SkillData::new));
	
	public int getColor() { return color.orElse(16777215); }
	public boolean getAfkExempt() { return afkExempt.orElse(false); }
	public boolean getDisplayGroupName() { return displayGroupName.orElse(false); }
	public boolean getShowInList() {return showInList.orElse(true);}
	public boolean getUseTotalLevels() { return useTotalLevels.orElse(false); }
	public long getMaxLevel() { return maxLevel.orElse(Config.server().levels().maxLevel()); }
	public ResourceLocation getIcon() { return icon.orElse(Reference.rl("textures/skills/missing_icon.png")); }
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
		boolean afkExempt, displayName, useTotal, showInList;
		ResourceLocation icon;
		Map<String, Double> groupOf;
		
		private Builder() {
			color = 16777215;
			maxLevel = Integer.MAX_VALUE;
			afkExempt = false;
			displayName = false;
			showInList = true;
			useTotal = false;
			icon = Reference.rl("textures/skills/missing_icon.png");
			iconSize = 18;
			groupOf = new HashMap<>();
		}
		public static SkillData getDefault() {
			return new SkillData(
				Optional.of(16777215), 
				Optional.of(false), 
				Optional.of(false),
				Optional.of(true),
				Optional.of(false),
				Optional.empty(), 
				Optional.of(Config.server().levels().maxLevel()),
				Optional.of(Reference.rl("textures/skills/missing_icon.png")),
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
		public Builder withMaxLevel(long maxLevel) {
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
		public Builder withShowInList(boolean show) {
			this.showInList = show;
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
				Optional.of(showInList),
				Optional.of(useTotal),
				groupOf.isEmpty() ? Optional.empty() : Optional.of(groupOf),
				Optional.of(maxLevel),
				Optional.of(icon),
				Optional.of(iconSize)
			);
		}


		public SkillData fromScripting(Map<String, String> values) {
			if (values.containsKey(COLOR)) this.withColor(Integer.parseInt(values.get(COLOR)));
			if (values.containsKey(ICON_SIZE)) this.withIconSize(Integer.parseInt(values.get(ICON_SIZE)));
			if (values.containsKey(MAX_LEVEL)) this.withMaxLevel(Long.parseLong(values.get(MAX_LEVEL)));
			if (values.containsKey(AFK_EXEMPT)) this.withAfkExempt(Boolean.parseBoolean(values.get(AFK_EXEMPT)));
			if (values.containsKey(DISPLAY)) this.withDisplayName(Boolean.parseBoolean(values.get(DISPLAY)));
			if (values.containsKey(USE_TOTAL_LVL)) this.withUseTotal(Boolean.parseBoolean(values.get(USE_TOTAL_LVL)));
			if (values.containsKey(SHOW_LIST)) this.withShowInList(Boolean.parseBoolean(values.get(SHOW_LIST)));
			if (values.containsKey(ICON)) this.withIcon(Reference.of(values.get(ICON)));
			if (values.containsKey(GROUP_FOR)) this.setGroupOf(Functions.doubleMap(values.get(GROUP_FOR)));
			return this.build();
		}
	}
}
