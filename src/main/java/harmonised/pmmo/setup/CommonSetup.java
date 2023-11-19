package harmonised.pmmo.setup;

import harmonised.pmmo.commands.CmdPmmoRoot;
import harmonised.pmmo.compat.curios.CurioCompat;
import harmonised.pmmo.compat.ftb_quests.FTBQHandler;
import harmonised.pmmo.config.readers.CoreLoader;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.perks.PerkRegistration;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.loot_modifiers.SkillUpTrigger;
import harmonised.pmmo.features.veinmining.capability.VeinHandler;
import harmonised.pmmo.features.veinmining.capability.VeinProvider;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.setup.datagen.BlockTagProvider;
import harmonised.pmmo.setup.datagen.DamageTagProvider;
import harmonised.pmmo.setup.datagen.EntityTagProvider;
import harmonised.pmmo.setup.datagen.GLMProvider;
import harmonised.pmmo.setup.datagen.ItemTagProvider;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.setup.datagen.LangProvider.Locale;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.storage.IChunkData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CommonSetup {
	
	public static void init(final FMLCommonSetupEvent event) {
		Networking.registerMessages();
		Networking.registerDataSyncPackets();
		PerkRegistration.init();
		
		event.enqueueWork(() -> CriteriaTriggers.register("pmmo:skill_up", SkillUpTrigger.SKILL_UP));
		//=========COMPAT=============
		// CurioCompat.hasCurio = ModList.get().isLoaded("curios");
		if (ModList.get().isLoaded("ftbquests")) FTBQHandler.init();
	}
	
	@SubscribeEvent
	public static void onServerStartup(ServerStartingEvent event) {
		MsLoggy.INFO.log(LOG_CODE.LOADING, "Loading PMMO Saved Data");
		Core.get(LogicalSide.SERVER).getData();
		MsLoggy.INFO.log(LOG_CODE.LOADING, "Computing data for cache");
		Core.get(LogicalSide.SERVER).getData().computeLevelsForCache();
		MsLoggy.INFO.log(LOG_CODE.LOADING, "PMMO Server loading process complete");
	}
	
	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Reloading event) {
		if (event.getConfig().getType().equals(ModConfig.Type.SERVER)) {
			if (event.getConfig().getFileName().equalsIgnoreCase("pmmo-server.toml"))
				Core.get(LogicalSide.SERVER).getData().computeLevelsForCache();
			if (event.getConfig().getFileName().equalsIgnoreCase("pmmo-autovalues.toml"))
				AutoValues.resetCache();
		}
	}
	
	@SubscribeEvent
	public static void onCommandRegister(RegisterCommandsEvent event) {
		CmdPmmoRoot.register(event.getDispatcher());
	}
	
	@SubscribeEvent
	public static void onAddReloadListeners(AddReloadListenerEvent event) {
		event.addListener(CoreLoader.RELOADER);
		event.addListener(Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER);
		event.addListener(Core.get(LogicalSide.SERVER).getLoader().BLOCK_LOADER);
		event.addListener(Core.get(LogicalSide.SERVER).getLoader().ENTITY_LOADER);
		event.addListener(Core.get(LogicalSide.SERVER).getLoader().BIOME_LOADER);
		event.addListener(Core.get(LogicalSide.SERVER).getLoader().DIMENSION_LOADER);
		event.addListener(Core.get(LogicalSide.SERVER).getLoader().PLAYER_LOADER);
		event.addListener(Core.get(LogicalSide.SERVER).getLoader().ENCHANTMENT_LOADER);
		event.addListener(Core.get(LogicalSide.SERVER).getLoader().EFFECT_LOADER);
	}
	
	public static void onCapabilityRegister(RegisterCapabilitiesEvent event) {
		event.register(IChunkData.class);
		event.register(VeinHandler.class);
	}
	
	@SubscribeEvent
	public static void onCapabilityAttach(AttachCapabilitiesEvent<LevelChunk> event) {
		event.addCapability(ChunkDataProvider.CHUNK_CAP_ID, new ChunkDataProvider());		
	}
	
	@SubscribeEvent
	public static void onPlayerCapabilityAttach(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player)
			event.addCapability(VeinProvider.VEIN_CAP_ID, new VeinProvider());
	}
	
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		if (event.includeClient()) {
			for (Locale locale : LangProvider.Locale.values()) {
				generator.addProvider(true, new LangProvider(generator.getPackOutput(), locale.str));
			}
		}
		if (event.includeServer()) {
			generator.addProvider(true, new GLMProvider(generator.getPackOutput()));
			BlockTagProvider blockProvider = new BlockTagProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper());
			generator.addProvider(true, blockProvider);
			generator.addProvider(true, new EntityTagProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
			generator.addProvider(true, new ItemTagProvider(generator.getPackOutput(), event.getLookupProvider(), blockProvider.contentsGetter(), event.getExistingFileHelper()));
			generator.addProvider(true, new DamageTagProvider(generator.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
		}
	}
}
