package harmonised.pmmo.config;

public enum CoreType {
	INFO_ORE,
    INFO_LOG,
    INFO_PLANT,
    INFO_SMELT,
    INFO_COOK,
    INFO_BREW,
    BIOME_EFFECT_NEGATIVE,
    BIOME_EFFECT_POSITIVE,
    BIOME_MOB_MULTIPLIER, //Used in mob scaling
    
    //FISH_POOL,
    //FISH_ENCHANT_POOL,    These 3 should be loot tables using pmmo loot predicates
    //MOB_RARE_DROP,
    LEVEL_UP_COMMAND,
    PLAYER_SPECIFIC,
    BLOCK_SPECIFIC,
    ITEM_SPECIFIC,
    VEIN_BLACKLIST,    

    //TREASURE,   Loot Predicate as well
    SALVAGE,/*

    SALVAGE_FROM,
    TREASURE_FROM,*/
    SKILLS/*,
    STATS,
    DIMENSION,
    CREDITS,
    SETTINGS,
    GUI_SETTINGS,
    
    HISCORE,
    PERKS*/
}
