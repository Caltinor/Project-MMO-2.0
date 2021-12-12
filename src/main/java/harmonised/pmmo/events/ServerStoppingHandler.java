package harmonised.pmmo.events;

import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public class ServerStoppingHandler
{
    public static void handleServerStop(FMLServerStoppingEvent event)
    {
        WorldTickHandler.refreshVein();
    }
}
