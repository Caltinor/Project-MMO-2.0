package harmonised.pmmo.config.readers;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent.UpdateCause;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.DEDICATED_SERVER)
public class CoreLoader {
	private static final Logger DATA_LOGGER = LogManager.getLogger();	
	
	@SubscribeEvent
	public static void onTagLoad(TagsUpdatedEvent event) {
		Core core = Core.get(event.getUpdateCause() == UpdateCause.CLIENT_PACKET_RECEIVED ? LogicalSide.CLIENT : LogicalSide.SERVER);
		if (event.shouldUpdateStaticData()) {
			core.getLoader().ITEM_LOADER.postProcess();
			core.getLoader().BLOCK_LOADER.postProcess();
			core.getLoader().ENTITY_LOADER.postProcess();
			//core.getLoader().DIMENSION_LOADER.postProcess();
			core.getLoader().BIOME_LOADER.postProcess();
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
	
	public MergeableCodecDataManager<?, ?> getLoader(ObjectType type) {
		return switch (type) {
		case ITEM -> ITEM_LOADER;
		case BLOCK -> BLOCK_LOADER;
		case ENTITY -> ENTITY_LOADER;
		case BIOME -> BIOME_LOADER;
		case DIMENSION -> DIMENSION_LOADER;
		case ENCHANTMENT -> ENCHANTMENT_LOADER;
		case EFFECT -> EFFECT_LOADER;
		case PLAYER -> PLAYER_LOADER;
		default -> null;};
	}
	
	public MergeableCodecDataManager<?, ?> getLoader(ModifierDataType type) {
		return switch(type) {
		case WORN, HELD -> ITEM_LOADER;
		case DIMENSION -> DIMENSION_LOADER;
		case BIOME -> BIOME_LOADER;
		default -> null;};
	}
	
	public static final ExecutableListener RELOADER = new ExecutableListener(() -> {
		//TODO change this to reset locally
		Core.get(LogicalSide.SERVER).resetDataForReload();
	});
	
	public final MergeableCodecDataManager<ObjectData, Item> ITEM_LOADER = new MergeableCodecDataManager<>(
			"pmmo/items", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData, ObjectData::new, ForgeRegistries.ITEMS);
	public final MergeableCodecDataManager<ObjectData, Block> BLOCK_LOADER = new MergeableCodecDataManager<>(
			"pmmo/blocks", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData, ObjectData::new, ForgeRegistries.BLOCKS);
	public final MergeableCodecDataManager<ObjectData, EntityType<?>> ENTITY_LOADER = new MergeableCodecDataManager<>(
			"pmmo/entities", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData, ObjectData::new, ForgeRegistries.ENTITY_TYPES);
	public final MergeableCodecDataManager<LocationData, Biome> BIOME_LOADER = new MergeableCodecDataManager<>(
			"pmmo/biomes", DATA_LOGGER, LocationData.CODEC, this::mergeLoaderData, this::printData, LocationData::new, ForgeRegistries.BIOMES);
	public final MergeableCodecDataManager<LocationData, Level> DIMENSION_LOADER = new MergeableCodecDataManager<>(
			"pmmo/dimensions", DATA_LOGGER, LocationData.CODEC, this::mergeLoaderData, this::printData, LocationData::new, null);
	public final MergeableCodecDataManager<PlayerData, Player> PLAYER_LOADER = new MergeableCodecDataManager<>(
			"pmmo/players", DATA_LOGGER, PlayerData.CODEC, this::mergeLoaderData, this::printData, PlayerData::new, null);
	public final MergeableCodecDataManager<EnhancementsData, Enchantment> ENCHANTMENT_LOADER = new MergeableCodecDataManager<>(
			"pmmo/enchantments", DATA_LOGGER, EnhancementsData.CODEC, this::mergeLoaderData, this::printData, EnhancementsData::new, ForgeRegistries.ENCHANTMENTS);
	public final MergeableCodecDataManager<EnhancementsData, MobEffect> EFFECT_LOADER = new MergeableCodecDataManager<>(
			"pmmo/effects", DATA_LOGGER, EnhancementsData.CODEC, this::mergeLoaderData, this::printData, EnhancementsData::new, ForgeRegistries.MOB_EFFECTS);
	
	
	private <T extends DataSource<T>> T mergeLoaderData(final List<T> raws) {
		T out = raws.stream().reduce((existing, element) -> existing.combine(element)).get();
		return out.isUnconfigured() ? null : out;
	}
	
	private void printData(Map<ResourceLocation, ? extends Record> data) {
		data.forEach((id, value) -> {MsLoggy.INFO.log(LOG_CODE.DATA, "Object: {} with Data: {}", id.toString(), value.toString());});
	}
}