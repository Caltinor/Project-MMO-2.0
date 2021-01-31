package harmonised.pmmo.events;

import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

public class ServerStoppingHandler
{
    public static void handleServerStop( FMLServerStoppingEvent event )
    {
        WorldTickHandler.refreshVein();
    }
}
