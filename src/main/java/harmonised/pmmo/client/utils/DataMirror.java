package harmonised.pmmo.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.features.loot_modifiers.RareDropModifier;
import harmonised.pmmo.features.loot_modifiers.SkillLootConditionHighestSkill;
import harmonised.pmmo.features.loot_modifiers.SkillLootConditionKill;
import harmonised.pmmo.features.loot_modifiers.SkillLootConditionPlayer;
import harmonised.pmmo.features.loot_modifiers.TreasureLootModifier;
import harmonised.pmmo.features.loot_modifiers.ValidBlockCondition;
import harmonised.pmmo.mixin.LootTableConditionMixin;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.fml.LogicalSide;

/**This class serves as a run-time cache of data that
 * PmmoSavedData typicaly stores.  
 * 
 * Deprecated interface methods are labeled such as they
 * should not be called from the client.  They are logical
 * methods used on the server
 * 
 * @author Caltinor
 *
 */
public class DataMirror implements IDataStorage{
	public DataMirror() {}
	
	public boolean me(UUID id) {return id == null || id.equals(Minecraft.getInstance().player.getUUID());}
	
	private Map<String, Long> mySkills = new HashMap<>();
	private Map<String, Long> otherSkills = new HashMap<>();
	private Map<String, Long> scheduledXp = new HashMap<>();
	private List<Long> levelCache = new ArrayList<>();
	
	public void setLevelCache(List<Long> cache) {levelCache = cache;}
	
	public long getScheduledXp(String skill) {return scheduledXp.getOrDefault(skill, 0l);}
	
	@Override
	public int getLevelFromXP(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (i == Config.MAX_LEVEL.get())
				return i;
			if (levelCache.get(i) > xp)
				return Core.get(LogicalSide.CLIENT).getLevelProvider().process("", i);
		}
		return Config.MAX_LEVEL.get();
	}
	
	private int getLevelFromXPwithoutLevelProvider(long xp) {
		for (int i = 0; i < levelCache.size(); i++) {
			if (levelCache.get(i) > xp)
				return i;
		}
		return Config.MAX_LEVEL.get();
	}
	
	public double getXpWithPercentToNextLevel(long rawXP) {
		int currentLevel = getLevelFromXPwithoutLevelProvider(rawXP);
		currentLevel = currentLevel >= levelCache.size() ? levelCache.size()-1 : currentLevel;
		long currentXPThreshold = currentLevel - 1 >= 0 ? levelCache.get(currentLevel - 1) : 0;
		long xpToNextLevel = levelCache.get(currentLevel) - currentXPThreshold;
		long progress = rawXP - currentXPThreshold;
		return (double)Core.get(LogicalSide.CLIENT).getLevelProvider().process("", currentLevel) + (double)progress/(double)xpToNextLevel;
	}
	
	@Override
	public long getXpRaw(UUID playerID, String skillName) {
		return me(playerID) ? mySkills.getOrDefault(skillName, 0L) : otherSkills.getOrDefault(skillName, 0L);
	}
	@Override
	public void setXpRaw(UUID playerID, String skillName, long value) {
		if (!me(playerID)) return;
		long oldValue = getXpRaw(playerID, skillName);
		if (value > oldValue)
			scheduledXp.merge(skillName, value-oldValue, Long::sum);
		mySkills.put(skillName, value);
		int newLevel = getLevelFromXP(value);
		int oldLevel = getLevelFromXP(oldValue);
		if (oldLevel < newLevel)
			ClientUtils.sendLevelUpUnlocks(skillName, newLevel);
		MsLoggy.DEBUG.log(LOG_CODE.XP,"Client Side Skill Map: "+MsLoggy.mapToString(mySkills));		
	}
	@Override
	public Map<String, Long> getXpMap(UUID playerID) {return me(playerID) ? mySkills : otherSkills;}
	@Override
	public void setXpMap(UUID playerID, Map<String, Long> map) {
		if (me(playerID))
			mySkills = map;
		else
			otherSkills = map;
	}
	@Override
	public int getPlayerSkillLevel(String skill, UUID player) {
		int rawLevel =  me(player) ? getLevelFromXP(mySkills.getOrDefault(skill, 0l)) 
			: getLevelFromXP(otherSkills.getOrDefault(skill, 0l));
		rawLevel = Core.get(LogicalSide.CLIENT).getLevelProvider().process(skill, rawLevel);
		int skillMax = SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.Builder.getDefault()).getMaxLevel();
		return Math.min(rawLevel, skillMax);
	}
	@Override
	public IDataStorage get() {return this;}
	@Override
	public long getBaseXpForLevel(int level) {return level > 0 && (level -1) < levelCache.size() ? levelCache.get(level - 1) : 0l;}

	//GLM clones
	public record GLM(Component header, ItemStack drop, int count, double chance, boolean perLevel, String skill, LootItemCondition[] conditions) {
		public static void add(RareDropModifier modifier) {
			DataMirror data = (DataMirror) Core.get(LogicalSide.CLIENT).getData();
			data.lootModifiers.add(new GLM(LangProvider.GLM_HEADER_RARE.asComponent().withStyle(ChatFormatting.BOLD), modifier.drop, modifier.drop.getCount(),
					modifier.chance, modifier.perLevel, modifier.skill, modifier.getConditions()));
		}

		public static void add(TreasureLootModifier modifier) {
			DataMirror data = (DataMirror) Core.get(LogicalSide.CLIENT).getData();
			data.lootModifiers.add(new GLM(LangProvider.GLM_HEADER_TREASURE.asComponent().withStyle(ChatFormatting.BOLD), modifier.drop, modifier.count,
					modifier.chance, modifier.perLevel, modifier.skill, modifier.getConditions()));
		}

		public List<Component> getGUILines(Core core) {
			List<Component> linesOut = new ArrayList<>();
			linesOut.add(header);
			Component dropText = drop.is(Items.AIR) ? Component.literal("itself") : drop.getDisplayName();
			linesOut.add(LangProvider.GLM_DROP_ITEM.asComponent(count, dropText));
			double actualChance = chance * (perLevel ? core.getData().getPlayerSkillLevel(skill, null) : 1d);
			String actualChanceFormated = String.valueOf(actualChance * 100d);
			linesOut.add(perLevel 
				? LangProvider.GLM_DROP_CHANCE_SKILL.asComponent(actualChanceFormated, LangProvider.skill(skill))
				: LangProvider.GLM_DROP_CHANCE.asComponent(actualChanceFormated)
			);
			int otherConditions = 0;
			for (LootItemCondition condition : conditions) {
				if (condition instanceof LootTableConditionMixin lootCondition) {
					linesOut.add(LangProvider.GLM_LOOT_TABLE.asComponent(lootCondition.getTargetLootTableId().getPath()));
				}
				else if (condition instanceof SkillLootConditionPlayer playerSkillCondition) {
					int maxLevel = Math.min(playerSkillCondition.levelMax, SkillsConfig.SKILLS.get().getOrDefault(playerSkillCondition.skill, SkillData.Builder.getDefault()).getMaxLevel());
					linesOut.add(LangProvider.GLM_SKILL_RANGE.asComponent(LangProvider.skill(playerSkillCondition.skill), 
							playerSkillCondition.levelMin, maxLevel));
				}
				else if (condition instanceof SkillLootConditionHighestSkill highSkillCondition) {
					String skills = highSkillCondition.comparables.stream().map(str -> LangProvider.skill(str).toString()).collect(Collectors.joining(", "));
					linesOut.add(LangProvider.GLM_HIGHEST_SKILL.asComponent(LangProvider.skill(highSkillCondition.targetSkill), skills));
				}
				else if (condition instanceof SkillLootConditionKill killCondition) {
					int maxLevel = Math.min(killCondition.levelMax, SkillsConfig.SKILLS.get().getOrDefault(killCondition.skill, SkillData.Builder.getDefault()).getMaxLevel());
					linesOut.add(LangProvider.GLM_SKILL_RANGE.asComponent(LangProvider.skill(killCondition.skill),
							killCondition.levelMin, maxLevel));
				}
				else if (condition instanceof ValidBlockCondition blockCondition) {
					Component target = blockCondition.tag == null
							? blockCondition.block.getName()
							: Component.literal(blockCondition.tag.location().toString());
					linesOut.add(LangProvider.GLM_VALID_BLOCK.asComponent(target));
				}
				else otherConditions++;
			}
			if (otherConditions > 0) linesOut.add(LangProvider.GLM_OTHER_CONDITIONS.asComponent(otherConditions));
			return linesOut;
		}
	}
	public final List<GLM> lootModifiers = new ArrayList<>();
}
