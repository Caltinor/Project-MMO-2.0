package harmonised.pmmo.events;


import net.minecraftforge.event.server.ServerStoppingEvent;

public class ServerStoppingHandler
{
    public static void handleServerStop(ServerStoppingEvent event)
    {
        WorldTickHandler.refreshVein();
    }
}
