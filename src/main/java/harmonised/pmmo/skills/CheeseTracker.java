package harmonised.pmmo.skills;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;

public class CheeseTracker
{
    private static final Map<UUID, Integer> playersLookCheeseCount = new HashMap<>();
    private static final Map<UUID, Double> playersLastLookVecs = new HashMap<>();

    public static void trackCheese(ServerPlayer player)
    {
        UUID uuid = player.getUUID();
        Vec3 playerLookVec = player.getLookAngle();
        double currLookVec = playerLookVec.x + playerLookVec.y + playerLookVec.z;
        double cheeseMaxStorage = Config.forgeConfig.cheeseMaxStorage.get();
        if(!playersLookCheeseCount.containsKey(uuid))
        {
            playersLookCheeseCount.put(uuid, 0);
            playersLastLookVecs.put(uuid, currLookVec);
        }
        int lookVecCheese = playersLookCheeseCount.get(uuid);

        double lazyMultiplier = getLazyMultiplier(uuid);
        if(playersLastLookVecs.get(uuid) != currLookVec)
        {
            playersLookCheeseCount.put(uuid, Math.max(0, lookVecCheese - Config.forgeConfig.activityCheeseReplenishSpeed.get()));
            if(lazyMultiplier != 1)
                player.displayClientMessage(new TranslatableComponent("pmmo.afkMultiplierRestored", DP.dpSoft(getLazyMultiplier(uuid) * 100)).setStyle(XP.getColorStyle(0x00ff00)), true);
        }
        else
        {
            playersLookCheeseCount.put(uuid, (int) Math.min(cheeseMaxStorage, Math.min(cheeseMaxStorage, lookVecCheese + 1)));
            if(lazyMultiplier < Config.forgeConfig.sendPlayerCheeseWarningBelowMultiplier.get())
                player.displayClientMessage(new TranslatableComponent("pmmo.afkMultiplierWarning", DP.dpSoft(getLazyMultiplier(uuid) * 100)).setStyle(XP.getColorStyle(0xff0000)), true);
        }

        playersLastLookVecs.put(uuid, currLookVec);
    }

    public static double getLazyMultiplier(UUID uuid, String skill)
    {
        if(JsonConfig.localData.get(JType.SKILLS).containsKey(skill) && JsonConfig.localData.get(JType.SKILLS).get(skill).getOrDefault("noAfkPenalty", 0D) != 0)
            return 1;
        else
            return getLazyMultiplier(uuid);
    }

    public static double getLazyMultiplier(UUID uuid)
    {
        if(!playersLookCheeseCount.containsKey(uuid))
            return 1;
        else
        {
            int playerCheese = playersLookCheeseCount.get(uuid);
            return Util.mapCapped(playerCheese - Config.forgeConfig.freeCheese.get(),
                    0,
                    Config.forgeConfig.cheeseMaxStorage.get() - Config.forgeConfig.freeCheese.get(),
                    1,
                    Config.forgeConfig.minimumCheeseXpMultiplier.get());
//            return  Math.max(0, 1 - playerCheese / (double) (cheeseMaxStorage - Config.forgeConfig.freeCheese.get()));
        }
    }
}
