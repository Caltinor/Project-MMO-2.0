package harmonised.pmmo.config;

import harmonised.pmmo.skills.Skill;

import java.util.HashMap;
import java.util.Map;

public enum JType
{
    REQ_WEAR(0),
    REQ_TOOL(1),
    REQ_WEAPON(2),
    REQ_USE(3),
    REQ_PLACE(4),
    REQ_BREAK(5),
    REQ_BIOME(6),
    REQ_KILL(7),
    XP_VALUE_GENERAL(8),
    XP_VALUE_BREAK(9),
    XP_VALUE_CRAFT(10),
    XP_VALUE_BREED(11),
    XP_VALUE_TAME(12),
    XP_VALUE_KILL(13),
    INFO_ORE(14),
    INFO_LOG(15),
    INFO_PLANT(16),
    BIOME_EFFECT(17),
    BIOME_MOB_MULTIPLIER(18),
    XP_BONUS_BIOME(19),
    XP_BONUS_HELD(20),
    XP_BONUS_WORN(21),
    SALVAGE_TO(22),
    SALVAGE_FROM(23),
    FISH_POOL(24),
    FISH_ENCHANT_POOL(25),
    MOB_RARE_DROP(26),
    LEVEL_UP_COMMAND(27),
    PLAYER_SPECIFIC(28),
    BLOCK_SPECIFIC(29),
    VEIN_BLACKLIST(30),
    STATS(100),
    DIMENSION(101);

    public static final Map< JType, Integer > jTypeMap = new HashMap<>();
    public static final Map< Integer, JType > intMap = new HashMap<>();
    public static final Map< String, JType > stringMap = new HashMap<>();
    private final int value;

    JType( int value )
    {
        this.value = value;
    }

    static
    {
        for( JType jType : JType.values() )
        {
            jTypeMap.put( jType, jType.value );
            intMap.put( jType.value, jType );
            stringMap.put( jType.name().toLowerCase(), jType );
        }
    }

    public int getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return this.name().toLowerCase();
    }

    public static JType getJType( String str )
    {
        return stringMap.get( str );
    }

    public static JType getJType( int i )
    {
        return intMap.get( i );
    }
}
