package harmonised.pmmo.config.writers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import harmonised.pmmo.api.APIUtils.SalvageBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.registries.ForgeRegistries;

public class PackGenerator {
	public static final String PACKNAME = "generated_pack";
	public static final String DISABLER = "pmmo_disabler_pack";
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	private enum Category {
		ITEMS("pmmo/items", server -> ForgeRegistries.ITEMS.getKeys(), override -> {
			ObjectData data = new ObjectData(override, new HashSet<>(),
					Arrays.stream(ReqType.ITEM_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r -> new HashMap<>())),
					Arrays.stream(ReqType.ITEM_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r -> new ArrayList<>())),
					new HashMap<>(),
					Arrays.stream(EventType.ITEM_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e -> new HashMap<>())),
					Arrays.stream(EventType.ITEM_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e -> new ArrayList<>())),
					Map.of(ModifierDataType.WORN, new HashMap<>(), ModifierDataType.HELD, new HashMap<>()),
					Map.of(ModifierDataType.WORN, new ArrayList<>(), ModifierDataType.HELD, new ArrayList<>()),
					Map.of(new ResourceLocation("modid:item"), SalvageBuilder.start().build()),
					VeinData.EMPTY);
			JsonElement raw = ObjectData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get();
			return gson.toJson(raw);}),
		BLOCKS("pmmo/blocks", server -> ForgeRegistries.BLOCKS.getKeys(), override -> {
			ObjectData data = new ObjectData(override, new HashSet<>(),
					Arrays.stream(ReqType.BLOCK_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r -> new HashMap<>())),
					Arrays.stream(ReqType.BLOCK_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r -> new ArrayList<>())),
					new HashMap<>(), //negative effects
					Arrays.stream(EventType.BLOCK_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e -> new HashMap<>())),
					Arrays.stream(EventType.BLOCK_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e -> new ArrayList<>())),
					new HashMap<>(), //bonuses
					new HashMap<>(), //nbt bonuses
					new HashMap<>(), //salvage
					new VeinData(Optional.empty(), Optional.empty(), Optional.of(1)));
			JsonObject raw = ObjectData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get().getAsJsonObject();
			raw.remove("negative_effect");
			raw.remove("bonuses");
			raw.remove("nbt_bonuses");
			raw.remove("salvage");
			return gson.toJson(raw);}),
		ENTITIES("pmmo/entities", server -> ForgeRegistries.ENTITY_TYPES.getKeys(), override -> {
			ObjectData data = new ObjectData(override, new HashSet<>(),
					Arrays.stream(ReqType.ENTITY_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r -> new HashMap<>())),
					Arrays.stream(ReqType.ENTITY_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r -> new ArrayList<>())),
					new HashMap<>(), //negative effects
					Arrays.stream(EventType.ENTITY_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e -> new HashMap<>())),
					Arrays.stream(EventType.ENTITY_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e -> new ArrayList<>())),
					new HashMap<>(), //bonuses
					new HashMap<>(), //nbt bonuses
					new HashMap<>(), //salvage
					VeinData.EMPTY);
			JsonObject raw = ObjectData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get().getAsJsonObject();
			raw.remove("negative_effect");
			raw.remove("bonuses");
			raw.remove("nbt_bonuses");
			raw.remove("salvage");
			raw.remove(VeinMiningLogic.VEIN_DATA);
			return gson.toJson(raw);}),
		DIMENSIONS("pmmo/dimensions", server -> new HashSet<>(server.levelKeys().stream().map(key -> key.location()).toList()), 
				override -> {
				LocationData data = new LocationData(override, new HashSet<>(),
						Map.of(ModifierDataType.DIMENSION, new HashMap<>()),
						new HashMap<>(),
						new HashMap<>(),
						new ArrayList<>(),
						new HashMap<>(),
						new HashMap<>());				
				JsonObject raw = LocationData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get().getAsJsonObject();
				raw.remove("positive_effect");
				raw.remove("negative_effect");
				return gson.toJson(raw);}),
		BIOMES("pmmo/biomes", server -> ForgeRegistries.BIOMES.getKeys(), override -> {
			LocationData data = new LocationData(override, new HashSet<>(),	Map.of(ModifierDataType.BIOME, new HashMap<>()),
					new HashMap<>(), new HashMap<>(),new ArrayList<>(),	new HashMap<>(), new HashMap<>());				
			JsonObject raw = LocationData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get().getAsJsonObject();
			return gson.toJson(raw);}),
		ENCHANTMENTS("pmmo/enchantments", server -> ForgeRegistries.ENCHANTMENTS.getKeys(), override -> {
			return gson.toJson(EnhancementsData.CODEC.encodeStart(JsonOps.INSTANCE, 
					new EnhancementsData(override, new HashMap<>())).result().get());
		}),
		EFFECTS("pmmo/effects", server -> ForgeRegistries.MOB_EFFECTS.getKeys(), override -> {
			return gson.toJson(EnhancementsData.CODEC.encodeStart(JsonOps.INSTANCE, 
					new EnhancementsData(override, new HashMap<>())).result().get());
		}); 

		
		public String route;
		public Function<MinecraftServer, Set<ResourceLocation>> valueList;
		private Function<Boolean, String> defaultData;
		Category(String route, Function<MinecraftServer, Set<ResourceLocation>> values, Function<Boolean, String> defaultData) {
			this.route = route;
			this.valueList = values;
			this.defaultData = defaultData;
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
				Files.writeString(
						finalPath.resolve(id.getPath()+".json"), 
						category.defaultData.apply(withOverride),
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
