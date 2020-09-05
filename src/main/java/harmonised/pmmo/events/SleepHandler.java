package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.XP;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;

public class SleepHandler
{
    private static final boolean sleepRechargesAllPlayersVeinCharge = Config.forgeConfig.sleepRechargesAllPlayersVeinCharge.get();
    private static final double maxVeinCharge = Config.forgeConfig.maxVeinCharge.get();

    public static void handleSleepFinished( SleepFinishedTimeEvent event )
    {
        if( sleepRechargesAllPlayersVeinCharge )
        {
            event.getWorld().getWorld().getServer().getPlayerList().getPlayers().forEach( player ->
            {
                XP.getAbilitiesTag( player ).putDouble( "veinLeft", maxVeinCharge );
            });
        }
    }
}
