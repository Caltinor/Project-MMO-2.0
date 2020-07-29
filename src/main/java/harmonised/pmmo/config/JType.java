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
    XP_VALUE_TRIGGER(15),
    INFO_ORE(16),
    INFO_LOG(17),
    INFO_PLANT(18),
    BIOME_EFFECT(19),
    BIOME_MOB_MULTIPLIER(20),
    XP_BONUS_BIOME(21),
    XP_BONUS_HELD(22),
    XP_BONUS_WORN(23),
    SALVAGE_TO(24),
    SALVAGE_FROM(25),
    FISH_POOL(26),
    FISH_ENCHANT_POOL(27),
    MOB_RARE_DROP(28),
    LEVEL_UP_COMMAND(29),
    PLAYER_SPECIFIC(30),
    BLOCK_SPECIFIC(31),
    VEIN_BLACKLIST(32),
    REQ_CRAFT(33),
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
