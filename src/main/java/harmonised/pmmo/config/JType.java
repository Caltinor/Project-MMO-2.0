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
    XP_VALUE_SMELT(15),
    XP_VALUE_TRIGGER(16),
    INFO_ORE(17),
    INFO_LOG(18),
    INFO_PLANT(19),
    INFO_SMELT(20),
    BIOME_EFFECT(21),
    BIOME_MOB_MULTIPLIER(22),
    XP_BONUS_BIOME(23),
    XP_BONUS_HELD(24),
    XP_BONUS_WORN(25),
    SALVAGE_TO(26),
    FISH_POOL(27),
    FISH_ENCHANT_POOL(28),
    MOB_RARE_DROP(29),
    LEVEL_UP_COMMAND(30),
    PLAYER_SPECIFIC(31),
    BLOCK_SPECIFIC(32),
    VEIN_BLACKLIST(33),
    REQ_CRAFT(34),
    SALVAGE_FROM(100),
    STATS(101),
    DIMENSION(102),
    CREDITS(103),
    SETTINGS(104),
    GUI_SETTINGS(105);

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
