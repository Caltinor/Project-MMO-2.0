package harmonised.pmmo.events;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import harmonised.pmmo.ftb_quests.SkillTask;
import net.minecraftforge.event.RegistryEvent;

public class RegisterHandler
{
    public static void handleFTBQRegistry(RegistryEvent.Register<TaskType> event)
    {
        event.getRegistry().register( SkillTask.SKILL = new TaskType( SkillTask::new ).setRegistryName( "skill" ).setIcon( Icon.getIcon("minecraft:item/wooden_pickaxe") ) );
    }
}