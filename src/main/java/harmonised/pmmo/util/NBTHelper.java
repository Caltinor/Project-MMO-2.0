package harmonised.pmmo.util;

import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;

public class NBTHelper
{
    public static Map<String, Double> nbtToMap( CompoundNBT nbt )
    {
        Map<String, Double> map = new HashMap<>();

        for( String key : nbt.keySet() )
        {
            map.put( key, nbt.getDouble( key ) );
        }

        return map;
    }

    public static CompoundNBT mapToNBT( Map<String, Double> map )
    {
        CompoundNBT nbt = new CompoundNBT();

        for( Map.Entry<String, Double> entry : map.entrySet() )
        {
            nbt.putDouble( entry.getKey(), entry.getValue() );
        }

        return nbt;
    }
}
