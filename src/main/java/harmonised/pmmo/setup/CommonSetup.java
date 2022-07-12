package harmonised.pmmo.setup;

import harmonised.pmmo.commands.CmdPmmoRoot;
import harmonised.pmmo.compat.curios.CurioCompat;
import harmonised.pmmo.compat.ftb_quests.FTBQHandler;
import harmonised.pmmo.config.readers.CoreParser;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.perks.PerkRegistration;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.loot_predicates.SkillLootConditionHighestSkill;
import harmonised.pmmo.features.loot_predicates.SkillLootConditionKill;
import harmonised.pmmo.features.loot_predicates.SkillLootConditionPlayer;
import harmonised.pmmo.features.veinmining.capability.IVeinCap;
import harmonised.pmmo.features.veinmining.capability.VeinProvider;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.storage.IChunkData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CommonSetup {
	
	public static void init(final FMLCommonSetupEvent event) {
		LootItemConditions.register("pmmo:skill_level_kill", new SkillLootConditionKill.Serializer());
		LootItemConditions.register("pmmo:skill_level", new SkillLootConditionPlayer.Serializer());
		LootItemConditions.register("pmmo:highest_skill", new SkillLootConditionHighestSkill.Serializer());
		Networking.registerMessages();
		Networking.registerDataSyncPackets();
		PerkRegistration.init();
		
		//=========COMPAT=============
		CurioCompat.hasCurio = ModList.get().isLoaded("curios");
		if (ModList.get().isLoaded("ftbquests")) FTBQHandler.init();
	}
	
	@SubscribeEvent
	public static void onServerStartup(ServerStartingEvent event) {
		MsLoggy.INFO.log(LOG_CODE.LOADING, "Loading PMMO Saved Data");
		Core.get(LogicalSide.SERVER).getData(event.getServer());
		MsLoggy.INFO.log(LOG_CODE.LOADING, "Computing data for cache");
		Core.get(LogicalSide.SERVER).getData().computeLevelsForCache();
		MsLoggy.INFO.log(LOG_CODE.LOADING, "Executing Default Registrations");
		Core.get(LogicalSide.SERVER).registerNBT();
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
		event.addListener(CoreParser.RELOADER);
		event.addListener(CoreParser.ITEM_LOADER);
		event.addListener(CoreParser.BLOCK_LOADER);
		event.addListener(CoreParser.ENTITY_LOADER);
		event.addListener(CoreParser.BIOME_LOADER);
		event.addListener(CoreParser.DIMENSION_LOADER);
		event.addListener(CoreParser.PLAYER_LOADER);
		event.addListener(CoreParser.ENCHANTMENT_LOADER);
	}
	
	public static void onCapabilityRegister(RegisterCapabilitiesEvent event) {
		event.register(IChunkData.class);
		event.register(IVeinCap.class);
	}
	
	@SubscribeEvent
	public static void onChunkCapabilityAttach(AttachCapabilitiesEvent<LevelChunk> event) {
		event.addCapability(ChunkDataProvider.CHUNK_CAP_ID, new ChunkDataProvider());
	}
	
	@SubscribeEvent
	public static void onItemCapabilityAttach(AttachCapabilitiesEvent<ItemStack> event) {
		event.addCapability(VeinProvider.VEIN_CAP_ID, new VeinProvider());
	}
	
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		if (event.includeClient()) {
			generator.addProvider(true, new LangProvider(generator, "en_us"));
		}
	}
}
