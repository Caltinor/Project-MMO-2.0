package harmonised.pmmo.util;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NBTHelper
{
    public static <T> Map<T, Double> maxDoubleMaps(Map<T, Double> mapA, Map<T, Double> mapB)
    {
        for(Map.Entry<T, Double> entry : mapB.entrySet())
        {
            mapA.put(entry.getKey(), Math.max(entry.getValue(), mapA.getOrDefault(entry.getKey(), entry.getValue())));
        }
        return mapA;
    }

    public static <T> Map<String, T> stringMapToLowerCase(Map<String, T> inMap)
    {
        Map<String, T> outMap = new HashMap<>();
        for(Map.Entry<String, T> entry : inMap.entrySet())
        {
            outMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return outMap;
    }

    public static <T> Map<String, T> mapStringKeyToString(Map<String, T> inMap)
    {
        Map<String, T> outMap = new HashMap<>();

        for(Map.Entry<String, T> entry : inMap.entrySet())
        {
            outMap.put(entry.getKey(), entry.getValue());
        }

        return outMap;
    }

    public static Map<String, Double> nbtToMapString(CompoundNBT nbt)
    {
        Map<String, Double> map = new HashMap<>();

        for(String key : nbt.keySet())
        {
            if(!Double.isNaN(nbt.getDouble(key)))
                map.put(key, nbt.getDouble(key));
        }

        return map;
    }

    public static CompoundNBT mapStringToNbt(Map<String, Double> map)
    {
        if(map == null)
            return new CompoundNBT();

        CompoundNBT nbt = new CompoundNBT();

        for(Map.Entry<String, Double> entry : map.entrySet())
        {
            nbt.putDouble(entry.getKey(), entry.getValue());
        }

        return nbt;
    }

    public static CompoundNBT mapStringMapStringToNbt(Map<String, Map<String, Double>> map)
    {
        CompoundNBT nbt = new CompoundNBT();

        for(Map.Entry<String, Map<String, Double>> entry : map.entrySet())
        {
            nbt.put(entry.getKey(), mapStringToNbt(entry.getValue()));
        }

        return nbt;
    }

    public static CompoundNBT mapUuidStringToNbt(Map<UUID, Map<String, Double>> inMap)
    {
        CompoundNBT outData = new CompoundNBT();
        CompoundNBT innerData;

        for(Map.Entry<UUID, Map<String, Double>> entry : inMap.entrySet())
        {
            outData.put(entry.getKey().toString(), mapStringToNbt(entry.getValue()));
        }

        return outData;
    }

    public static Map<UUID, Map<String, Double>> nbtToMapUuidString(CompoundNBT inData)
    {
        Map<UUID, Map<String, Double>> outMap = new HashMap<>();
        for(String uuidKey : inData.keySet())
        {
            outMap.put(UUID.fromString(uuidKey), nbtToMapString(inData.getCompound(uuidKey)));
        }

        return outMap;
    }

    public static Map<String, Map<String, Double>> nbtToMapStringString(CompoundNBT inData)
    {
        Map<String, Map<String, Double>> outMap = new HashMap<>();

        for(String key : inData.keySet())
        {
            outMap.put(key, nbtToMapString(inData.getCompound(key)));
        }

        return outMap;
    }

    public static Map<UUID, Map<String, Map<String, Double>>> nbtToMapStringMapUuidString(CompoundNBT inData)
    {
        Map<UUID, Map<String, Map<String, Double>>> outMap = new HashMap<>();

        for(String playerUUIDKey : inData.keySet())
        {
            UUID playerUUID = UUID.fromString(playerUUIDKey);
            outMap.put(playerUUID, new HashMap<>());

            for(String xpBoostKey : inData.getCompound(playerUUIDKey).keySet())
            {
                outMap.get(playerUUID).put(xpBoostKey, nbtToMapString(inData.getCompound(playerUUIDKey).getCompound(xpBoostKey)));
            }
        }

        return outMap;
    }

    public static CompoundNBT mapStringNbtToNbt(Map<String, CompoundNBT> inMap)
    {
        CompoundNBT outNbt = new CompoundNBT();

        for(Map.Entry<String, CompoundNBT> entry : inMap.entrySet())
        {
            outNbt.put(entry.getKey(), entry.getValue());
        }

        return outNbt;
    }

    public static CompoundNBT extractNbtPlayersIndividualTagsFromPlayersTag(CompoundNBT playersTag, String element)
    {
        CompoundNBT outData = new CompoundNBT();
        CompoundNBT playerTag;

        for(String uuidKey : playersTag.keySet())
        {
            playerTag = playersTag.getCompound(uuidKey);
            if(playerTag.contains(element))
                outData.put(uuidKey, playerTag.getCompound(element));
        }

        return outData;
    }

    public static Map<UUID, Map<String, Double>> nbtToXpMaps(CompoundNBT input)
    {
        Map<UUID, Map<String, Double>> output = new HashMap<>();
        UUID uuid;
        for(String key1 : input.keySet())
        {
            uuid = UUID.fromString(key1);
            output.put(uuid, new HashMap<>());
            for(String key2 : input.getCompound(key1).keySet())
            {
                output.get(uuid).put(key2, input.getCompound(key1).getDouble(key2));
            }
        }

        return output;
    }

    public static CompoundNBT xpMapsToNbt(Map<UUID, Map<String, Double>> input)
    {
        CompoundNBT output = new CompoundNBT();

        for(UUID key1 : input.keySet())
        {
            output.put(key1.toString(), new CompoundNBT());
            output.getCompound(key1.toString()).putString("name", PmmoSavedData.get().getName(key1));
            for(String skill : input.get(key1).keySet())
            {
                Double value = input.get(key1).get(skill);
                output.getCompound(key1.toString()).putDouble(skill, value);
            }
        }

        return output;
    }

    public static Map<JType, Map<String, Map<String, Double>>> nbtToData3(CompoundNBT input)
    {
        Map<JType, Map<String, Map<String, Double>>> output = new HashMap<>();
        JType jType;
        for(String jTypeKey : input.keySet())
        {
            jType = JType.getJType(jTypeKey);
            output.put(jType, new HashMap<>());
            for(String topKey : input.getCompound(jTypeKey).keySet())
            {
                output.get(jType).put(topKey, new HashMap<>());
                for(String botKey : input.getCompound(jTypeKey).getCompound(topKey).keySet())
                {
                    output.get(jType).get(topKey).put(botKey, input.getCompound(jTypeKey).getCompound(topKey).getDouble(botKey));
                }
            }
        }

        return output;
    }

    public static CompoundNBT data3ToNbt(Map<JType, Map<String, Map<String, Double>>> input)
    {
        CompoundNBT output = new CompoundNBT();

        for(JType jType : input.keySet())
        {
            output.put(jType.toString(), new CompoundNBT());
            for(String topKey : input.get(jType).keySet())
            {
                output.getCompound(jType.toString()).put(topKey, new CompoundNBT());
                for(String botKey : input.get(jType).get(topKey).keySet())
                {
                    Double value = input.get(jType).get(topKey).get(botKey);

                    output.getCompound(jType.toString()).getCompound(topKey).putDouble(botKey, value);
                }
            }
        }

        return output;
    }

    public static void addData3(Map<JType, Map<String, Map<String, Double>>> input1, Map<JType, Map<String, Map<String, Double>>> input2)
    {
        for(Map.Entry<JType, Map<String, Map<String, Double>>> entry3 : input2.entrySet())
        {
            if(!input1.containsKey(entry3.getKey()))
                input1.put(entry3.getKey(), new HashMap<>());

            for(Map.Entry<String, Map<String, Double>> entry2 : entry3.getValue().entrySet())
            {
                if(!input1.get(entry3.getKey()).containsKey(entry2.getKey()))
                    input1.get(entry3.getKey()).put(entry2.getKey(), new HashMap<>());

                for(Map.Entry<String, Double> entry1 : entry2.getValue().entrySet())
                {
                    if(!input1.get(entry3.getKey()).get(entry2.getKey()).containsKey(entry1.getKey()))
                        input1.get(entry3.getKey()).get(entry2.getKey()).put(entry1.getKey(), entry1.getValue());
                }
            }
        }
    }

    public static Map<JType, Map<String, Map<String, Map<String, Double>>>> nbtToData4(CompoundNBT input)
    {
        Map<JType, Map<String, Map<String, Map<String, Double>>>> output = new HashMap<>();
        JType jType;
        for(String jTypeKey : input.keySet())
        {
            jType = JType.getJType(jTypeKey);
            output.put(jType, new HashMap<>());
            for(String topKey : input.getCompound(jTypeKey).keySet())
            {
                output.get(jType).put(topKey, new HashMap<>());
                for(String midKey : input.getCompound(jTypeKey).getCompound(topKey).keySet())
                {
                    output.get(jType).get(topKey).put(midKey, new HashMap<>());
                    for(String botKey : input.getCompound(jTypeKey).getCompound(topKey).getCompound(midKey).keySet())
                    {
                        output.get(jType).get(topKey).get(midKey).put(botKey, input.getCompound(jTypeKey).getCompound(topKey).getCompound(midKey).getDouble(botKey));
                    }
                }
            }
        }

        return output;
    }

    public static CompoundNBT data4ToNbt(Map<JType, Map<String, Map<String, Map<String, Double>>>> input)
    {
        CompoundNBT output = new CompoundNBT();

        for(JType jType : input.keySet())
        {
            output.put(jType.toString(), new CompoundNBT());
            for(String topKey : input.get(jType).keySet())
            {
                output.getCompound(jType.toString()).put(topKey, new CompoundNBT());
                for(String midKey : input.get(jType).get(topKey).keySet())
                {
                    output.getCompound(jType.toString()).getCompound(topKey).put(midKey, new CompoundNBT());
                    for(String botKey : input.get(jType).get(topKey).get(midKey).keySet())
                    {
                        Double value = input.get(jType).get(topKey).get(midKey).get(botKey);
                        output.getCompound(jType.toString()).getCompound(topKey).getCompound(midKey).putDouble(botKey, value);
                    }
                }
            }
        }

        return output;
    }

    public static void addData4(Map<JType, Map<String, Map<String, Map<String, Double>>>> input1, Map<JType, Map<String, Map<String, Map<String, Double>>>> input2)
    {
        for(Map.Entry<JType, Map<String, Map<String, Map<String, Double>>>> entry4 : input2.entrySet())
        {
            if(!input1.containsKey(entry4.getKey()))
                input1.put(entry4.getKey(), new HashMap<>());

            for(Map.Entry<String, Map<String, Map<String, Double>>> entry3 : entry4.getValue().entrySet())
            {
                if(!input1.get(entry4.getKey()).containsKey(entry3.getKey()))
                    input1.get(entry4.getKey()).put(entry3.getKey(), new HashMap<>());

                for(Map.Entry<String, Map<String, Double>> entry2 : entry3.getValue().entrySet())
                {
                    if(!input1.get(entry4.getKey()).get(entry3.getKey()).containsKey(entry2.getKey()))
                        input1.get(entry4.getKey()).get(entry3.getKey()).put(entry2.getKey(), entry2.getValue());
                }
            }
        }
    }

    public static float getOrDefaultFromNBT(CompoundNBT nbt, String key, float defaultValue)
    {
        return nbt.contains(key) ? nbt.getFloat(key) : defaultValue;
    }

    public static double getOrDefaultFromNBT(CompoundNBT nbt, String key, double defaultValue)
    {
        try
        {
            return nbt.contains(key) ? nbt.getDouble(key) : defaultValue;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static long getOrDefaultFromNBT(CompoundNBT nbt, String key, long defaultValue)
    {
        return nbt.contains(key) ? nbt.getLong(key) : defaultValue;
    }

    public static int getOrDefaultFromNBT(CompoundNBT nbt, String key, int defaultValue)
    {
        return nbt.contains(key) ? nbt.getInt(key) : defaultValue;
    }

    public static String getOrDefaultFromNBT(CompoundNBT nbt, String key, String defaultValue)
    {
        return nbt.contains(key) ? nbt.getString(key) : defaultValue;
    }

    public static boolean getOrDefaultFromNBT(CompoundNBT nbt, String key, boolean defaultValue)
    {
        return nbt.contains(key) ? nbt.getBoolean(key) : defaultValue;
    }

    public static byte getOrDefaultFromNBT(CompoundNBT nbt, String key, byte defaultValue)
    {
        return nbt.contains(key) ? nbt.getByte(key) : defaultValue;
    }
}