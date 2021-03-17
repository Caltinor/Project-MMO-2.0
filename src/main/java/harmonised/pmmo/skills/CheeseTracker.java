package harmonised.pmmo.skills;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;

public class CheeseTracker
{
    private static final Map<UUID, Integer> playersLookCheeseCount = new HashMap<>();
    private static final Map<UUID, Double> playersLastLookVecs = new HashMap<>();

    private static final int cheeseMaxStorage = 120;
    private static final int freeCheese = 20;

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
            playersLookCheeseCount.put( uuid, Math.max( 0, lookVecCheese - 2 ) );
        else
            playersLookCheeseCount.put( uuid, Math.min( cheeseMaxStorage, lookVecCheese + 1 ) );

        playersLastLookVecs.put( uuid, currLookVec );

//        System.out.println( getLazyMultiplier( uuid ) );
    }

    public static double getLazyMultiplier( UUID uuid )
    {
        if( !playersLookCheeseCount.containsKey( uuid ) )
            return 1;
        else
        {
            int playerCheese = playersLookCheeseCount.get( uuid );
            return  Math.max( 0, 1 - playerCheese / (double) ( cheeseMaxStorage - freeCheese ) );
        }
    }
}
