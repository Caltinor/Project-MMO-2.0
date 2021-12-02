package harmonised.pmmo.proxy;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.AttributeHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void updateNBTTag(MessageUpdatePlayerNBT packet, Player player)
    {
        CompoundTag newPackage = packet.reqPackage;
        Set<String> keySet = new HashSet<>(newPackage.getAllKeys());

        switch(packet.type)
        {
            case 0:
                Map<String, Double> prefsMap = Config.getPreferencesMap(player);
                for(String tag : keySet)
                {
                    prefsMap.remove(tag);
                }
                for(String tag : keySet)
                {
                    prefsMap.put(tag, newPackage.getDouble(tag));
                }
                AttributeHandler.updateAll(player);
                PmmoSavedData.get().setDirty(true);
                break;

            default:
                LOGGER.error("ERROR MessageUpdateNBT WRONG TYPE", packet);
                break;
        }
    }
}
