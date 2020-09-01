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
    REQ_CRAFT(9),
    XP_VALUE_GENERAL(10),
    XP_VALUE_BREAK(11),
    XP_VALUE_CRAFT(12),
    XP_VALUE_BREED(13),
    XP_VALUE_TAME(14),
    XP_VALUE_KILL(15),
    XP_VALUE_SMELT(16),
    XP_VALUE_COOK(17),
    XP_VALUE_TRIGGER(18),
    XP_VALUE_BREW(19),
    INFO_ORE(20),
    INFO_LOG(21),
    INFO_PLANT(22),
    INFO_SMELT(23),
    INFO_COOK(24),
    INFO_BREW(25),
    BIOME_EFFECT(26),
    BIOME_MOB_MULTIPLIER(27),
    XP_BONUS_BIOME(28),
    XP_BONUS_HELD(29),
    XP_BONUS_WORN(30),
    SALVAGE_TO(31),
    FISH_POOL(32),
    FISH_ENCHANT_POOL(33),
    MOB_RARE_DROP(34),
    LEVEL_UP_COMMAND(35),
    PLAYER_SPECIFIC(36),
    BLOCK_SPECIFIC(37),
    VEIN_BLACKLIST(38),
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
