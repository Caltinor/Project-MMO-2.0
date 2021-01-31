package harmonised.pmmo.events;

import harmonised.pmmo.util.Reference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerCloneHandler
{
    public static void handleClone( net.minecraftforge.event.entity.player.PlayerEvent.Clone event )
    {
        event.getEntityPlayer().getEntityData().getCompoundTag( EntityPlayer.PERSISTED_NBT_TAG ).setTag( Reference.MOD_ID, event.getOriginal().getEntityData().getCompoundTag( Reference.MOD_ID ) );
    }
}