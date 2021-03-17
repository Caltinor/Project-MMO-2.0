package harmonised.pmmo.skills;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class CheeseTracker
{
    private static final Map<UUID, Integer> playersLookCheeseCount = new HashMap<>();
    private static final Map<UUID, Double> playersLastLookVecs = new HashMap<>();

    public static void trackCheese( ServerPlayerEntity player )
    {
        UUID uuid = player.getUniqueID();
        Vector3d playerLookVec = player.getLookVec();
        double currLookVec = playerLookVec.x + playerLookVec.y + playerLookVec.z;
        if( !playersLookCheeseCount.containsKey( uuid ) )
        {
            playersLookCheeseCount.put( uuid, 0 );
            playersLastLookVecs.put( uuid, currLookVec );
        }
        int lookVecCheese = playersLookCheeseCount.get( uuid );

        if( playersLastLookVecs.get( uuid ) != currLookVec )
            playersLookCheeseCount.put( uuid, Math.max( 0, lookVecCheese - Config.forgeConfig.activityCheeseReplenishSpeed.get() ) );
        else
        {
            playersLookCheeseCount.put( uuid, Math.min( Config.forgeConfig.cheeseMaxStorage.get(), lookVecCheese + 1 ) );
            double lazyMultiplier = getLazyMultiplier( uuid );
            if( lazyMultiplier < Config.forgeConfig.sendPlayerCheeseWarningBelowMultiplier.get() )
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.afkMultiplierWarning", DP.dpSoft( lazyMultiplier * 100 ) ).setStyle(XP.getColorStyle( 0xff0000 ) ), true );
        }

        playersLastLookVecs.put( uuid, currLookVec );

        System.out.println( getLazyMultiplier( uuid ) );
    }

    public static double getLazyMultiplier( UUID uuid )
    {
        if( !playersLookCheeseCount.containsKey( uuid ) )
            return 1;
        else
        {
            int playerCheese = playersLookCheeseCount.get( uuid );
            return Util.mapCapped( playerCheese - Config.forgeConfig.freeCheese.get(),
                    0,
                    Config.forgeConfig.cheeseMaxStorage.get() - Config.forgeConfig.freeCheese.get(),
                    1,
                    Config.forgeConfig.minimumCheeseXpMultiplier.get() );
//            return  Math.max( 0, 1 - playerCheese / (double) ( Config.forgeConfig.cheeseMaxStorage.get() - Config.forgeConfig.freeCheese.get() ) );
        }
    }
}
