package harmonised.pmmo.setup;

import harmonised.pmmo.commands.CmdPmmoRoot;
import harmonised.pmmo.config.readers.CoreParser;
import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonSetup {
	
	public static void init(FMLCommonSetupEvent event) {
		Networking.registerMessages();
	}
	
	public static void onServerStartup(ServerStartingEvent event) {
		MsLoggy.info("Loading PMMO Saved Data");
		PmmoSavedData.init(event.getServer());
		MsLoggy.info("Computing data for cache");
		XpUtils.computeLevelsForCache();
		MsLoggy.info("Loading settings from config jsons");
		CoreParser.init();
		MsLoggy.info("PMMO Server loading process complete");
	}
	
	public static void onConfigReload(ModConfigEvent.Reloading event) {
		XpUtils.computeLevelsForCache();
	}
	
	public static void onCommandRegister(RegisterCommandsEvent event) {
		CmdPmmoRoot.register(event.getDispatcher());
	}
}
