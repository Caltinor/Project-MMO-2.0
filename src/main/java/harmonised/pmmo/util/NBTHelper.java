package harmonised.pmmo.util;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NBTHelper
{
    public static Map<String, Double> nbtToMapString( NBTTagCompound nbt )
    {
        Map<String, Double> map = new HashMap<>();

        for( String key : nbt.getKeySet() )
        {
            map.put( key, nbt.getDouble( key ) );
        }

        return map;
    }

    public static Map<Skill, Double> nbtToMapSkill( NBTTagCompound nbt )
    {
        Map<Skill, Double> map = new HashMap<>();

        for( String key : nbt.getKeySet() )
        {
            if( Skill.getSkill( key ) != Skill.INVALID_SKILL )
                map.put( Skill.getSkill( key ), nbt.getDouble( key ) );
        }

        return map;
    }

    public static NBTTagCompound mapStringToNbt(Map<String, Double> map )
    {
        if( map == null )
            return new NBTTagCompound();

        NBTTagCompound nbt = new NBTTagCompound();

        for( Map.Entry<String, Double> entry : map.entrySet() )
        {
            nbt.setDouble( entry.getKey(), entry.getValue() );
        }

        return nbt;
    }

    public static NBTTagCompound mapStringMapSkillToNbt( Map<String, Map<Skill, Double>> map )
    {
        NBTTagCompound nbt = new NBTTagCompound();

        for( Map.Entry<String, Map<Skill, Double>> entry : map.entrySet() )
        {
            nbt.setTag( entry.getKey(), mapSkillToNbt( entry.getValue() ) );
        }

        return nbt;
    }

    public static NBTTagCompound mapSkillToNbt( Map<Skill, Double> map )
    {
        NBTTagCompound nbt = new NBTTagCompound();

        for( Map.Entry<Skill, Double> entry : map.entrySet() )
        {
            nbt.setDouble( entry.getKey().toString(), entry.getValue() );
        }

        return nbt;
    }

    public static NBTTagCompound mapUuidSkillToNbt( Map<UUID, Map<Skill, Double>> inMap )
    {
        NBTTagCompound outData = new NBTTagCompound();

        for( Map.Entry<UUID, Map<Skill, Double>> entry : inMap.entrySet() )
        {
            outData.setTag( entry.getKey().toString(), mapSkillToNbt( entry.getValue() ) );
        }

        return outData;
    }

    public static NBTTagCompound mapUuidStringToNbt( Map<UUID, Map<String, Double>> inMap )
    {
        NBTTagCompound outData = new NBTTagCompound();
        NBTTagCompound innerData;

        for( Map.Entry<UUID, Map<String, Double>> entry : inMap.entrySet() )
        {
            outData.setTag( entry.getKey().toString(), mapStringToNbt( entry.getValue() ) );
        }

        return outData;
    }

    public static Map<UUID, Map<String, Double>> nbtToMapUuidString( NBTTagCompound inData )
    {
        Map<UUID, Map<String, Double>> outMap = new HashMap<>();
        for( String uuidKey : inData.getKeySet() )
        {
            outMap.put( UUID.fromString( uuidKey ), nbtToMapString( inData.getCompoundTag( uuidKey ) ) );
        }

        return outMap;
    }

    public static Map<UUID, Map<Skill, Double>> nbtToMapUuidSkill( NBTTagCompound inData )
    {
        Map<UUID, Map<Skill, Double>> outMap = new HashMap<>();

        for( String uuidKey : inData.getKeySet() )
        {
            outMap.put( UUID.fromString( uuidKey ), nbtToMapSkill( inData.getCompoundTag( uuidKey ) ) );
        }

        return outMap;
    }

    public static Map<String, Map<Skill, Double>> nbtToMapStringSkill( NBTTagCompound inData )
    {
        Map<String, Map<Skill, Double>> outMap = new HashMap<>();

        for( String key : inData.getKeySet() )
        {
            outMap.put( key, nbtToMapSkill( inData.getCompoundTag( key ) ) );
        }

        return outMap;
    }

    public static Map<UUID, Map<String, Map<Skill, Double>>> nbtToMapStringMapUuidSkill(NBTTagCompound inData )
    {
        Map<UUID, Map<String, Map<Skill, Double>>> outMap = new HashMap<>();

        for( String playerUUIDKey : inData.getKeySet() )
        {
            UUID playerUUID = UUID.fromString( playerUUIDKey );
            outMap.put( playerUUID, new HashMap<>() );

            for( String xpBoostKey : inData.getCompoundTag( playerUUIDKey ).getKeySet() )
            {
                outMap.get( playerUUID ).put( xpBoostKey, nbtToMapSkill( inData.getCompoundTag( playerUUIDKey ).getCompoundTag( xpBoostKey ) ) );
            }
        }

        return outMap;
    }

    public static NBTTagCompound mapStringNbtToNbt( Map<String, NBTTagCompound> inMap )
    {
        NBTTagCompound outNbt = new NBTTagCompound();

        for( Map.Entry<String, NBTTagCompound> entry : inMap.entrySet() )
        {
            outNbt.setTag( entry.getKey(), entry.getValue() );
        }

        return outNbt;
    }

    public static NBTTagCompound extractNbtPlayersIndividualTagsFromPlayersTag( NBTTagCompound playersTag, String element )
    {
        NBTTagCompound outData = new NBTTagCompound();
        NBTTagCompound playerTag;

        for( String uuidKey : playersTag.getKeySet() )
        {
            playerTag = playersTag.getCompoundTag( uuidKey );
            if( playerTag.hasKey( element ) )
                outData.setTag( uuidKey, playerTag.getCompoundTag( element ) );
        }

        return outData;
    }

    public static Map<UUID, Map<Skill, Double>> nbtToXpMaps( NBTTagCompound input )
    {
        Map<UUID, Map<Skill, Double>> output = new HashMap<>();
        UUID uuid;
        for( String key1 : input.getKeySet() )
        {
            uuid = UUID.fromString( key1 );
            output.put( uuid, new HashMap<>() );
            for( String key2 : input.getCompoundTag( key1 ).getKeySet() )
            {
                output.get( uuid ).put( Skill.getSkill( key2 ), input.getCompoundTag( key1 ).getDouble( key2 ) );
            }
        }

        return output;
    }

    public static NBTTagCompound xpMapsToNbt( Map<UUID, Map<Skill, Double>> input )
    {
        NBTTagCompound output = new NBTTagCompound();
        String key2;

        for( UUID key1 : input.keySet() )
        {
            output.setTag( key1.toString(), new NBTTagCompound() );
            output.getCompoundTag( key1.toString() ).setString( "name", PmmoSavedData.get().getName( key1 ) );
            for( Skill skill : input.get( key1 ).keySet() )
            {
                key2 = skill.toString();
                Double value = input.get( key1 ).get( skill );
                output.getCompoundTag( key1.toString() ).setDouble( key2, value );
            }
        }

        return output;
    }

    public static Map<JType, Map<String, Map<String, Double>>> nbtToData3( NBTTagCompound input )
    {
        Map<JType, Map<String, Map<String, Double>>> output = new HashMap<>();
        JType jType;
        for( String jTypeKey : input.getKeySet() )
        {
            jType = JType.getJType( jTypeKey );
            output.put( jType, new HashMap<>() );
            for( String topKey : input.getCompoundTag( jTypeKey ).getKeySet() )
            {
                output.get( jType ).put( topKey, new HashMap<>() );
                for( String botKey : input.getCompoundTag( jTypeKey ).getCompoundTag( topKey ).getKeySet() )
                {
                    output.get( jType ).get( topKey ).put( botKey, input.getCompoundTag( jTypeKey ).getCompoundTag( topKey ).getDouble( botKey ) );
                }
            }
        }

        return output;
    }

    public static NBTTagCompound data3ToNbt( Map<JType, Map<String, Map<String, Double>>> input )
    {
        NBTTagCompound output = new NBTTagCompound();

        for( JType jType : input.keySet() )
        {
            output.setTag( jType.toString(), new NBTTagCompound() );
            for( String topKey : input.get( jType ).keySet() )
            {
                output.getCompoundTag( jType.toString() ).setTag( topKey, new NBTTagCompound() );
                for( String botKey : input.get( jType ).get( topKey ).keySet() )
                {
                    Double value = input.get( jType ).get( topKey ).get( botKey );

                    output.getCompoundTag( jType.toString() ).getCompoundTag( topKey ).setDouble( botKey, value );
                }
            }
        }

        return output;
    }

    public static void addData3( Map<JType, Map<String, Map<String, Double>>> input1, Map<JType, Map<String, Map<String, Double>>> input2 )
    {
        for( Map.Entry<JType, Map<String, Map<String, Double>>> entry3 : input2.entrySet() )
        {
            if( !input1.containsKey( entry3.getKey() ) )
                input1.put( entry3.getKey(), new HashMap<>() );

            for( Map.Entry<String, Map<String, Double>> entry2 : entry3.getValue().entrySet() )
            {
                if( !input1.get( entry3.getKey() ).containsKey( entry2.getKey() ) )
                    input1.get( entry3.getKey() ).put( entry2.getKey(), new HashMap<>() );

                for( Map.Entry<String, Double> entry1 : entry2.getValue().entrySet() )
                {
                    if( !input1.get( entry3.getKey() ).get( entry2.getKey() ).containsKey( entry1.getKey() ) )
                        input1.get( entry3.getKey() ).get( entry2.getKey() ).put( entry1.getKey(), entry1.getValue() );
                }
            }
        }
    }

    public static Map<JType, Map<String, Map<String, Map<String, Double>>>> nbtToData4( NBTTagCompound input )
    {
        Map<JType, Map<String, Map<String, Map<String, Double>>>> output = new HashMap<>();
        JType jType;
        for( String jTypeKey : input.getKeySet() )
        {
            jType = JType.getJType( jTypeKey );
            output.put( jType, new HashMap<>() );
            for( String topKey : input.getCompoundTag( jTypeKey ).getKeySet() )
            {
                output.get( jType ).put( topKey, new HashMap<>() );
                for( String midKey : input.getCompoundTag( jTypeKey ).getCompoundTag( topKey ).getKeySet() )
                {
                    output.get( jType ).get( topKey ).put( midKey, new HashMap<>() );
                    for( String botKey : input.getCompoundTag( jTypeKey ).getCompoundTag( topKey ).getCompoundTag( midKey ).getKeySet() )
                    {
                        output.get( jType ).get( topKey ).get( midKey ).put( botKey, input.getCompoundTag( jTypeKey ).getCompoundTag( topKey ).getCompoundTag( midKey ).getDouble( botKey ) );
                    }
                }
            }
        }

        return output;
    }

    public static NBTTagCompound data4ToNbt( Map<JType, Map<String, Map<String, Map<String, Double>>>> input )
    {
        NBTTagCompound output = new NBTTagCompound();

        for( JType jType : input.keySet() )
        {
            output.setTag( jType.toString(), new NBTTagCompound() );
            for( String topKey : input.get( jType ).keySet() )
            {
                output.getCompoundTag( jType.toString() ).setTag( topKey, new NBTTagCompound() );
                for( String midKey : input.get( jType ).get( topKey ).keySet() )
                {
                    output.getCompoundTag( jType.toString() ).getCompoundTag( topKey ).setTag( midKey, new NBTTagCompound() );
                    for( String botKey : input.get( jType ).get( topKey ).get( midKey ).keySet() )
                    {
                        Double value = input.get( jType ).get( topKey ).get( midKey ).get( botKey );
                        output.getCompoundTag( jType.toString() ).getCompoundTag( topKey ).getCompoundTag( midKey ).setDouble( botKey, value );
                    }
                }
            }
        }

        return output;
    }

    public static void addData4( Map<JType, Map<String, Map<String, Map<String, Double>>>> input1, Map<JType, Map<String, Map<String, Map<String, Double>>>> input2 )
    {
        for( Map.Entry<JType, Map<String, Map<String, Map<String, Double>>>> entry4 : input2.entrySet() )
        {
            if( !input1.containsKey( entry4.getKey() ) )
                input1.put( entry4.getKey(), new HashMap<>() );

            for( Map.Entry<String, Map<String, Map<String, Double>>> entry3 : entry4.getValue().entrySet() )
            {
                if( !input1.get( entry4.getKey() ).containsKey( entry3.getKey() ) )
                    input1.get( entry4.getKey() ).put( entry3.getKey(), new HashMap<>() );

                for( Map.Entry<String, Map<String, Double>> entry2 : entry3.getValue().entrySet() )
                {
                    if( !input1.get( entry4.getKey() ).get( entry3.getKey() ).containsKey( entry2.getKey() ) )
                        input1.get( entry4.getKey() ).get( entry3.getKey() ).put( entry2.getKey(), entry2.getValue() );
                }
            }
        }
    }
}