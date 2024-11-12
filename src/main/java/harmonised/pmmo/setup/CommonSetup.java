package harmonised.pmmo.setup;

import com.mojang.serialization.Codec;
import harmonised.pmmo.commands.CmdPmmoRoot;
import harmonised.pmmo.compat.ftb_quests.FTBQHandler;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.readers.CoreLoader;
import harmonised.pmmo.config.scripting.Scripting;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.perks.PerkRegistration;
import harmonised.pmmo.features.loot_modifiers.SkillUpTrigger;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.setup.datagen.BlockTagProvider;
import harmonised.pmmo.setup.datagen.DamageTagProvider;
import harmonised.pmmo.setup.datagen.EntityTagProvider;
import harmonised.pmmo.setup.datagen.ItemTagProvider;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.setup.datagen.LangProvider.Locale;
import harmonised.pmmo.setup.datagen.defaultpacks.DefaultGLMProvider;
import harmonised.pmmo.setup.datagen.defaultpacks.EasyBlockConfigProvider;
import harmonised.pmmo.setup.datagen.defaultpacks.EasyConfigProvider;
import harmonised.pmmo.setup.datagen.defaultpacks.EasyGLMProvider;
import harmonised.pmmo.setup.datagen.defaultpacks.EasyItemConfigProvider;
import harmonised.pmmo.setup.datagen.defaultpacks.HardcoreGLMProvider;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.GAME)
public class CommonSetup {
	public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, Reference.MOD_ID);
	private static final Supplier<SkillUpTrigger> SKILL_UP_TRIGGER = TRIGGERS.register("skill_up", SkillUpTrigger::new);

	public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.createDataComponents(Reference.MOD_ID);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BREWED =
			DATA_COMPONENTS.register("brewed", () -> DataComponentType.<Boolean>builder()
					.persistent(Codec.BOOL)
					.networkSynchronized(ByteBufCodecs.BOOL)
					.build());

	public static void init(final FMLCommonSetupEvent event) {
		Networking.registerDataSyncPackets();
		PerkRegistration.init();
		//=========COMPAT=============
		if (ModList.get().isLoaded("ftbquests")) FTBQHandler.init();
	}
	
	@SubscribeEvent
	public static void onServerStartup(ServerStartingEvent event) {
		MsLoggy.INFO.log(LOG_CODE.LOADING, "Loading PMMO Saved Data");
		Core.get(LogicalSide.SERVER).getData();
		MsLoggy.INFO.log(LOG_CODE.LOADING, "PMMO Server loading process complete");
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
		event.addListener(Config.CONFIG);
	}
	
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		CompletableFuture<HolderLookup.Provider> reg = event.getLookupProvider();
		if (event.includeClient()) {
			for (Locale locale : LangProvider.Locale.values()) {
				generator.addProvider(true, new LangProvider(generator.getPackOutput(), locale.str));
			}
		}
		if (event.includeServer()) {
			//Easy Feature Pack Generators
			generator.addProvider(true, new EasyGLMProvider(generator.getPackOutput(), reg));
			generator.addProvider(true, new EasyItemConfigProvider(generator.getPackOutput()));
			generator.addProvider(true, new EasyBlockConfigProvider(generator.getPackOutput()));
			generator.addProvider(true, new EasyConfigProvider(generator.getPackOutput()));
			//Default Feature Pack Generators
			generator.addProvider(true, new DefaultGLMProvider(generator.getPackOutput(), reg));
			//Hardcore Feature Pack Generators
			generator.addProvider(true, new HardcoreGLMProvider(generator.getPackOutput(), reg));

			//Common mod data
			BlockTagProvider blockProvider = new BlockTagProvider(generator.getPackOutput(), reg, event.getExistingFileHelper());
			generator.addProvider(true, blockProvider);
			generator.addProvider(true, new EntityTagProvider(generator.getPackOutput(), reg, event.getExistingFileHelper()));
			generator.addProvider(true, new ItemTagProvider(generator.getPackOutput(), reg, blockProvider.contentsGetter(), event.getExistingFileHelper()));
			generator.addProvider(true, new DamageTagProvider(generator.getPackOutput(), reg, event.getExistingFileHelper()));
		}
	}
}
