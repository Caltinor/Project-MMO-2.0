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
    XP_VALUE_PLACE(13),
    XP_VALUE_BREED(14),
    XP_VALUE_TAME(15),
    XP_VALUE_KILL(16),
    XP_VALUE_SMELT(17),
    XP_VALUE_COOK(18),
    XP_VALUE_TRIGGER(19),
    XP_VALUE_BREW(20),
    XP_VALUE_GROW(21),
    INFO_ORE(22),
    INFO_LOG(23),
    INFO_PLANT(24),
    INFO_SMELT(25),
    INFO_COOK(26),
    INFO_BREW(27),
    BIOME_EFFECT_NEGATIVE(28),
    BIOME_EFFECT_POSITIVE(29),
    BIOME_MOB_MULTIPLIER(30),
    XP_BONUS_BIOME(31),
    XP_BONUS_HELD(32),
    XP_BONUS_WORN(33),
    XP_BONUS_DIMENSION(34),
    FISH_POOL(35),
    FISH_ENCHANT_POOL(36),
    MOB_RARE_DROP(37),
    LEVEL_UP_COMMAND(38),
    PLAYER_SPECIFIC(39),
    BLOCK_SPECIFIC(40),
    ITEM_SPECIFIC(41),
    VEIN_BLACKLIST(42),

    TREASURE(100),
    SALVAGE(101),

    SALVAGE_FROM(201),
    TREASURE_FROM(202),
    SKILLS(203),
    STATS(204),
    DIMENSION(205),
    CREDITS(206),
    SETTINGS(207),
    GUI_SETTINGS(208),
    XP_MULTIPLIER_DIMENSION(209),
    HISCORE(210);

    public static final Map<JType, Integer> jTypeMap = new HashMap<>();
    public static final Map<Integer, JType> intMap = new HashMap<>();
    public static final Map<String, JType> stringMap = new HashMap<>();
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