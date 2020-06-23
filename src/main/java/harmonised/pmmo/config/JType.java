package harmonised.pmmo.config;

import harmonised.pmmo.skills.Skill;

import java.util.HashMap;
import java.util.Map;

public enum JType
{
    NONE(0),
    REQ_WEAR(1),
    REQ_TOOL(2),
    REQ_WEAPON(3),
    REQ_USE(4),
    REQ_PLACE(5),
    REQ_BREAK(6),
    REQ_BIOME(7),
    REQ_KILL(8),
    XP_VALUE_GENERAL(9),
    XP_VALUE_BREAK(10),
    XP_VALUE_CRAFT(11),
    XP_VALUE_BREED(12),
    XP_VALUE_TAME(13),
    XP_VALUE_KILL(14),
    INFO_ORE(15),
    INFO_LOG(16),
    INFO_PLANT(17),
    BIOME_EFFECT(18),
    BIOME_MOB_MULTIPLIER(19),
    XP_BONUS_BIOME(20),
    XP_BONUS_HELD(21),
    XP_BONUS_WORN(22),
    SALVAGE_TO(23),
    SALVAGE_FROM(24),
    FISH_POOL(25),
    FISH_ENCHANT_POOL(26),
    MOB_RARE_DROP(27),
    LEVEL_UP_COMMAND(28),
    PLAYER_SPECIFIC(29),
    BLOCK_SPECIFIC(30),
    VEIN_BLACKLIST(31),
    STATS(100),
    DIMENSION(101),
    CREDITS(102),
    SETTINGS(103),
    GUI_SETTINGS(104);

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
