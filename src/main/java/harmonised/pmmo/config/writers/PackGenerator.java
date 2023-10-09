package harmonised.pmmo.config.writers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.APIUtils.SalvageBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import harmonised.pmmo.util.Functions;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagFile;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

public class PackGenerator {
	public static final String PACKNAME = "generated_pack";
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static boolean applyOverride = false, applyDefaults = false, applyDisabler = false, applySimple = false;
	public static List<String> namespaceFilter = new ArrayList<>();
	public static Set<ServerPlayer> players = new HashSet<>();
	
	private enum Category {
		@SuppressWarnings("unchecked")
		ITEMS("pmmo/items", server -> ForgeRegistries.ITEMS.getKeys(), (id) -> {
			Core core = Core.get(LogicalSide.SERVER);
			ObjectData existing = core.getLoader().ITEM_LOADER.getData(id);
			
			ObjectData data = new ObjectData(applyOverride, new HashSet<>(),
					Arrays.stream(ReqType.ITEM_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r -> 
							applyDefaults 
								? existing.reqs().getOrDefault(r, AutoValues.getRequirements(r, id, ObjectType.ITEM))
								: new HashMap<>()))
						.entrySet().stream()
						.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
						.collect(Collectors.toMap(Map.Entry::getKey, e -> (Map<String, Integer>)e.getValue())),
					Arrays.stream(ReqType.ITEM_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r -> 
							applyDefaults
								? existing.nbtReqs().getOrDefault(r, new ArrayList<>())
								: new ArrayList<>()))
						.entrySet().stream()
						.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
						.collect(Collectors.toMap(Map.Entry::getKey, e -> (List<LogicEntry>)e.getValue())),
					applyDefaults ? existing.negativeEffects() : new HashMap<>(),
					Arrays.stream(EventType.ITEM_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e -> 
						applyDefaults 
							? existing.xpValues().getOrDefault(e, AutoValues.getExperienceAward(e, id, ObjectType.ITEM))
							: new HashMap<>()))
						.entrySet().stream().filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
						.collect(Collectors.toMap(Map.Entry::getKey, e -> (Map<String, Long>)e.getValue())),
					Stream.of(EventType.RECEIVE_DAMAGE, EventType.DEAL_DAMAGE).collect(Collectors.toMap(e -> e, e ->
							applyDefaults
									? existing.damageXpValues().getOrDefault(e, new HashMap<>())
									: new HashMap<>())),
					Arrays.stream(EventType.ITEM_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e -> 
							applyDefaults
								? existing.nbtXpValues().getOrDefault(e, new ArrayList<>())
								: new ArrayList<>()))
						.entrySet().stream()
						.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
						.collect(Collectors.toMap(Map.Entry::getKey, e -> (List<LogicEntry>)e.getValue())),
					Arrays.stream(new ModifierDataType[] {ModifierDataType.WORN, ModifierDataType.HELD})
						.collect(Collectors.toMap(m -> m, m -> applyDefaults ? existing.bonuses().getOrDefault(m, new HashMap<>()) : new HashMap<>()))
						.entrySet().stream()
						.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
						.collect(Collectors.toMap(Map.Entry::getKey, e -> (Map<String, Double>)e.getValue())),
					Arrays.stream(new ModifierDataType[] {ModifierDataType.WORN, ModifierDataType.HELD})
						.collect(Collectors.toMap(m -> m, m -> applyDefaults ? existing.nbtBonuses().getOrDefault(m, new ArrayList<>()) : new ArrayList<>()))
						.entrySet().stream()
						.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
						.collect(Collectors.toMap(Map.Entry::getKey, m -> (List<LogicEntry>)m.getValue())),
					applyDefaults ? existing.salvage() : Map.of(new ResourceLocation("modid:item"), SalvageBuilder.start().build()),
					applyDefaults ? existing.veinData() : VeinData.EMPTY);
			JsonObject raw = ObjectData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get().getAsJsonObject();
			return gson.toJson(raw);}),
		BLOCKS("pmmo/blocks", server -> ForgeRegistries.BLOCKS.getKeys(), (id) -> {
			Core core = Core.get(LogicalSide.SERVER);
			ObjectData existing = core.getLoader().BLOCK_LOADER.getData(id);

			ObjectData data = new ObjectData(applyOverride, new HashSet<>(),
					Arrays.stream(ReqType.BLOCK_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r ->
						applyDefaults
							? existing.reqs().getOrDefault(r, AutoValues.getRequirements(r, id, ObjectType.BLOCK))
							: new HashMap<>()))
						.entrySet().stream()
							.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (Map<String, Integer>)e.getValue())),
					Arrays.stream(ReqType.BLOCK_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r ->
							applyDefaults
									? existing.nbtReqs().getOrDefault(r, new ArrayList<>())
									: new ArrayList<>()))
							.entrySet().stream()
							.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (List<LogicEntry>)e.getValue())),
					new HashMap<>(), //negative effects
					Arrays.stream(EventType.BLOCK_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e ->
							applyDefaults
									? existing.xpValues().getOrDefault(e, AutoValues.getExperienceAward(e, id, ObjectType.BLOCK))
									: new HashMap<>()))
							.entrySet().stream().filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (Map<String, Long>)e.getValue())),
					new HashMap<>(), //damage events
					Arrays.stream(EventType.BLOCK_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e ->
							applyDefaults
									? existing.nbtXpValues().getOrDefault(e, new ArrayList<>())
									: new ArrayList<>()))
							.entrySet().stream()
							.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (List<LogicEntry>)e.getValue())),
					new HashMap<>(), //bonuses
					new HashMap<>(), //nbt bonuses
					new HashMap<>(), //salvage
					applyDefaults ? existing.veinData() : new VeinData(Optional.empty(), Optional.empty(), Optional.of(1)));
			JsonObject raw = ObjectData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get().getAsJsonObject();
			raw.remove("negative_effect");
			raw.remove("bonuses");
			raw.remove("dealt_damage_xp");
			raw.remove("received_damage_xp");
			raw.remove("nbt_bonuses");
			raw.remove("salvage");
			return gson.toJson(raw);}),
		ENTITIES("pmmo/entities", server -> ForgeRegistries.ENTITY_TYPES.getKeys(), (id) -> {
			Core core = Core.get(LogicalSide.SERVER);
			ObjectData existing = core.getLoader().ENTITY_LOADER.getData(id);

			ObjectData data = new ObjectData(applyOverride, new HashSet<>(),
					Arrays.stream(ReqType.ENTITY_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r ->
							applyDefaults
									? existing.reqs().getOrDefault(r, AutoValues.getRequirements(r, id, ObjectType.ENTITY))
									: new HashMap<>()))
							.entrySet().stream()
							.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (Map<String, Integer>)e.getValue())),
					Arrays.stream(ReqType.ENTITY_APPLICABLE_EVENTS).collect(Collectors.toMap(r -> r, r ->
							applyDefaults
									? existing.nbtReqs().getOrDefault(r, new ArrayList<>())
									: new ArrayList<>()))
							.entrySet().stream()
							.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (List<LogicEntry>)e.getValue())),
					new HashMap<>(), //negative effects
					Arrays.stream(EventType.ENTITY_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e ->
							applyDefaults
									? existing.xpValues().getOrDefault(e, AutoValues.getExperienceAward(e, id, ObjectType.ENTITY))
									: new HashMap<>()))
							.entrySet().stream().filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (Map<String, Long>)e.getValue())),
					Stream.of(EventType.RECEIVE_DAMAGE, EventType.DEAL_DAMAGE).collect(Collectors.toMap(e -> e, e ->
							applyDefaults
									? existing.damageXpValues().getOrDefault(e, new HashMap<>())
									: new HashMap<>())),
					Arrays.stream(EventType.ENTITY_APPLICABLE_EVENTS).collect(Collectors.toMap(e -> e, e ->
							applyDefaults
									? existing.nbtXpValues().getOrDefault(e, new ArrayList<>())
									: new ArrayList<>()))
							.entrySet().stream()
							.filter(entry -> (applySimple && !entry.getValue().isEmpty()) || !applySimple)
							.collect(Collectors.toMap(Map.Entry::getKey, e -> (List<LogicEntry>)e.getValue())),
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
				(id) -> {
				Core core = Core.get(LogicalSide.SERVER);
				LocationData existing = core.getLoader().DIMENSION_LOADER.getData(id);

				LocationData data = new LocationData(applyOverride, new HashSet<>(),
						applyDefaults ? existing.bonusMap() : Map.of(ModifierDataType.DIMENSION, new HashMap<>()),
						new HashMap<>(),
						new HashMap<>(),
						applyDefaults ? existing.veinBlacklist() : new ArrayList<>(),
						applyDefaults ? existing.travelReq() : new HashMap<>(),
						applyDefaults ? existing.mobModifiers() : new HashMap<>());
				JsonObject raw = LocationData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get().getAsJsonObject();
				raw.remove("positive_effect");
				raw.remove("negative_effect");
				return gson.toJson(raw);}),
		BIOMES("pmmo/biomes", server -> server.registryAccess().registryOrThrow(Registries.BIOME).keySet(), (id) -> {
			Core core = Core.get(LogicalSide.SERVER);
			LocationData existing = core.getLoader().BIOME_LOADER.getData(id);

			LocationData data = new LocationData(applyOverride, new HashSet<>(),
					applyDefaults ? existing.bonusMap() : Map.of(ModifierDataType.BIOME, new HashMap<>()),
					applyDefaults ? existing.positive() : new HashMap<>(),
					applyDefaults ? existing.negative() : new HashMap<>(),
					applyDefaults ? existing.veinBlacklist() : new ArrayList<>(),
					applyDefaults ? existing.travelReq() : new HashMap<>(),
					applyDefaults ? existing.mobModifiers() : new HashMap<>());
			JsonObject raw = LocationData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().get().getAsJsonObject();
			return gson.toJson(raw);}),
		ENCHANTMENTS("pmmo/enchantments", server -> ForgeRegistries.ENCHANTMENTS.getKeys(), (id) -> {
			Core core = Core.get(LogicalSide.SERVER);
			EnhancementsData existing = core.getLoader().ENCHANTMENT_LOADER.getData(id);

			return gson.toJson(EnhancementsData.CODEC.encodeStart(JsonOps.INSTANCE, 
					new EnhancementsData(applyOverride,
							applyDefaults ? existing.skillArray() : new HashMap<>())).result().get());
			}),
		EFFECTS("pmmo/effects", server -> ForgeRegistries.MOB_EFFECTS.getKeys(), (id) -> {
			Core core = Core.get(LogicalSide.SERVER);
			EnhancementsData existing = core.getLoader().EFFECT_LOADER.getData(id);

			return gson.toJson(EnhancementsData.CODEC.encodeStart(JsonOps.INSTANCE, 
					new EnhancementsData(applyOverride,
							applyDefaults ? existing.skillArray() : new HashMap<>())).result().get());
			}),
		TAGS("tags", server -> Set.of(
				Functions.pathPrepend(Reference.CROPS.location(), "blocks"),
				Functions.pathPrepend(Reference.CASCADING_BREAKABLES.location(), "blocks"),
				Functions.pathPrepend(Reference.ANIMAL_TAG.location(), "entity_types"),
				Functions.pathPrepend(Reference.BREEDABLE_TAG.location(), "entity_types"),
				Functions.pathPrepend(Reference.MOB_TAG.location(), "entity_types"),
				Functions.pathPrepend(Reference.RIDEABLE_TAG.location(), "entity_types"),
				Functions.pathPrepend(Reference.TAMABLE_TAG.location(), "entity_types"),
				Functions.pathPrepend(Reference.BREWABLES.location(), "items"),
				Functions.pathPrepend(Reference.SMELTABLES.location(), "items")), 
				(id) -> gson.toJson(TagFile.CODEC.encodeStart(JsonOps.INSTANCE, new TagFile(List.of(), false)).result().get())); 

		
		public String route;
		public Function<MinecraftServer, Set<ResourceLocation>> valueList;
		private Function<ResourceLocation, String> defaultData;
		Category(String route, Function<MinecraftServer, Set<ResourceLocation>> values, Function<ResourceLocation, String> defaultData) {
			this.route = route;
			this.valueList = values;
			this.defaultData = defaultData;
		}
	}
	
	private static final Filter defaultFilter = new Filter(List.of(new BlockFilter(Optional.empty(), Optional.of("pmmo"))));
	
	public static int generatePack(MinecraftServer server) {
		//create the filepath for our datapack.  this will do nothing if already created
		Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACKNAME);
		filepath.toFile().mkdirs();
		/* checks for existence of the pack.mcmeta.  This will:
		 * 1. create a new file if not present, using the disabler setting
		 * 2. overwrite the existing file if the disabler setting conflicts*/
		Path packPath = filepath.resolve("pack.mcmeta");
		try {
			Files.writeString(
				packPath, 
				gson.toJson(getPackObject(applyDisabler)), 
				Charset.defaultCharset(),
				StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);		
			
		} catch (IOException e) {System.out.println("Error While Generating pack.mcmeta for Generated Data: "+e.toString());}

		for (Category category : Category.values()) {
			Collection<ResourceLocation> filteredList = namespaceFilter.isEmpty() || category == Category.TAGS
					? category.valueList.apply(server)
					: category.valueList.apply(server).stream().filter(id -> namespaceFilter.contains(id.getNamespace())).toList();
			for (ResourceLocation id : filteredList) {
				int index = id.getPath().lastIndexOf('/');
				String pathRoute = id.getPath().substring(0, index >= 0 ? index : 0);
				Path finalPath = filepath.resolve("data/"+id.getNamespace()+"/"+category.route+"/"+pathRoute);
				finalPath.toFile().mkdirs();
				try {					
					Files.writeString(
						finalPath.resolve(id.getPath().substring(id.getPath().lastIndexOf('/')+1)+".json"), 
						category.defaultData.apply(id),
						Charset.defaultCharset(),
						StandardOpenOption.CREATE_NEW,
						StandardOpenOption.WRITE);
				} catch (IOException e) {System.out.println("Error While Generating Pack File For: "+id.toString()+" ("+e.toString()+")");}
			}			
		}
		generatePlayerConfigs(server, players);
		return 0;
	}
	
	public static int generatePlayerConfigs(MinecraftServer server, Collection<ServerPlayer> players) {
		Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACKNAME+"/data/minecraft/pmmo/players/");
		filepath.toFile().mkdirs();
		for (ServerPlayer player : players) {
			String idString = player.getUUID().toString();
			try {
				Files.writeString(
						filepath.resolve(idString+".json"), 
						gson.toJson(PlayerData.CODEC.encodeStart(JsonOps.INSTANCE, new PlayerData()).result().get()),
						Charset.defaultCharset(),
						StandardOpenOption.CREATE_NEW,
						StandardOpenOption.WRITE);
			} catch (IOException e) {System.out.println("Error While Generating Pack File For: "+idString.toString()+" ("+e.toString()+")");}
		}
		return 0;
	}
	
	private static record Pack(String description, int format) {
		public static final Codec<Pack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("description").forGetter(Pack::description),
				Codec.INT.fieldOf("pack_format").forGetter(Pack::format)
				).apply(instance, Pack::new));
	}
	private static record BlockFilter(Optional<String> namespace, Optional<String> path) {
		public static final Codec<BlockFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("namespace").forGetter(BlockFilter::namespace),
				Codec.STRING.optionalFieldOf("path").forGetter(BlockFilter::path)
				).apply(instance, BlockFilter::new));
	}
	private static record Filter(List<BlockFilter> block) {
		public static final Codec<Filter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				BlockFilter.CODEC.listOf().fieldOf("block").forGetter(Filter::block)
				).apply(instance, Filter::new));
	}
	private static record McMeta(Pack pack, Optional<Filter> filter) {
		public static final Codec<McMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Pack.CODEC.fieldOf("pack").forGetter(McMeta::pack),
				Filter.CODEC.optionalFieldOf("filter").forGetter(McMeta::filter)
				).apply(instance, McMeta::new));
	}
	private static JsonElement getPackObject(boolean isDisabler) {
		McMeta pack = new McMeta(
				new Pack(isDisabler 
					? "Generated Resources including a disabler filter for PMMO's defaults"
					: "Generated Resources",
					9),
				isDisabler 
					? Optional.of(defaultFilter)
					: Optional.empty());
		
		return McMeta.CODEC.encodeStart(JsonOps.INSTANCE, pack).result().get();
	}
}
