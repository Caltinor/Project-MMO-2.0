package harmonised.pmmo.config.writers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.TriConsumer;

import com.google.common.collect.Comparators;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.JsonOps;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecMapEnchantment;
import harmonised.pmmo.config.codecs.CodecMapLocation;
import harmonised.pmmo.config.codecs.CodecMapLocation.LocationMapContainer;
import harmonised.pmmo.config.codecs.CodecMapObject;
import harmonised.pmmo.config.codecs.CodecMapObject.ObjectMapContainer;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

public class DataMigrator {
	public static boolean shouldMigrate(MinecraftServer server) {
		return !server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACKNAME).toFile().exists()
				&& FMLPaths.CONFIGDIR.get().resolve(LEGACYDIR).toFile().exists();
	}
	
	private static final String PACKNAME = "migration_pack";
	private static final String LEGACYDIR = "pmmo";
	private static final Type mapType = new TypeToken<Map<String, Map<String, Double>>>(){}.getType();
    private static final Type mapType2 = new TypeToken<Map<String, Map<String, Map<String, Double>>>>(){}.getType();
	private static final Map<ObjectType, Map<ResourceLocation, ObjectMapContainer>> objects = new HashMap<>();
	private static final Map<ObjectType, Map<ResourceLocation, LocationMapContainer>> locations = new HashMap<>();
	private static final Map<ResourceLocation, CodecMapEnchantment> enchants = new HashMap<>();
	
	private static enum JType {
	    REQ_WEAR(DataMigrator.mapType, ObjectType.ITEM),
	    REQ_USE_ENCHANTMENT(DataMigrator.mapType2, ObjectType.ENCHANTMENT),
	    REQ_TOOL(DataMigrator.mapType, ObjectType.ITEM),
	    REQ_WEAPON(DataMigrator.mapType, ObjectType.ITEM),
	    REQ_USE(DataMigrator.mapType, ObjectType.ITEM),
	    REQ_PLACE(DataMigrator.mapType, ObjectType.BLOCK),
	    REQ_BREAK(DataMigrator.mapType, ObjectType.BLOCK),
	    REQ_BIOME(DataMigrator.mapType, ObjectType.BIOME),
	    REQ_KILL(DataMigrator.mapType, ObjectType.ENTITY),
	    REQ_DIMENSION_TRAVEL(DataMigrator.mapType, ObjectType.DIMENSION),
	    XP_VALUE_BREAK(DataMigrator.mapType, ObjectType.BLOCK),
	    XP_VALUE_CRAFT(DataMigrator.mapType, ObjectType.ITEM),
	    XP_VALUE_PLACE(DataMigrator.mapType, ObjectType.BLOCK),
	    XP_VALUE_BREED(DataMigrator.mapType, ObjectType.ENTITY),
	    XP_VALUE_TAME(DataMigrator.mapType, ObjectType.ENTITY),
	    XP_VALUE_KILL(DataMigrator.mapType, ObjectType.ENTITY),
	    XP_VALUE_SMELT(DataMigrator.mapType, ObjectType.ITEM),
	    XP_VALUE_COOK(DataMigrator.mapType, ObjectType.ITEM),
	    XP_VALUE_BREW(DataMigrator.mapType, ObjectType.ITEM),
	    XP_VALUE_GROW(DataMigrator.mapType, ObjectType.BLOCK),
	    XP_VALUE_RIGHT_CLICK(DataMigrator.mapType, ObjectType.ITEM),
//	    INFO_ORE(DataMigrator.mapType),
//	    INFO_LOG(DataMigrator.mapType),
//	    INFO_PLANT(DataMigrator.mapType),
//	    INFO_SMELT(DataMigrator.mapType),
//	    INFO_COOK(DataMigrator.mapType),
//	    INFO_BREW(DataMigrator.mapType),
	    BIOME_EFFECT_NEGATIVE(DataMigrator.mapType, ObjectType.BIOME),
	    BIOME_EFFECT_POSITIVE(DataMigrator.mapType, ObjectType.BIOME),
//	    BIOME_MOB_MULTIPLIER(DataMigrator.mapType, ObjectType.BIOME),
	    XP_BONUS_BIOME(DataMigrator.mapType, ObjectType.BIOME),
	    XP_BONUS_HELD(DataMigrator.mapType, ObjectType.ITEM),
	    XP_BONUS_WORN(DataMigrator.mapType, ObjectType.ITEM),
	    XP_BONUS_DIMENSION(DataMigrator.mapType, ObjectType.DIMENSION),
//	    FISH_POOL(DataMigrator.mapType),
//	    MOB_RARE_DROP(DataMigrator.mapType),
//	    PLAYER_SPECIFIC(DataMigrator.mapType, ObjectType.PLAYER),
	    VEIN_BLACKLIST(DataMigrator.mapType, ObjectType.DIMENSION),
	    REQ_ENTITY_INTERACT(DataMigrator.mapType, ObjectType.ENTITY),
//	    TREASURE(DataMigrator.mapType2),
	    SALVAGE(DataMigrator.mapType2, ObjectType.ITEM);
		
		public Type type;
		public ObjectType obj;
		JType(Type type, ObjectType obj) {this.type = type; this.obj = obj;}
	}
	
	private static final TriConsumer<JType, String, Map<?,?>> ITEM_PROCESSOR = (type, rl, data) -> {
		TagKey<Item> tag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(rl.substring(1)));
		List<ResourceLocation> objectIDs = rl.contains("#") 
				? ForgeRegistries.ITEMS.tags().getTag(tag).stream().map(item -> ForgeRegistries.ITEMS.getKey(item)).toList()
				: List.of(new ResourceLocation(rl));
		for (ResourceLocation objectID : objectIDs)	{
			switch (type) {
			case REQ_WEAR -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().reqs(Map.of(ReqType.WEAR, remapInt(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case REQ_TOOL -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().reqs(Map.of(ReqType.TOOL, remapInt(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case REQ_WEAPON -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().reqs(Map.of(ReqType.WEAPON, remapInt(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case REQ_USE -> {
				Map<String, Integer> mappedRaws = remapInt(data);
				ObjectMapContainer raw = CodecMapObject.Builder.start().reqs(
						Map.of(ReqType.USE, mappedRaws, ReqType.INTERACT, mappedRaws)).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_CRAFT -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.CRAFT, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_SMELT -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.SMELT, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_COOK -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.SMELT, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_BREW -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.BREW, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_RIGHT_CLICK -> {
				Map<String, Long> remappedData = remapLong(data);
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(
						Map.of(EventType.ACTIVATE_ITEM, remappedData, EventType.ACTIVATE_BLOCK, remappedData)).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_BONUS_HELD -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().bonus(Map.of(ModifierDataType.HELD, remapDouble(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_BONUS_WORN -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().bonus(Map.of(ModifierDataType.WORN, remapDouble(data))).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case SALVAGE -> {
				ObjectMapContainer remappedRaws = CodecMapObject.Builder.start().salvage(remapSalvage(data)).build();
				objects.computeIfAbsent(ObjectType.ITEM, a -> new HashMap<>()).merge(objectID, remappedRaws, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			default -> {}}
		}
	};
	
	private static final TriConsumer<JType, String, Map<?,?>> BLOCK_PROCESSOR = (type, rl, data) -> {
		TagKey<Block> tag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(rl.substring(1)));
		List<ResourceLocation> objectIDs = rl.contains("#") 
				? ForgeRegistries.BLOCKS.tags().getTag(tag).stream().map(item -> ForgeRegistries.BLOCKS.getKey(item)).toList()
				: List.of(new ResourceLocation(rl));
		for (ResourceLocation objectID : objectIDs)	{
			switch (type) {
			case REQ_BREAK -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().reqs(Map.of(ReqType.BREAK, remapInt(data))).build();
				objects.computeIfAbsent(ObjectType.BLOCK, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case REQ_PLACE -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().reqs(Map.of(ReqType.PLACE, remapInt(data))).build();
				objects.computeIfAbsent(ObjectType.BLOCK, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_BREAK -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.BLOCK_BREAK, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.BLOCK, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_PLACE -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.BLOCK_PLACE, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.BLOCK, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_GROW -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.GROW, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.BLOCK, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			default -> {}}
		}
	};
	
	private static final TriConsumer<JType, String, Map<?,?>> ENTITY_PROCESSOR = (type, rl, data) -> {
		TagKey<EntityType<?>> tag = TagKey.create(ForgeRegistries.ENTITIES.getRegistryKey(), new ResourceLocation(rl.substring(1)));
		List<ResourceLocation> objectIDs = rl.contains("#") 
				? ForgeRegistries.ENTITIES.tags().getTag(tag).stream().map(item -> ForgeRegistries.ENTITIES.getKey(item)).toList()
				: List.of(new ResourceLocation(rl));
		for (ResourceLocation objectID : objectIDs)	{
			switch (type) {
			case REQ_KILL -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().reqs(Map.of(ReqType.KILL, remapInt(data))).build();
				objects.computeIfAbsent(ObjectType.ENTITY, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case REQ_ENTITY_INTERACT -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().reqs(Map.of(ReqType.ENTITY_INTERACT, remapInt(data))).build();
				objects.computeIfAbsent(ObjectType.ENTITY, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_BREED -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.BREED, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.ENTITY, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_TAME -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.TAMING, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.ENTITY, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			case XP_VALUE_KILL -> {
				ObjectMapContainer raw = CodecMapObject.Builder.start().xpValues(Map.of(EventType.DEATH, remapLong(data))).build();
				objects.computeIfAbsent(ObjectType.ENTITY, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> ObjectMapContainer.combine(og, ng));
			}
			default -> {}}
		}
	};
	
	private static final TriConsumer<JType, String, Map<?,?>> DIMENSION_PROCESSOR = (type, rl, data) -> {
		switch (type) {
		case REQ_DIMENSION_TRAVEL -> {
			LocationMapContainer raw = CodecMapLocation.Builder.start().req(remapInt(data)).build();
			locations.computeIfAbsent(ObjectType.DIMENSION, a -> new HashMap<>()).merge(new ResourceLocation(rl), raw, (og, ng) -> LocationMapContainer.combine(og, ng));
		}
		case XP_BONUS_DIMENSION -> {
			LocationMapContainer raw = CodecMapLocation.Builder.start().bonus(Map.of(ModifierDataType.DIMENSION, remapDouble(data))).build();
			locations.computeIfAbsent(ObjectType.DIMENSION, a -> new HashMap<>()).merge(new ResourceLocation(rl), raw, (og, ng) -> LocationMapContainer.combine(og, ng));
		}
		case VEIN_BLACKLIST -> {
			LocationMapContainer raw = CodecMapLocation.Builder.start().veinBlacklist(remapVein(data)).build();
			locations.computeIfAbsent(ObjectType.DIMENSION, a -> new HashMap<>()).merge(new ResourceLocation(rl), raw, (og, ng) -> LocationMapContainer.combine(og, ng));
		}
		default -> {}}
	};
	
	private static final TriConsumer<JType, String, Map<?,?>> BIOME_PROCESSOR = (type, rl, data) -> {
		TagKey<Biome> tag = TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), new ResourceLocation(rl.substring(1)));
		List<ResourceLocation> objectIDs = rl.contains("#") 
				? ForgeRegistries.BIOMES.tags().getTag(tag).stream().map(item -> ForgeRegistries.BIOMES.getKey(item)).toList()
				: List.of(new ResourceLocation(rl));
		for (ResourceLocation objectID : objectIDs)	{
			switch (type) {
			case REQ_BIOME -> {
				LocationMapContainer raw = CodecMapLocation.Builder.start().req(remapInt(data)).build();
				locations.computeIfAbsent(ObjectType.BIOME, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> LocationMapContainer.combine(og, ng));
			}
			case BIOME_EFFECT_POSITIVE -> {
				LocationMapContainer raw = CodecMapLocation.Builder.start().positive(remapInt(data).entrySet().stream()
						.collect(Collectors.toMap(entry -> new ResourceLocation(entry.getKey()), entry -> entry.getValue()))).build();
				locations.computeIfAbsent(ObjectType.BIOME, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> LocationMapContainer.combine(og, ng));
			}
			case BIOME_EFFECT_NEGATIVE -> {
				LocationMapContainer raw = CodecMapLocation.Builder.start().negative(remapInt(data).entrySet().stream()
						.collect(Collectors.toMap(entry -> new ResourceLocation(entry.getKey()), entry -> entry.getValue()))).build();
				locations.computeIfAbsent(ObjectType.BIOME, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> LocationMapContainer.combine(og, ng));
			}
			//case BIOME_MOB_MULTIPLIER -> {}
			case XP_BONUS_BIOME -> {
				LocationMapContainer raw = CodecMapLocation.Builder.start().bonus(Map.of(ModifierDataType.BIOME, remapDouble(data))).build();
				locations.computeIfAbsent(ObjectType.BIOME, a -> new HashMap<>()).merge(objectID, raw, (og, ng) -> LocationMapContainer.combine(og, ng));
			}
			default -> {}}
		}
	};
	
	private static Map<String, Integer> remapInt(Map<?,?> data) {
		return data.entrySet().stream().collect(Collectors.toMap(
						entry -> String.valueOf(entry.getKey()), 
						entry -> Double.valueOf(entry.getValue().toString()).intValue()));
	}
	private static Map<String, Long> remapLong(Map<?,?> data) {
		return data.entrySet().stream().collect(Collectors.toMap(
						entry -> String.valueOf(entry.getKey()), 
						entry -> Double.valueOf(entry.getValue().toString()).longValue()));
	}
	private static Map<String, Double> remapDouble(Map<?,?> data) {
		return data.entrySet().stream().collect(Collectors.toMap(
						entry -> String.valueOf(entry.getKey()), 
						entry -> Double.valueOf(entry.getValue().toString())));
	}
	private static Map<ResourceLocation, SalvageData> remapSalvage(Map<?,?> data) {
		Map<String, Map<String, Double>> mappedRaws = data.entrySet().stream().collect(Collectors.toMap(
				entry -> String.valueOf(entry.getKey()), 
				entry -> ((Map<?,?>)entry.getValue()).entrySet().stream().collect(Collectors.toMap(
						e -> String.valueOf(e.getKey()), 
						e -> Double.valueOf(e.getValue().toString())))));
		Map<ResourceLocation, SalvageData> outData = new HashMap<>();
		mappedRaws.forEach((key, value) -> outData.put(new ResourceLocation(key), SalvageData.migrate(value)));				
		return outData;
	}
	private static List<ResourceLocation> remapVein(Map<?,?> data) {
		return data.keySet().stream().map(key -> new ResourceLocation(key.toString())).toList();
	}
	private static List<Map<String, Integer>> remapEnchant(Map<?,?> data) {
		Map<Integer, Map<String, Integer>> typeCastMap = data.entrySet().stream().collect(Collectors.toMap(
				entry -> Double.valueOf(entry.getKey().toString()).intValue(), 
				entry -> remapInt((Map<?,?>)entry.getValue())));
		int maxConfigured = typeCastMap.keySet().stream().max(Comparators::max).orElse(-1);
		List<Map<String, Integer>> outMap= new ArrayList<>();
		for (int i = 0; i <= maxConfigured; i++) {
			var innerMap = typeCastMap.get(i);
			outMap.add(innerMap == null ? new HashMap<>() : innerMap);
		}
		return outMap;
	}
	
	private static enum ObjectType {
		ITEM("/pmmo/items/", DataMigrator.ITEM_PROCESSOR),
		BLOCK("/pmmo/blocks/",DataMigrator.BLOCK_PROCESSOR),
		ENTITY("/pmmo/entities/", DataMigrator.ENTITY_PROCESSOR),
		DIMENSION("/pmmo/dimensions/", DataMigrator.DIMENSION_PROCESSOR),
		BIOME("/pmmo/biomes/", DataMigrator.BIOME_PROCESSOR),
		ENCHANTMENT("/pmmo/enchantments/", (type, rl, data) -> {
			switch (type) {
			case REQ_USE_ENCHANTMENT -> {
				CodecMapEnchantment raw = CodecMapEnchantment.Builder.start().skillArray(remapEnchant(data)).build();
				enchants.merge(new ResourceLocation(rl), raw, (og, ng) -> CodecMapEnchantment.combine(og, ng));
			}
			default -> {}}
		});
		
		public String path;
		private TriConsumer<JType, String, Map<?,?>> processor;
		ObjectType(String path, TriConsumer<JType, String, Map<?,?>> processor) {
			this.path = path;
			this.processor = processor;
		}
		
		
	}

	public static void generateMigrationPack(MinecraftServer server) {	
		Gson gson = new Gson();
		Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACKNAME);
		filepath.toFile().mkdirs();
		try {
		Files.write(
			filepath.resolve("pack.mcmeta"), 
			List.of(
					"{",
					"\"pack\":{\"description\":\"PMMO Lecacy to Rework Migration Pack\",",
					"\"pack_format\":9},",
					"\"filter\":{\"block\":[{\"path\":\"pmmo\"}]}",
					"}"), 
			Charset.defaultCharset(),
			StandardOpenOption.CREATE_NEW,
			StandardOpenOption.WRITE);
		} catch (IOException e) {System.out.println("Error While Generating pack.mcmeta for Generated Data: "+e.toString());}

		for (JType jtype : JType.values()) {
            String fileName = jtype.name().toLowerCase() + ".json";
            File file = FMLPaths.CONFIGDIR.get().resolve("pmmo/" + fileName).toFile();
            Map<?,?> rawMap;
            try(InputStream input = new FileInputStream(file.getPath());
                Reader reader = new BufferedReader(new InputStreamReader(input))) {
                rawMap = gson.fromJson(reader, jtype.type);
            }
            catch(Exception e) {
            	e.printStackTrace();
            	return;
            }
            if (rawMap == null) return;
			for (Map.Entry<?,?> id : rawMap.entrySet()) {
				System.out.println("key:"+id.getKey().toString()+" Value:"+MsLoggy.mapToString((Map<?,?>)id.getValue()));
				jtype.obj.processor.accept(jtype, (String)id.getKey(), (Map<?,?>)id.getValue());
			}			
		}
		
		for(Map.Entry<ObjectType, Map<ResourceLocation, ObjectMapContainer>> entry : objects.entrySet()) {
			ObjectType type = entry.getKey();
			System.out.println("Migration State: Objects Serialized"+entry.getValue().size());
			for (Map.Entry<ResourceLocation, ObjectMapContainer> value : entry.getValue().entrySet()) {
				JsonElement data = ObjectMapContainer.CODEC.encodeStart(JsonOps.INSTANCE, value.getValue())
						.resultOrPartial(str -> System.out.println(str))
						.get();
				write(value.getKey(), data, type, filepath);
			}
		}
		for(Map.Entry<ObjectType, Map<ResourceLocation, LocationMapContainer>> entry : locations.entrySet()) {
			ObjectType type = entry.getKey();
			System.out.println("Migration State: Locations Serialized"+entry.getValue().size());
			for (Map.Entry<ResourceLocation, LocationMapContainer> value : entry.getValue().entrySet()) {
				JsonElement data = LocationMapContainer.CODEC.encodeStart(JsonOps.INSTANCE, value.getValue())
						.resultOrPartial(str -> System.out.println(str))
						.get();
				write(value.getKey(), data, type, filepath);
			}
		}
		System.out.println("Migration State: Enchants Serialized"+enchants.size());
		for (Map.Entry<ResourceLocation, CodecMapEnchantment> value : enchants.entrySet()) {			
			JsonElement data = CodecMapEnchantment.CODEC.encodeStart(JsonOps.INSTANCE, value.getValue())
					.resultOrPartial(str -> System.out.println(str))
					.get();
			write(value.getKey(), data, ObjectType.ENCHANTMENT, filepath);
		}
		
		server.getCommands().performCommand(server.createCommandSourceStack(), "reload");
	}
	
	private static void write(ResourceLocation rl, JsonElement data, ObjectType type, Path filepath) {
		Path finalPath = filepath.resolve("data/"+rl.getNamespace()+type.path);
		finalPath.toFile().mkdirs();
		try {
		Files.write(
				finalPath.resolve(rl.getPath()+".json"), 
				List.of(data.toString()),
				Charset.defaultCharset(),
				StandardOpenOption.CREATE_NEW,
				StandardOpenOption.WRITE);
		} catch (IOException e) {System.out.println("Error While Generating Migration Pack File For: "+rl.toString()+" ("+e.toString()+")");}
	}
}