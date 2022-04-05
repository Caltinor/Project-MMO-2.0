package harmonised.pmmo.setup;

import harmonised.pmmo.commands.CmdPmmoRoot;
import harmonised.pmmo.config.readers.CoreParser;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.perks.PerkRegistration;
import harmonised.pmmo.features.loot_predicates.SkillLootConditionHighestSkill;
import harmonised.pmmo.features.loot_predicates.SkillLootConditionKill;
import harmonised.pmmo.features.loot_predicates.SkillLootConditionPlayer;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.storage.IChunkData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
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
	}
	
	@SubscribeEvent
	public static void onServerStartup(ServerStartingEvent event) {
		MsLoggy.info("Loading PMMO Saved Data");
		Core.get(LogicalSide.SERVER).getData(event.getServer());
		MsLoggy.info("Computing data for cache");
		Core.get(LogicalSide.SERVER).getData().computeLevelsForCache();
		MsLoggy.info("Executing Default Registrations");
		Core.get(LogicalSide.SERVER).registerNBT();
		MsLoggy.info("PMMO Server loading process complete");
	}
	
	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Reloading event) {
		if (event.getConfig().getType().equals(ModConfig.Type.SERVER))
			Core.get(LogicalSide.SERVER).getData().computeLevelsForCache();
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
	}
	
	@SubscribeEvent
	public static void onCapabilityAttach(AttachCapabilitiesEvent<LevelChunk> event) {
		event.addCapability(ChunkDataProvider.CHUNK_CAP_ID, new ChunkDataProvider());
	}
}
