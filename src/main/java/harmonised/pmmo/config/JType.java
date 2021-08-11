package harmonised.pmmo.config;

import harmonised.pmmo.skills.Skill;

import java.util.HashMap;
import java.util.Map;

public enum JType
{
    NONE(0),
    REQ_WEAR(1),
    REQ_USE_ENCHANTMENT(2),
    REQ_TOOL(3),
    REQ_WEAPON(4),
    REQ_USE(5),
    REQ_PLACE(6),
    REQ_BREAK(7),
    REQ_BIOME(8),
    REQ_KILL(9),
    REQ_CRAFT(10),
    REQ_DIMENSION_TRAVEL(11),
    XP_VALUE_GENERAL(12),
    XP_VALUE_BREAK(13),
    XP_VALUE_CRAFT(14),
    XP_VALUE_PLACE(15),
    XP_VALUE_BREED(16),
    XP_VALUE_TAME(17),
    XP_VALUE_KILL(18),
    XP_VALUE_SMELT(19),
    XP_VALUE_COOK(20),
    XP_VALUE_TRIGGER(21),
    XP_VALUE_BREW(22),
    XP_VALUE_GROW(23),
    INFO_ORE(24),
    INFO_LOG(25),
    INFO_PLANT(26),
    INFO_SMELT(27),
    INFO_COOK(28),
    INFO_BREW(29),
    BIOME_EFFECT_NEGATIVE(30),
    BIOME_EFFECT_POSITIVE(31),
    BIOME_MOB_MULTIPLIER(32),
    XP_BONUS_BIOME(33),
    XP_BONUS_HELD(34),
    XP_BONUS_WORN(35),
    XP_BONUS_DIMENSION(36),
    FISH_POOL(37),
    FISH_ENCHANT_POOL(38),
    MOB_RARE_DROP(39),
    LEVEL_UP_COMMAND(40),
    PLAYER_SPECIFIC(41),
    BLOCK_SPECIFIC(42),
    ITEM_SPECIFIC(43),
    VEIN_BLACKLIST(44),
    REQ_ENTITY_INTERACT(45),

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
    XP_MULTIPLIER_ENTITY(210),
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