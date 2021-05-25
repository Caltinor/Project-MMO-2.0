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
    XP_VALUE_GENERAL(11),
    XP_VALUE_BREAK(12),
    XP_VALUE_CRAFT(13),
    XP_VALUE_PLACE(14),
    XP_VALUE_BREED(15),
    XP_VALUE_TAME(16),
    XP_VALUE_KILL(17),
    XP_VALUE_SMELT(18),
    XP_VALUE_COOK(19),
    XP_VALUE_TRIGGER(20),
    XP_VALUE_BREW(21),
    XP_VALUE_GROW(22),
    INFO_ORE(23),
    INFO_LOG(24),
    INFO_PLANT(25),
    INFO_SMELT(26),
    INFO_COOK(27),
    INFO_BREW(28),
    BIOME_EFFECT_NEGATIVE(29),
    BIOME_EFFECT_POSITIVE(30),
    BIOME_MOB_MULTIPLIER(31),
    XP_BONUS_BIOME(32),
    XP_BONUS_HELD(33),
    XP_BONUS_WORN(34),
    XP_BONUS_DIMENSION(35),
    FISH_POOL(36),
    FISH_ENCHANT_POOL(37),
    MOB_RARE_DROP(38),
    LEVEL_UP_COMMAND(39),
    PLAYER_SPECIFIC(40),
    BLOCK_SPECIFIC(41),
    ITEM_SPECIFIC(42),
    VEIN_BLACKLIST(43),
    REQ_ENTITY_INTERACT(44),

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