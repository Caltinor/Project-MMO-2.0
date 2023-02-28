package harmonised.pmmo.config.writers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.registries.ForgeRegistries;

public class PackGenerator {
	public static final String PACKNAME = "generated_pack";
	public static final String DISABLER = "pmmo_disabler_pack";
	
	private enum Category {
		ITEMS("pmmo/items", server -> ForgeRegistries.ITEMS.getKeys(), 
				List.of("{",
					    "\"xp_values\":{",
					    "	\""+EventType.ANVIL_REPAIR.name()+"\":{},",
					    "	\""+EventType.BLOCK_PLACE.name()+"\":{},",
					    "	\""+EventType.BREW.name()+"\":{},",
					    "	\""+EventType.CONSUME.name()+"\":{},",
					    "	\""+EventType.CRAFT.name()+"\":{},",
					    "	\""+EventType.ENCHANT.name()+"\":{},",
					    "	\""+EventType.FISH.name()+"\":{},",
					    "	\""+EventType.SMELT.name()+"\":{},",
					    "	\""+EventType.DEAL_MELEE_DAMAGE.name()+"\":{},",
					    "	\""+EventType.MELEE_TO_MOBS.name()+"\":{},",
					    "	\""+EventType.MELEE_TO_ANIMALS.name()+"\":{},",
					    "	\""+EventType.MELEE_TO_PLAYERS.name()+"\":{},",
					    "	\""+EventType.DEAL_RANGED_DAMAGE.name()+"\":{},",
					    "	\""+EventType.RANGED_TO_MOBS.name()+"\":{},",
					    "	\""+EventType.RANGED_TO_ANIMALS.name()+"\":{},",
					    "	\""+EventType.RANGED_TO_PLAYERS.name()+"\":{},",
					    "	\""+EventType.ACTIVATE_ITEM.name()+"\":{}",
					    "},",
					    "\"nbt_xp_values\":{",
					    "	\""+EventType.ANVIL_REPAIR.name()+"\":[],",
					    "	\""+EventType.BLOCK_PLACE.name()+"\":[],",
					    "	\""+EventType.BREW.name()+"\":[],",
					    "	\""+EventType.CONSUME.name()+"\":[],",
					    "	\""+EventType.CRAFT.name()+"\":[],",
					    "	\""+EventType.ENCHANT.name()+"\":[],",
					    "	\""+EventType.FISH.name()+"\":[],",
					    "	\""+EventType.SMELT.name()+"\":[],",
					    "	\""+EventType.ACTIVATE_ITEM.name()+"\":[]",
					    "},",
					    "\"requirements\":{",
					    "	\""+ReqType.WEAR.name()+"\":{},",
					    "	\""+ReqType.TOOL.name()+"\":{},",
					    "	\""+ReqType.WEAPON.name()+"\":{},",
					    "	\""+ReqType.USE.name()+"\":{},",
					    "	\""+ReqType.PLACE.name()+"\":{},",
					    "	\""+ReqType.BREAK.name()+"\":{},",
					    "	\""+ReqType.INTERACT.name()+"\":{}",
					    "},",
					    "\"nbt_requirements\":{",
					    "	\""+ReqType.WEAR.name()+"\":[],",
					    "	\""+ReqType.TOOL.name()+"\":[],",
					    "	\""+ReqType.WEAPON.name()+"\":[],",
					    "	\""+ReqType.USE.name()+"\":[],",
					    "	\""+ReqType.PLACE.name()+"\":[],",
					    "	\""+ReqType.BREAK.name()+"\":[],",
					    "	\""+ReqType.INTERACT.name()+"\":[]",
					    "},",
					    "\"bonuses\":{",
					    "	\""+ModifierDataType.HELD.name()+"\":{},",
					    "	\""+ModifierDataType.WORN.name()+"\":{}",
					    "},",
					    "\"nbt_bonuses\":{",
					    "	\""+ModifierDataType.HELD.name()+"\":[],",
					    "	\""+ModifierDataType.WORN.name()+"\":[]",
					    "},",
					    "\"negative_effect\":{},",
					    "\"salvage\":{",
					    "    \"minecraft:item\": {",
					    "        \"salvageMax\": 1,",
					    "        \"baseChance\": 0.0,",
					    "        \"maxChance\": 1.0,",
					    "        \"chancePerLevel\": {\"skillname\": 1},",
					    "        \"levelReq\": {\"skillname\": 1},",
					    "        \"xpPerItem\": {\"skillname\": 1}",
					    "    }",
					    "},",
					    "\"vein_data\":{",
					    "    \"chargeCap\": 0,",
					    "    \"chargeRate\": 0.0",
					    "}",
						"}")),
		BLOCKS("pmmo/blocks", server -> ForgeRegistries.BLOCKS.getKeys(), 
				List.of("{",
					    "\"xp_values\":{",
					    "	\""+EventType.BLOCK_BREAK.name()+"\":{},",
					    "	\""+EventType.BLOCK_PLACE.name()+"\":{},",
					    "	\""+EventType.GROW.name()+"\":{},",
					    "	\""+EventType.HIT_BLOCK.name()+"\":{},",
					    "	\""+EventType.ACTIVATE_BLOCK.name()+"\":{}",
					    "},",
					    "\"nbt_xp_values\":{",
					    "	\""+EventType.BLOCK_BREAK.name()+"\":[],",
					    "	\""+EventType.BLOCK_PLACE.name()+"\":[],",
					    "	\""+EventType.GROW.name()+"\":[],",
					    "	\""+EventType.HIT_BLOCK.name()+"\":[],",
					    "	\""+EventType.ACTIVATE_BLOCK.name()+"\":[]",
					    "},",
					    "\"requirements\":{",
					    "	\""+ReqType.PLACE.name()+"\":{},",
					    "	\""+ReqType.BREAK.name()+"\":{},",
					    "	\""+ReqType.INTERACT.name()+"\":{}",
					    "},",
					    "\"nbt_requirements\":{",
					    "	\""+ReqType.PLACE.name()+"\":[],",
					    "	\""+ReqType.BREAK.name()+"\":[],",
					    "	\""+ReqType.INTERACT.name()+"\":[]",
					    "},",
					    "\"vein_data\":{",
					    "    \"consumeAmount\": 0",
					    "}",
						"}")),
		ENTITIES("pmmo/entities", server -> ForgeRegistries.ENTITY_TYPES.getKeys(), 
				List.of("{",
						"\"xp_values\":{",
					    "	\""+EventType.BREED.name()+"\":{},",
					    "	\""+EventType.RECEIVE_DAMAGE.name()+"\":{},",
					    "	\""+EventType.FROM_MOBS.name()+"\":{},",
					    "	\""+EventType.FROM_PLAYERS.name()+"\":{},",
					    "	\""+EventType.FROM_ANIMALS.name()+"\":{},",
					    "	\""+EventType.FROM_PROJECTILES.name()+"\":{},",
					    "	\""+EventType.DEAL_MELEE_DAMAGE.name()+"\":{},",
					    "	\""+EventType.MELEE_TO_MOBS.name()+"\":{},",
					    "	\""+EventType.MELEE_TO_PLAYERS.name()+"\":{},",
					    "	\""+EventType.MELEE_TO_ANIMALS.name()+"\":{},",
					    "	\""+EventType.DEAL_RANGED_DAMAGE.name()+"\":{},",
					    "	\""+EventType.RANGED_TO_MOBS.name()+"\":{},",
					    "	\""+EventType.RANGED_TO_PLAYERS.name()+"\":{},",
					    "	\""+EventType.RANGED_TO_ANIMALS.name()+"\":{},",
					    "	\""+EventType.DEATH.name()+"\":{},",
					    "	\""+EventType.ENTITY.name()+"\":{},",
					    "	\""+EventType.RIDING.name()+"\":{},",
					    "	\""+EventType.SHIELD_BLOCK.name()+"\":{},",
					    "	\""+EventType.SLEEP.name()+"\":{},",
					    "	\""+EventType.TAMING.name()+"\":{}",
					    "},",
					    "\"nbt_xp_values\":{",
					    "	\""+EventType.BREED.name()+"\":[],",
					    "	\""+EventType.RECEIVE_DAMAGE.name()+"\":[],",
					    "	\""+EventType.FROM_MOBS.name()+"\":[],",
					    "	\""+EventType.FROM_PLAYERS.name()+"\":[],",
					    "	\""+EventType.FROM_ANIMALS.name()+"\":[],",
					    "	\""+EventType.FROM_PROJECTILES.name()+"\":[],",
					    "	\""+EventType.DEAL_MELEE_DAMAGE.name()+"\":[],",
					    "	\""+EventType.MELEE_TO_MOBS.name()+"\":[],",
					    "	\""+EventType.MELEE_TO_PLAYERS.name()+"\":[],",
					    "	\""+EventType.MELEE_TO_ANIMALS.name()+"\":[],",
					    "	\""+EventType.DEAL_RANGED_DAMAGE.name()+"\":[],",
					    "	\""+EventType.RANGED_TO_MOBS.name()+"\":[],",
					    "	\""+EventType.RANGED_TO_PLAYERS.name()+"\":[],",
					    "	\""+EventType.RANGED_TO_ANIMALS.name()+"\":[],",
					    "	\""+EventType.DEATH.name()+"\":[],",
					    "	\""+EventType.ENTITY.name()+"\":[],",
					    "	\""+EventType.RIDING.name()+"\":[],",
					    "	\""+EventType.SHIELD_BLOCK.name()+"\":[],",
					    "	\""+EventType.SLEEP.name()+"\":[],",
					    "	\""+EventType.TAMING.name()+"\":[]",
					    "},",
					    "\"requirements\":{",
					    "	\""+ReqType.KILL.name()+"\":{},",
					    "	\""+ReqType.RIDE.name()+"\":{},",
					    "	\""+ReqType.TAME.name()+"\":{},",
					    "	\""+ReqType.BREED.name()+"\":{},",
					    "	\""+ReqType.ENTITY_INTERACT.name()+"\":{}",
					    "},",
					    "\"nbt_requirements\":{",
					    "	\""+ReqType.KILL.name()+"\":[],",
					    "	\""+ReqType.RIDE.name()+"\":[],",
					    "	\""+ReqType.TAME.name()+"\":[],",
					    "	\""+ReqType.BREED.name()+"\":[],",
					    "	\""+ReqType.ENTITY_INTERACT.name()+"\":[]",
					    "}",
						"}")),
		DIMENSIONS("pmmo/dimensions", server -> new HashSet<>(server.levelKeys().stream().map(key -> key.location()).toList()), 
				List.of("{",
					    "\"bonus\":{",
					    "    \""+ModifierDataType.DIMENSION.name()+"\":{}",
					    "},",
					    "\"travel_req\":{},",
					    "\"vein_blacklist\":[],",
					    "\"mob_multiplier\":{}",
						"}")),
		BIOMES("pmmo/biomes", server -> ForgeRegistries.BIOMES.getKeys(), 
				List.of("{",
					    "\"bonus\":{",
					    "    \""+ModifierDataType.BIOME.name()+"\":{}",
					    "},",
					    "\"positive_effect\":{},",
					    "\"negative_effect\":{},",
					    "\"travel_req\":{},",
					    "\"vein_blacklist\":[],",
					    "\"mob_multiplier\":{}",
						"}")),
		ENCHANTMENTS("pmmo/enchantments", server -> ForgeRegistries.ENCHANTMENTS.getKeys(), 
				List.of("{",
						"\"levels\":[]",
						"}")),
		EFFECTS("pmmo/effects", server -> ForgeRegistries.MOB_EFFECTS.getKeys(), 
				List.of("{",
				() -> List.of("{",
						"\"levels\":[]",
						"}"));
		
		public String route;
		public Function<MinecraftServer, Set<ResourceLocation>> valueList;
		private List<String> defaultData;
		Category(String route, Function<MinecraftServer, Set<ResourceLocation>> values, List<String> defaultData) {
			this.route = route;
			this.valueList = values;
			this.defaultData = defaultData;
		}
		
		public List<String> defaultData(boolean withOverride) {
			List<String> outList = new ArrayList<>(defaultData);
			if (withOverride)
				outList.set(0, "{\"override\":true,");
			return outList;
		}
	}
	
	public static void generateEmptyPack(MinecraftServer server, boolean withOverride) {		
		Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACKNAME);
		filepath.toFile().mkdirs();
		try {
		Files.write(
			filepath.resolve("pack.mcmeta"), 
			List.of("{","\"pack\":{\"description\":\"Generated Resources\",","\"pack_format\":9}","}"), 
			Charset.defaultCharset(),
			StandardOpenOption.CREATE_NEW,
			StandardOpenOption.WRITE);
		} catch (IOException e) {System.out.println("Error While Generating pack.mcmeta for Generated Data: "+e.toString());}
		
		for (Category category : Category.values()) {
			for (ResourceLocation id : category.valueList.apply(server)) {
				int index = id.getPath().lastIndexOf('/');
				String pathRoute = id.getPath().substring(0, index >= 0 ? index : 0);
				Path finalPath = filepath.resolve("data/"+id.getNamespace()+"/"+category.route+"/"+pathRoute);
				finalPath.toFile().mkdirs();
				try {
				Files.write(
						finalPath.resolve(id.getPath()+".json"), 
						category.defaultData(withOverride), 
						Charset.defaultCharset(),
						StandardOpenOption.CREATE_NEW,
						StandardOpenOption.WRITE);
				} catch (IOException e) {System.out.println("Error While Generating Pack File For: "+id.toString()+" ("+e.toString()+")");}
			}			
		}
	}
	
	public static void generateDisablingPack(MinecraftServer server) {
		Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(DISABLER);
		filepath.toFile().mkdirs();
		filepath.resolve("data").toFile().mkdirs();
		try {
		Files.write(
			filepath.resolve("pack.mcmeta"), 
			List.of(
					"{",
					"\"pack\":{\"description\":\"Pack to disable PMMO Defaults\",",
					"\"pack_format\":9},",
					"\"filter\":{\"block\":[{\"path\":\"pmmo\"}]}",
					"}"), 
			Charset.defaultCharset(),
			StandardOpenOption.CREATE_NEW,
			StandardOpenOption.WRITE);
		} catch (IOException e) {System.out.println("Error While Generating pack.mcmeta for Generated Data: "+e.toString());}
	}
}
