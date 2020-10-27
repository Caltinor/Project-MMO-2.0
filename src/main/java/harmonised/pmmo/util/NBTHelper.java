package harmonised.pmmo.util;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NBTHelper
{
    public static Map<String, Double> nbtToMapString( CompoundNBT nbt )
    {
        Map<String, Double> map = new HashMap<>();

        for( String key : nbt.keySet() )
        {
            map.put( key, nbt.getDouble( key ) );
        }

        return map;
    }

    public static Map<Skill, Double> nbtToMapSkill( CompoundNBT nbt )
    {
        Map<Skill, Double> map = new HashMap<>();

        for( String key : nbt.keySet() )
        {
            if( Skill.getSkill( key ) != Skill.INVALID_SKILL )
                map.put( Skill.getSkill( key ), nbt.getDouble( key ) );
        }

        return map;
    }

    public static CompoundNBT mapStringToNbt(Map<String, Double> map )
    {
        if( map == null )
            return new CompoundNBT();

        CompoundNBT nbt = new CompoundNBT();

        for( Map.Entry<String, Double> entry : map.entrySet() )
        {
            nbt.putDouble( entry.getKey(), entry.getValue() );
        }

        return nbt;
    }

    public static CompoundNBT mapStringMapSkillToNbt( Map<String, Map<Skill, Double>> map )
    {
        CompoundNBT nbt = new CompoundNBT();

        for( Map.Entry<String, Map<Skill, Double>> entry : map.entrySet() )
        {
            nbt.put( entry.getKey(), mapSkillToNbt( entry.getValue() ) );
        }

        return nbt;
    }

    public static CompoundNBT mapSkillToNbt( Map<Skill, Double> map )
    {
        CompoundNBT nbt = new CompoundNBT();

        for( Map.Entry<Skill, Double> entry : map.entrySet() )
        {
            nbt.putDouble( entry.getKey().toString(), entry.getValue() );
        }

        return nbt;
    }

    public static CompoundNBT mapUuidSkillToNbt( Map<UUID, Map<Skill, Double>> inMap )
    {
        CompoundNBT outData = new CompoundNBT();

        for( Map.Entry<UUID, Map<Skill, Double>> entry : inMap.entrySet() )
        {
            outData.put( entry.getKey().toString(), mapSkillToNbt( entry.getValue() ) );
        }

        return outData;
    }

    public static CompoundNBT mapUuidStringToNbt( Map<UUID, Map<String, Double>> inMap )
    {
        CompoundNBT outData = new CompoundNBT();
        CompoundNBT innerData;

        for( Map.Entry<UUID, Map<String, Double>> entry : inMap.entrySet() )
        {
            outData.put( entry.getKey().toString(), mapStringToNbt( entry.getValue() ) );
        }

        return outData;
    }

    public static Map<UUID, Map<String, Double>> nbtToMapUuidString( CompoundNBT inData )
    {
        Map<UUID, Map<String, Double>> outMap = new HashMap<>();
        for( String uuidKey : inData.keySet() )
        {
            outMap.put( UUID.fromString( uuidKey ), nbtToMapString( inData.getCompound( uuidKey ) ) );
        }

        return outMap;
    }

    public static Map<UUID, Map<Skill, Double>> nbtToMapUuidSkill( CompoundNBT inData )
    {
        Map<UUID, Map<Skill, Double>> outMap = new HashMap<>();

        for( String uuidKey : inData.keySet() )
        {
            outMap.put( UUID.fromString( uuidKey ), nbtToMapSkill( inData.getCompound( uuidKey ) ) );
        }

        return outMap;
    }

    public static Map<String, Map<Skill, Double>> nbtToMapStringSkill( CompoundNBT inData )
    {
        Map<String, Map<Skill, Double>> outMap = new HashMap<>();

        for( String key : inData.keySet() )
        {
            outMap.put( key, nbtToMapSkill( inData.getCompound( key ) ) );
        }

        return outMap;
    }

    public static Map<UUID, Map<String, Map<Skill, Double>>> nbtToMapStringMapUuidSkill(CompoundNBT inData )
    {
        Map<UUID, Map<String, Map<Skill, Double>>> outMap = new HashMap<>();

        for( String playerUUIDKey : inData.keySet() )
        {
            UUID playerUUID = UUID.fromString( playerUUIDKey );
            outMap.put( playerUUID, new HashMap<>() );

            for( String xpBoostKey : inData.getCompound( playerUUIDKey ).keySet() )
            {
                outMap.get( playerUUID ).put( xpBoostKey, nbtToMapSkill( inData.getCompound( playerUUIDKey ).getCompound( xpBoostKey ) ) );
            }
        }

        return outMap;
    }

    public static CompoundNBT mapStringNbtToNbt( Map<String, CompoundNBT> inMap )
    {
        CompoundNBT outNbt = new CompoundNBT();

        for( Map.Entry<String, CompoundNBT> entry : inMap.entrySet() )
        {
            outNbt.put( entry.getKey(), entry.getValue() );
        }

        return outNbt;
    }

    public static CompoundNBT extractNbtPlayersIndividualTagsFromPlayersTag( CompoundNBT playersTag, String element )
    {
        CompoundNBT outData = new CompoundNBT();
        CompoundNBT playerTag;

        for( String uuidKey : playersTag.keySet() )
        {
            playerTag = playersTag.getCompound( uuidKey );
            if( playerTag.contains( element ) )
                outData.put( uuidKey, playerTag.getCompound( element ) );
        }

        return outData;
    }

    public static Map<JType, Map<String, Map<String, Double>>> nbtToData3( CompoundNBT input )
    {
        Map<JType, Map<String, Map<String, Double>>> output = new HashMap<>();
        JType jType;
        for( String jTypeKey : input.keySet() )
        {
            jType = JType.getJType( jTypeKey );
            output.put( jType, new HashMap<>() );
            for( String topKey : input.getCompound( jTypeKey ).keySet() )
            {
                output.get( jType ).put( topKey, new HashMap<>() );
                for( String botKey : input.getCompound( jTypeKey ).getCompound( topKey ).keySet() )
                {
                    output.get( jType ).get( topKey ).put( botKey, input.getCompound( jTypeKey ).getCompound( topKey ).getDouble( botKey ) );
                }
            }
        }

        return output;
    }

    public static CompoundNBT data3ToNbt( Map<JType, Map<String, Map<String, Double>>> input )
    {
        CompoundNBT output = new CompoundNBT();

        for( JType jType : input.keySet() )
        {
            output.put( jType.toString(), new CompoundNBT() );
            for( String topKey : input.get( jType ).keySet() )
            {
                output.getCompound( jType.toString() ).put( topKey, new CompoundNBT() );
                for( String botKey : input.get( jType ).get( topKey ).keySet() )
                {
                    Double value = input.get( jType ).get( topKey ).get( botKey );

                    output.getCompound( jType.toString() ).getCompound( topKey ).putDouble( botKey, value );
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

    public static Map<JType, Map<String, Map<String, Map<String, Double>>>> nbtToData4( CompoundNBT input )
    {
        Map<JType, Map<String, Map<String, Map<String, Double>>>> output = new HashMap<>();
        JType jType;
        for( String jTypeKey : input.keySet() )
        {
            jType = JType.getJType( jTypeKey );
            output.put( jType, new HashMap<>() );
            for( String topKey : input.getCompound( jTypeKey ).keySet() )
            {
                output.get( jType ).put( topKey, new HashMap<>() );
                for( String midKey : input.getCompound( jTypeKey ).getCompound( topKey ).keySet() )
                {
                    output.get( jType ).get( topKey ).put( midKey, new HashMap<>() );
                    for( String botKey : input.getCompound( jTypeKey ).getCompound( topKey ).getCompound( midKey ).keySet() )
                    {
                        output.get( jType ).get( topKey ).get( midKey ).put( botKey, input.getCompound( jTypeKey ).getCompound( topKey ).getCompound( midKey ).getDouble( botKey ) );
                    }
                }
            }
        }

        return output;
    }

    public static CompoundNBT data4ToNbt( Map<JType, Map<String, Map<String, Map<String, Double>>>> input )
    {
        CompoundNBT output = new CompoundNBT();

        for( JType jType : input.keySet() )
        {
            output.put( jType.toString(), new CompoundNBT() );
            for( String topKey : input.get( jType ).keySet() )
            {
                output.getCompound( jType.toString() ).put( topKey, new CompoundNBT() );
                for( String midKey : input.get( jType ).get( topKey ).keySet() )
                {
                    output.getCompound( jType.toString() ).getCompound( topKey ).put( midKey, new CompoundNBT() );
                    for( String botKey : input.get( jType ).get( topKey ).get( midKey ).keySet() )
                    {
                        Double value = input.get( jType ).get( topKey ).get( midKey ).get( botKey );
                        output.getCompound( jType.toString() ).getCompound( topKey ).getCompound( midKey ).putDouble( botKey, value );
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