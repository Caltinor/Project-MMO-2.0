package harmonised.pmmo.events;

import harmonised.pmmo.util.Reference;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerCloneHandler
{
    public static void handleClone( PlayerEvent.Clone event )
    {
        event.getPlayer().getPersistentData().setTag( Reference.MOD_ID, event.getOriginal().getPersistentData().getCompoundTag( Reference.MOD_ID ) );
    }
}
