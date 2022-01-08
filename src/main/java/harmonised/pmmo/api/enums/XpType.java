package harmonised.pmmo.api.enums;

public enum XpType {
	VALUE_GENERAL,
    VALUE_BREAK,
    VALUE_CRAFT,
    VALUE_PLACE,
    VALUE_BREED,
    VALUE_TAME,
    VALUE_KILL,
    VALUE_SMELT,
    VALUE_COOK,
    VALUE_TRIGGER,
    VALUE_BREW,
    VALUE_GROW,
    VALUE_RIGHT_CLICK,
    
    BONUS_BIOME,
    BONUS_HELD,
    BONUS_WORN,
    BONUS_DIMENSION,
    
    MULTIPLIER_DIMENSION,
    MULTIPLIER_ENTITY;
    
	private static final XpType[] modifierTypes = new XpType[]{BONUS_BIOME, BONUS_HELD, BONUS_WORN, BONUS_DIMENSION, MULTIPLIER_DIMENSION, MULTIPLIER_ENTITY};
    
	public static XpType[] getModifierTypes() {
		return modifierTypes;
	}
}
