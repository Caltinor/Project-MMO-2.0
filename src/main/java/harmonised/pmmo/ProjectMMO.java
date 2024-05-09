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
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.util.Optional;

@Mod(Reference.MOD_ID)
public class ProjectMMO {
    public ProjectMMO(IEventBus modbus) {
    	ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    	ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
		modbus.addListener(this::onConfigReload);
		modbus.addListener(this::onPackFind);
		modbus.addListener(Networking::registerMessages);
    	
    	GLMRegistry.CONDITIONS.register(modbus);
    	GLMRegistry.GLM.register(modbus);
		DataAttachmentTypes.ATTACHMENT_TYPES.register(modbus);
		CommonSetup.TRIGGERS.register(modbus);

    	modbus.addListener(CommonSetup::init);
    	modbus.addListener(CommonSetup::gatherData);
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
			var resourcePath = ModList.get().getModFileById(Reference.MOD_ID).getFile().findResource("resourcepacks/%s".formatted(holder.id().getPath()));
			var pack = Pack.readMetaAndCreate(
					new PackLocationInfo("builtin/%s".formatted(holder.id().getPath()), holder.titleKey().asComponent(), holder.source(), Optional.empty()),
					new PathPackResources.PathResourcesSupplier(resourcePath), holder.type(),
					new PackSelectionConfig(false, Pack.Position.BOTTOM, false));
			event.addRepositorySource(consumer -> consumer.accept(pack));
		});
	}
}
