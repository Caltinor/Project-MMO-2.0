package harmonised.pmmo;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.GlobalsConfig;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.anticheese.AntiCheeseConfig;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.loot_modifiers.GLMRegistry;
import harmonised.pmmo.setup.CommonSetup;
import harmonised.pmmo.setup.GameplayPacks;
import harmonised.pmmo.storage.DataAttachmentTypes;
import harmonised.pmmo.util.Reference;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(Reference.MOD_ID)
public class ProjectMMO {
	
    public ProjectMMO(IEventBus modbus) {
    	ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG); 
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AntiCheeseConfig.SERVER_CONFIG, "pmmo-AntiCheese.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AutoValueConfig.SERVER_CONFIG, "pmmo-AutoValues.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GlobalsConfig.SERVER_CONFIG, "pmmo-Globals.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SkillsConfig.SERVER_CONFIG, "pmmo-Skills.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PerksConfig.SERVER_CONFIG, "pmmo-Perks.toml");
		modbus.addListener(this::onConfigReload);
		modbus.addListener(this::onPackFind);
    	
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
			if (event.getConfig().getFileName().equalsIgnoreCase("pmmo-server.toml"))
				Core.get(LogicalSide.SERVER).getData().computeLevelsForCache();
			if (event.getConfig().getFileName().equalsIgnoreCase("pmmo-autovalues.toml"))
				AutoValues.resetCache();
		}
	}

	@SubscribeEvent
	public void onPackFind(AddPackFindersEvent event) {
		GameplayPacks.getPacks().stream().filter(holder -> holder.type().equals(event.getPackType())).forEach(holder -> {
			var resourcePath = ModList.get().getModFileById(Reference.MOD_ID).getFile().findResource("resourcepacks/%s".formatted(holder.id().getPath()));
			var pack = Pack.readMetaAndCreate("builtin/%s".formatted(holder.id().getPath()), holder.titleKey().asComponent(), holder.required(),
					new PathPackResources.PathResourcesSupplier(resourcePath,true), holder.type(), Pack.Position.BOTTOM, PackSource.FEATURE);
			event.addRepositorySource(consumer -> consumer.accept(pack));
		});
	}
}
