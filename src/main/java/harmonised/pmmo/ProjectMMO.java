package harmonised.pmmo;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.loot_modifiers.GLMRegistry;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.setup.CommonSetup;
import harmonised.pmmo.setup.GameplayPacks;
import harmonised.pmmo.storage.DataAttachmentTypes;
import harmonised.pmmo.util.Reference;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.resource.JarContentsPackResources;

import java.util.Optional;

@Mod(Reference.MOD_ID)
public class ProjectMMO {
    public ProjectMMO(IEventBus modbus, ModContainer container) {
    	ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    	ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
		modbus.addListener(this::onConfigReload);
		modbus.addListener(this::onPackFind);
		modbus.addListener(Networking::registerMessages);
    	
    	GLMRegistry.CONDITIONS.register(modbus);
    	GLMRegistry.GLM.register(modbus);
		DataAttachmentTypes.ATTACHMENT_TYPES.register(modbus);
		CommonSetup.TRIGGERS.register(modbus);
		CommonSetup.DATA_COMPONENTS.register(modbus);
		CommonSetup.ATTRIBUTES.register(modbus);

    	modbus.addListener(CommonSetup::init);
    	modbus.addListener(CommonSetup::gatherData);
		modbus.addListener(CommonSetup::addAttributes);

//		final MutableTestFramework framework = FrameworkConfiguration.builder(Reference.rl("tests")) // The ID of the framework. Used by logging, primarily
//				.clientConfiguration(() -> ClientConfiguration.builder() // Client-side compatibility configuration. This is COMPLETLY optional, but it is recommended for ease of use.
//						.toggleOverlayKey(GLFW.GLFW_KEY_J) // The key used to toggle the tests overlay
//						.openManagerKey(GLFW.GLFW_KEY_N) // The key used to open the Test Manager screen
//						.build())
//				.build().create(); // Build and store the InternalTestFramework. We use the "internal" version because we want to access methods not usually exposed, like the init method

		// Initialise this framework, using the mod event bus of the currently loading mod, and the container of the currently loading mod.
		// The container is used for collecting annotations.
		// This method will collect and register tests, structure templates, group data, and will fire init listeners.
//		framework.init(modbus, container);
//
//		// Register the commands of the framework under the `tests` top-level command.
//		NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
//			final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
//			framework.registerCommands(node);
//			event.getDispatcher().register(node);
//		});
    }

	@SubscribeEvent
	public void onConfigReload(ModConfigEvent.Reloading event) {
		if (event.getConfig().getType().equals(ModConfig.Type.SERVER)) {
			if (event.getConfig().getFileName().equalsIgnoreCase("pmmo-autovalues.toml"))
				AutoValues.resetCache();
		}
	}

	@SubscribeEvent
	public void onPackFind(AddPackFindersEvent event) {
		GameplayPacks.getPacks().stream().filter(holder -> holder.type().equals(event.getPackType())).forEach(holder -> {
			JarContentsPackResources.JarContentsResourcesSupplier supplier = new JarContentsPackResources.JarContentsResourcesSupplier(
					ModList.get().getModFileById(Reference.MOD_ID).getFile().getContents(),
					"resourcepacks/%s".formatted(holder.id().getPath()));
			Pack pack = Pack.readMetaAndCreate(
					new PackLocationInfo("builtin/%s".formatted(holder.id().getPath()), holder.titleKey().asComponent(), holder.source(), Optional.empty()),
					supplier, holder.type(),
					new PackSelectionConfig(false, Pack.Position.BOTTOM, false));
			event.addRepositorySource(consumer -> consumer.accept(pack));
		});
	}
}
