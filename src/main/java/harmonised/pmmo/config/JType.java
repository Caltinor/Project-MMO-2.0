package harmonised.pmmo.config;

import java.util.HashMap;

public enum JType
{
    WEAR_REQ(0),
    TOOL_REQ(1),
    WEAPON_REQ(2),
    USE_REQ(3),
    PLACE_REQ(4),
    BREAK_REQ(5),
    BIOME_REQ(6),
    KILL_REQ(7),
    XP_VALUE_BREAK(8),
    XP_VALUE_CRAFT(9),
    XP_VALUE_BREED(10),
    XP_VALUE_TAME(11),
    XP_VALUE_KILL(12),
    ORE_INFO(13),
    LOG_INFO(14),
    PLANT_INFO(15),
    BIOME_EFFECT(16),
    BIOME_MOB_MULTIPLIER(17),
    BIOME_XP_BONUS(18),
    HELD_XP_BONUS(19),
    WORN_XP_BONUS(20),
    SALVAGE_TO(21),
    SALVAGE_FROM(22),
    FISH_POOL(23),
    FISH_ENCHANT_POOL(24),
    MOB_RARE_DROP(25),
    LEVEL_UP_COMMAND(26),
    PLAYER_SPECIFIC(27),
    BLOCK_SPECIFIC(28),
    VEIN_BLACKLIST(29);

    private final int value;

    JType( int value )
    {
        this.value = value;
    }
}
