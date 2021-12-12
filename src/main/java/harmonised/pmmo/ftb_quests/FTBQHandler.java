package harmonised.pmmo.ftb_quests;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import harmonised.pmmo.util.Reference;
import net.minecraft.util.ResourceLocation;

public class FTBQHandler
{
    public static TaskType SKILL = TaskTypes.register(new ResourceLocation(Reference.MOD_ID, "skill"), SkillTask::new, () -> Icon.getIcon(Reference.MOD_ID + ":textures/gui/star.png"));
    public static RewardType XP_REWARD = RewardTypes.register(new ResourceLocation(Reference.MOD_ID, "xpreward"), XpReward::new, () -> Icon.getIcon(Reference.MOD_ID + ":textures/gui/star.png"));
    public static RewardType LEVEL_REWARD = RewardTypes.register(new ResourceLocation(Reference.MOD_ID, "levelreward"), LevelReward::new, () -> Icon.getIcon(Reference.MOD_ID + ":textures/gui/star.png"));

    public static void init()
    {

    }
}
