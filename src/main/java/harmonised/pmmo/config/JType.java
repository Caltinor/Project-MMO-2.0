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
    XP_VALUE_COOK(16),
    XP_VALUE_TRIGGER(17),
    INFO_ORE(18),
    INFO_LOG(19),
    INFO_PLANT(20),
    INFO_SMELT(21),
    INFO_COOK(22),
    BIOME_EFFECT(23),
    BIOME_MOB_MULTIPLIER(24),
    XP_BONUS_BIOME(25),
    XP_BONUS_HELD(26),
    XP_BONUS_WORN(27),
    SALVAGE_TO(28),
    FISH_POOL(29),
    FISH_ENCHANT_POOL(30),
    MOB_RARE_DROP(31),
    LEVEL_UP_COMMAND(32),
    PLAYER_SPECIFIC(33),
    BLOCK_SPECIFIC(34),
    VEIN_BLACKLIST(35),
    REQ_CRAFT(36),
    TREASURE(37),
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