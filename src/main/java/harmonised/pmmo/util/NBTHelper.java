package harmonised.pmmo.util;

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

    public static CompoundNBT mapSkillToNbt(Map<Skill, Double> map )
    {
        if( map == null )
            return new CompoundNBT();

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
}
