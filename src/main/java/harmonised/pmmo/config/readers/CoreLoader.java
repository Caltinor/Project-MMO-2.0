package harmonised.pmmo.config.readers;

import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent.UpdateCause;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CoreLoader {
	private static final Logger DATA_LOGGER = LogManager.getLogger();	
	
	@SubscribeEvent
	public static void onTagLoad(TagsUpdatedEvent event) {
		Core core = Core.get(event.getUpdateCause() == UpdateCause.CLIENT_PACKET_RECEIVED ? LogicalSide.CLIENT : LogicalSide.SERVER);
		core.getLoader().ITEM_LOADER.postProcess(event.getRegistryAccess());
		core.getLoader().BLOCK_LOADER.postProcess(event.getRegistryAccess());
		core.getLoader().ENTITY_LOADER.postProcess(event.getRegistryAccess());
		//Until dimensions are stored as a client registry, this must remain commented.
		//core.getLoader().DIMENSION_LOADER.postProcess(event.getRegistryAccess());
		core.getLoader().BIOME_LOADER.postProcess(event.getRegistryAccess());
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
		Core.get(LogicalSide.SERVER).getLoader().resetData();
	});
	
	public void resetData() {
		ITEM_LOADER.clearData();
		BLOCK_LOADER.clearData();
		ENTITY_LOADER.clearData();
		BIOME_LOADER.clearData();
		DIMENSION_LOADER.clearData();
		PLAYER_LOADER.clearData();
		ENCHANTMENT_LOADER.clearData();
		EFFECT_LOADER.clearData();
	}
	
	public final MergeableCodecDataManager<ObjectData, Item> ITEM_LOADER = new MergeableCodecDataManager<>(
			"pmmo/items", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData, ObjectData::new, Registries.ITEM);
	public final MergeableCodecDataManager<ObjectData, Block> BLOCK_LOADER = new MergeableCodecDataManager<>(
			"pmmo/blocks", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData, ObjectData::new, Registries.BLOCK);
	public final MergeableCodecDataManager<ObjectData, EntityType<?>> ENTITY_LOADER = new MergeableCodecDataManager<>(
			"pmmo/entities", DATA_LOGGER, ObjectData.CODEC, this::mergeLoaderData, this::printData, ObjectData::new, Registries.ENTITY_TYPE);
	public final MergeableCodecDataManager<LocationData, Biome> BIOME_LOADER = new MergeableCodecDataManager<>(
			"pmmo/biomes", DATA_LOGGER, LocationData.CODEC, this::mergeLoaderData, this::printData, LocationData::new, Registries.BIOME);
	public final MergeableCodecDataManager<LocationData, Level> DIMENSION_LOADER = new MergeableCodecDataManager<>(
			"pmmo/dimensions", DATA_LOGGER, LocationData.CODEC, this::mergeLoaderData, this::printData, LocationData::new, Registries.DIMENSION);
	public final MergeableCodecDataManager<PlayerData, Player> PLAYER_LOADER = new MergeableCodecDataManager<>(
			"pmmo/players", DATA_LOGGER, PlayerData.CODEC, this::mergeLoaderData, this::printData, PlayerData::new, null);
	public final MergeableCodecDataManager<EnhancementsData, Enchantment> ENCHANTMENT_LOADER = new MergeableCodecDataManager<>(
			"pmmo/enchantments", DATA_LOGGER, EnhancementsData.CODEC, this::mergeLoaderData, this::printData, EnhancementsData::new, Registries.ENCHANTMENT);
	public final MergeableCodecDataManager<EnhancementsData, MobEffect> EFFECT_LOADER = new MergeableCodecDataManager<>(
			"pmmo/effects", DATA_LOGGER, EnhancementsData.CODEC, this::mergeLoaderData, this::printData, EnhancementsData::new, Registries.MOB_EFFECT);
	
	
	private <T extends DataSource<T>> T mergeLoaderData(final List<T> raws) {
		T out = raws.stream().reduce((existing, element) -> existing.combine(element)).get();
		return out.isUnconfigured() ? null : out;
	}
	
	private void printData(Map<ResourceLocation, ? extends Record> data) {
		data.forEach((id, value) -> {
			if (id == null || value == null) return;
			MsLoggy.INFO.log(LOG_CODE.DATA, "Object: {} with Data: {}", id.toString(), value.toString());
		});
	}
}