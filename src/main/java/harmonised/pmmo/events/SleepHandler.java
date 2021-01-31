package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.world.World;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;

public class SleepHandler
{
    public static void handleSleepFinished( SleepFinishedTimeEvent event )
    {
        if( Config.forgeConfig.sleepRechargesAllPlayersVeinCharge.get() )
        {
            ( (World) event.getWorld() ).getServer().getPlayerList().getPlayers().forEach(player ->
            {
                Config.getAbilitiesMap( player ).put( "veinLeft", Config.forgeConfig.maxVeinCharge.get() );
            });
        }
    }
}