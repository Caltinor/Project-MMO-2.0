package harmonised.pmmo.events;

import com.feed_the_beast.ftblib.lib.icon.Icon;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import harmonised.pmmo.ftb_quests.SkillTask;
import harmonised.pmmo.util.Reference;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;

public class RegisterHandler
{
    public static void handleFTBQRegistry(RegistryEvent.Register<TaskType> event)
    {
        if( Loader.isModLoaded( "ftbquests" ) )
            event.getRegistry().register( SkillTask.SKILL = new TaskType( SkillTask::new ).setRegistryName( "skill" ).setIcon( Icon.getIcon( Reference.MOD_ID + ":textures/gui/star.png" ) ) );
    }
}