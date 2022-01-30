package harmonised.pmmo.config;

/*import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;*/

public enum CoreType /*implements StringRepresentable, IExtensibleEnum*/{
	//EXTRA_CHANCE,			<= to loot table
    LOCATION_EFFECT_NEGATIVE,	//integerType {"modid:biome":{"potion:id":0, "potion:id":1}}
    LOCATION_EFFECT_POSITIVE,	//integerType{"modid:biome":{"potion:id":0, "potion:id":1}}
    //BIOME_MOB_MULTIPLIER, //Used in mob scaling //jsonType  {"modid:entity":{"attribute":0.5, "attribute": 0.35}}
    
    //FISH_POOL,
    //FISH_ENCHANT_POOL,    These 3 should be loot tables using pmmo loot predicates
    //MOB_RARE_DROP,
    //TODO level up command will run a function from a datapack instead of reinenting the wheel
    //LEVEL_UP_COMMAND,		TODO this is going to be a MC Function trigger
    PLAYER_SPECIFIC,		//jsonType
    //BLOCK_SPECIFIC,			//map<string, boolean?> maybe merge with Info?
    //ITEM_SPECIFIC,			//TODO this might not be necessary anymore
    //VEIN_BLACKLIST,    		//Dim //String Array

    //TREASURE,   Loot Predicate as well
    //SALVAGE,				//map<string, jsonObject> new type
    GLOBALS,
    SKILLS;/*,				//JsonType <= TODO update to
    CREDITS,
    
    HISCORE*/
    
    public static final CoreType[] jsonTypes = new CoreType[] {PLAYER_SPECIFIC, SKILLS};
	
    /*public static final Codec<CoreType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(CoreType::values, CoreType::byName);
    private static final Map<String, CoreType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(CoreType::getSerializedName, s -> {return s;}));
    public static CoreType byName(String name) {return BY_NAME.get(name);}
    
    @Override
	public String getSerializedName() {return this.name().toLowerCase();}*/
}
