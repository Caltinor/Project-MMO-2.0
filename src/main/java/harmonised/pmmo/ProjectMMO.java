package harmonised.pmmo;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.GlobalsConfig;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.features.anticheese.AntiCheeseConfig;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.features.loot_modifiers.GLMRegistry;
import harmonised.pmmo.setup.CommonSetup;
import harmonised.pmmo.util.Reference;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.MOD_ID)
public class ProjectMMO {
	
    public ProjectMMO() {
    	ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG); 
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AntiCheeseConfig.SERVER_CONFIG, "pmmo-AntiCheese.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AutoValueConfig.SERVER_CONFIG, "pmmo-AutoValues.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GlobalsConfig.SERVER_CONFIG, "pmmo-Globals.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SkillsConfig.SERVER_CONFIG, "pmmo-Skills.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PerksConfig.SERVER_CONFIG, "pmmo-Perks.toml");
    	
    	GLMRegistry.CONDITIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
    	GLMRegistry.GLM.register(FMLJavaModLoadingContext.get().getModEventBus());

    	FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::init);
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::onCapabilityRegister);
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::gatherData);
    }
}
