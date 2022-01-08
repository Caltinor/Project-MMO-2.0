package harmonised.pmmo.config.readers;

public enum XpValueDataType {
	VALUES_ITEM,
	VALUES_BLOCK,
	VALUES_ENTITY,
	/*VALUE_GENERAL,
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
    VALUE_RIGHT_CLICK,*/
    
    BONUS_BIOME,
    BONUS_HELD,
    BONUS_WORN,
    BONUS_DIMENSION,
    
    MULTIPLIER_DIMENSION,
    MULTIPLIER_ENTITY;
    
	public static final XpValueDataType[] baseTypes = new XpValueDataType[]{VALUES_ITEM, VALUES_BLOCK, VALUES_ENTITY};
	public static final XpValueDataType[] modifierTypes = new XpValueDataType[]{BONUS_BIOME, BONUS_HELD, BONUS_WORN, BONUS_DIMENSION, MULTIPLIER_DIMENSION, MULTIPLIER_ENTITY};
}
