package harmonised.pmmo.events;

import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;

public class ServerStoppingHandler
{
    public static void handleServerStop( FMLServerStoppingEvent event )
    {
        WorldTickHandler.refreshVein();
    }
}
