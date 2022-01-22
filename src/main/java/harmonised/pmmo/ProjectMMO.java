package harmonised.pmmo;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.setup.CommonSetup;
import harmonised.pmmo.util.Reference;

@Mod(Reference.MOD_ID)
public class ProjectMMO {

    public ProjectMMO() {
    	ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
    	
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::init);
    }
}
