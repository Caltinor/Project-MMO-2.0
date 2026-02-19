package harmonised.pmmo.client.utils;

import harmonised.pmmo.config.Config;
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
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
	
	private Map<String, Experience> mySkills = new HashMap<>();
	private Map<String, Experience> otherSkills = new HashMap<>();
	
	public double getXpWithPercentToNextLevel(Experience rawXP) {
		return  ((double)rawXP.getXp()/(double)rawXP.getLevel().getXpToNext()) + (double)rawXP.getLevel().getLevel();
	}
	
	@Override
	public long getXp(UUID playerID, String skillName) {
		return me(playerID) ? mySkills.getOrDefault(skillName, new Experience()).getXp() : otherSkills.getOrDefault(skillName, new Experience()).getXp();
	}
	@Override
	public void setXp(UUID playerID, String skillName, long value) {
		if (!me(playerID)) return;
		mySkills.computeIfAbsent(skillName, s -> new Experience()).setXp(value);
		MsLoggy.DEBUG.log(LOG_CODE.XP,"Client Side Skill Map: "+MsLoggy.mapToString(mySkills));		
	}
	@Override
	public Map<String, Experience> getXpMap(UUID playerID) {return me(playerID) ? mySkills : otherSkills;}
	@Override
	public void setXpMap(UUID playerID, Map<String, Experience> map) {
		if (me(playerID))
			mySkills = map;
		else
			otherSkills = map;
	}
	@Override
	public long getLevel(String skill, UUID player) {
		long rawLevel =  me(player) ? mySkills.getOrDefault(skill, new Experience()).getLevel().getLevel()
			: otherSkills.getOrDefault(skill, new Experience()).getLevel().getLevel();
		rawLevel = Core.get(LogicalSide.CLIENT).getLevelProvider().process(skill, rawLevel);
		long skillMax = Config.skills().get(skill).getMaxLevel();
		return Math.min(rawLevel, skillMax);
	}
	@Override
	public IDataStorage get() {return this;}

	//GLM clones
	public record GLM(Component header, ItemStack drop, int count, double chance, boolean perLevel, String skill, LootItemCondition[] conditions) {
		public static void add(RareDropModifier modifier) {
			DataMirror data = (DataMirror) Core.get(LogicalSide.CLIENT).getData();
			data.lootModifiers.add(new GLM(LangProvider.GLM_HEADER_RARE.asComponent().withStyle(ChatFormatting.BOLD), modifier.drop, modifier.drop.getCount(),
					modifier.chance, modifier.perLevel, modifier.skill, modifier.getConditions()));
		}

		public static void add(TreasureLootModifier modifier) {
			DataMirror data = (DataMirror) Core.get(LogicalSide.CLIENT).getData();
			data.lootModifiers.add(new GLM(LangProvider.GLM_HEADER_TREASURE.asComponent().withStyle(ChatFormatting.BOLD), modifier.drop.orElse(ItemStack.EMPTY), modifier.count,
					modifier.chance, modifier.perLevel, modifier.skill, modifier.getConditions()));
		}

		public List<Component> getGUILines(Core core) {
			List<Component> linesOut = new ArrayList<>();
			linesOut.add(header);
			Component dropText = drop.is(Items.AIR) ? Component.literal("itself") : drop.getDisplayName();
			linesOut.add(LangProvider.GLM_DROP_ITEM.asComponent(count, dropText));
			double actualChance = chance * (perLevel ? core.getData().getLevel(skill, null) : 1d);
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
					long maxLevel = Math.min(playerSkillCondition.levelMax, Config.skills().skills().getOrDefault(playerSkillCondition.skill, SkillData.Builder.getDefault()).getMaxLevel());
					linesOut.add(LangProvider.GLM_SKILL_RANGE.asComponent(LangProvider.skill(playerSkillCondition.skill),
							playerSkillCondition.levelMin, maxLevel));
				}
				else if (condition instanceof SkillLootConditionHighestSkill highSkillCondition) {
					String skills = highSkillCondition.comparables.stream().map(str -> LangProvider.skill(str).toString()).collect(Collectors.joining(", "));
					linesOut.add(LangProvider.GLM_HIGHEST_SKILL.asComponent(LangProvider.skill(highSkillCondition.targetSkill), skills));
				}
				else if (!(condition instanceof SkillLootConditionKill killCondition)) {
                    if (condition instanceof ValidBlockCondition blockCondition) {
                        Component target = blockCondition.tag.isEmpty()
                                ? blockCondition.block.get().getName()
                                : Component.literal(blockCondition.tag.get().location().toString());
                        linesOut.add(LangProvider.GLM_VALID_BLOCK.asComponent(target));
                    }
                    else otherConditions++;
                } else {
                    long maxLevel = Math.min(killCondition.levelMax, Config.skills().skills().getOrDefault(killCondition.skill, SkillData.Builder.getDefault()).getMaxLevel());
                    linesOut.add(LangProvider.GLM_SKILL_RANGE.asComponent(LangProvider.skill(killCondition.skill),
                            killCondition.levelMin, maxLevel));
                }
            }
			if (otherConditions > 0) linesOut.add(LangProvider.GLM_OTHER_CONDITIONS.asComponent(otherConditions));
			return linesOut;
		}
	}
	public final List<GLM> lootModifiers = new ArrayList<>();
}
