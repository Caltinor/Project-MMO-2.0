package harmonised.pmmo.config.readers;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.registry.ConfigurationRegistry;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.DEDICATED_SERVER)
public class CoreParser {
	private static final Logger DATA_LOGGER = LogManager.getLogger();	
	
	@SubscribeEvent
	public static void onTagLoad(TagsUpdatedEvent event) {
		if (event.shouldUpdateStaticData()) {
			
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends DataSource<T>> void applyData(ObjectType type, Map<ResourceLocation, T> data) {
		switch (type) {
		case ITEM -> {ITEM_LOADER.data.putAll((Map<? extends ResourceLocation, ? extends ObjectData>) data);}
		case BLOCK -> {BLOCK_LOADER.data.putAll((Map<? extends ResourceLocation, ? extends ObjectData>) data);}
		case ENTITY -> {ENTITY_LOADER.data.putAll((Map<? extends ResourceLocation, ? extends ObjectData>) data);}
		case DIMENSION -> {DIMENSION_LOADER.data.putAll((Map<? extends ResourceLocation, ? extends LocationData>) data);}
		case BIOME -> {BIOME_LOADER.data.putAll((Map<? extends ResourceLocation, ? extends LocationData>) data);}
		case PLAYER -> {PLAYER_LOADER.data.putAll((Map<? extends ResourceLocation, ? extends PlayerData>) data);}
		case ENCHANTMENT -> {ENCHANTMENT_LOADER.data.putAll((Map<? extends ResourceLocation, ? extends EnhancementsData>) data);}
		case EFFECT -> {EFFECT_LOADER.data.putAll((Map<? extends ResourceLocation, ? extends EnhancementsData>) data);}
		default -> {}}
		printData((Map<ResourceLocation, ? extends Record>) data);
	}
	
	public static final ExecutableListener RELOADER = new ExecutableListener(() -> {
		Core.get(LogicalSide.SERVER).resetDataForReload();
	});
	public static final ExecutableListener DEFAULT_CONFIG = new ExecutableListener(() -> {
		MsLoggy.INFO.log(LOG_CODE.DATA, "Configuration Defaults from API Applied");
		ConfigurationRegistry.get().applyDefaults(Core.get(LogicalSide.SERVER));
	});
	public static final ExecutableListener OVERRIDE_CONFIG = new ExecutableListener(() -> {
		MsLoggy.INFO.log(LOG_CODE.DATA, "Configuration Overrides from API Applied");
		ConfigurationRegistry.get().applyOverrides(Core.get(LogicalSide.SERVER));
	});
	
	public final MergeableCodecDataManager<ObjectData> ITEM_LOADER = new MergeableCodecDataManager<>(
			"pmmo/items", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData);
	public final MergeableCodecDataManager<ObjectData> BLOCK_LOADER = new MergeableCodecDataManager<>(
			"pmmo/blocks", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData);
	public final MergeableCodecDataManager<ObjectData> ENTITY_LOADER = new MergeableCodecDataManager<>(
			"pmmo/entities", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData);
	public final MergeableCodecDataManager<LocationData> BIOME_LOADER = new MergeableCodecDataManager<>(
			"pmmo/biomes", DATA_LOGGER, LocationData.CODEC, this::mergeLoaderData, this::printData);
	public final MergeableCodecDataManager<LocationData> DIMENSION_LOADER = new MergeableCodecDataManager<>(
			"pmmo/dimensions", DATA_LOGGER, LocationData.CODEC, this::mergeLoaderData, this::printData);
	public final MergeableCodecDataManager<PlayerData> PLAYER_LOADER = new MergeableCodecDataManager<>(
			"pmmo/players", DATA_LOGGER, PlayerData.CODEC, this::mergeLoaderData, this::printData);
	public final MergeableCodecDataManager<EnhancementsData> ENCHANTMENT_LOADER = new MergeableCodecDataManager<>(
			"pmmo/enchantments", DATA_LOGGER, EnhancementsData.CODEC, this::mergeLoaderData, this::printData);
	public final MergeableCodecDataManager<EnhancementsData> EFFECT_LOADER = new MergeableCodecDataManager<>(
			"pmmo/effects", DATA_LOGGER, EnhancementsData.CODEC, this::mergeLoaderData, this::printData);
	
	
	private <T extends DataSource<T>> T mergeLoaderData(final List<T> raws) {
		T out = raws.stream().reduce((existing, element) -> existing.combine(element)).get();
		return out.isUnconfigured() ? null : out;
	}
	
	private void printData(Map<ResourceLocation, ? extends Record> data) {
		data.forEach((id, value) -> {MsLoggy.INFO.log(LOG_CODE.DATA, "Object: {} with Data: {}", id.toString(), value.toString());});
	}
}