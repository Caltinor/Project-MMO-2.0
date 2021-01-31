package harmonised.pmmo.events;

import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import harmonised.pmmo.ftb_quests.LevelReward;
import harmonised.pmmo.ftb_quests.SkillTask;
import harmonised.pmmo.ftb_quests.XpReward;
import harmonised.pmmo.util.Reference;
import net.minecraftforge.event.RegistryEvent;

public class RegisterHandler
{
    public static void handleFTBQRegistryTaskType(RegistryEvent.Register<TaskType> event)
    {
        event.getRegistry().register( SkillTask.SKILL = new TaskType( SkillTask::new ).setRegistryName( "skill" ).setIcon( Icon.getIcon( Reference.MOD_ID + ":textures/gui/star.png" ) ) );
    }

    public static void handleFTBQRegistryRewardType(RegistryEvent.Register<RewardType> event)
    {
        event.getRegistry().register( XpReward.XP_REWARD = new RewardType( XpReward::new ).setRegistryName( "xpreward" ).setIcon( Icon.getIcon( Reference.MOD_ID + ":textures/gui/star.png" ) ) );
        event.getRegistry().register( LevelReward.LEVEL_REWARD = new RewardType( LevelReward::new ).setRegistryName( "levelreward" ).setIcon( Icon.getIcon( Reference.MOD_ID + ":textures/gui/star.png" ) ) );
    }
}
