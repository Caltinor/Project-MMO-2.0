package harmonised.pmmo;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.GlobalsConfig;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.setup.CommonSetup;
import harmonised.pmmo.util.Reference;

@Mod(Reference.MOD_ID)
public class ProjectMMO {
	
    public ProjectMMO() {
    	ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG); 
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AutoValueConfig.SERVER_CONFIG, "pmmo-AutoValues.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GlobalsConfig.SERVER_CONFIG, "pmmo-Globals.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SkillsConfig.SERVER_CONFIG, "pmmo-Skills.toml");
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PerksConfig.SERVER_CONFIG, "pmmo-Perks.toml");

    	FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::init);
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::onCapabilityRegister);
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::gatherData);
    }
}
