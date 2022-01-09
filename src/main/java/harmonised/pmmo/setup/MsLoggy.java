package harmonised.pmmo.setup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import harmonised.pmmo.config.Config;

public class MsLoggy {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void info(String message, Object... obj) {
		if (Config.ADV_LOGGING.get())
			LOGGER.info(message, obj);
	}
	
	public static void warn(String message, Object... obj) {
		if (Config.ADV_LOGGING.get())
			LOGGER.warn(message, obj);
	}
	
	public static void debug(String message, Object... obj) {
		if (Config.ADV_LOGGING.get())
			LOGGER.debug(message, obj);
	}
	
	public static void error(String message, Object... obj) {
		if (Config.ADV_LOGGING.get())
			LOGGER.error(message, obj);
	}
	
	public static void fatal(String message, Object... obj) {
		if (Config.ADV_LOGGING.get())
			LOGGER.fatal(message, obj);
	}
}