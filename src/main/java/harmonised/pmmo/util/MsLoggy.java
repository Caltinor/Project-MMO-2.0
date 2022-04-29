package harmonised.pmmo.util;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import harmonised.pmmo.config.Config;

public class MsLoggy {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static enum LOG_CODE {
		API("api"),
		AUTO_VALUES("autovalues"),
		CHUNK("chunk"),
		DATA("data"),
		EVENT("event"),
		FEATURE("feature"),
		LOADING("loading"),
		NETWORK("network"),
		XP("xp"),
		//unused default
		NONE("none");
		
		public String code;
		LOG_CODE(String code) {this.code = code;}
	}
	
	public static void info(LOG_CODE code, String message, Object... obj) {
		if (Config.INFO_LOGGING.get().contains(code.code))
			LOGGER.info(message, obj);
	}
	
	public static void warn(LOG_CODE code, String message, Object... obj) {
		if (Config.WARN_LOGGING.get().contains(code.code))
			LOGGER.warn(message, obj);
	}
	
	public static void debug(LOG_CODE code, String message, Object... obj) {
		if (Config.DEBUG_LOGGING.get().contains(code.code))
			LOGGER.debug(message, obj);
	}
	
	public static void error(LOG_CODE code, String message, Object... obj) {
		if (Config.ERROR_LOGGING.get().contains(code.code))
			LOGGER.error(message, obj);
	}
	
	public static void fatal(LOG_CODE code, String message, Object... obj) {
		if (Config.FATAL_LOGGING.get().contains(code.code))
			LOGGER.fatal(message, obj);
	}
	
	//=============PRINTING UTILITIES=========================
	
	public static String mapToString(Map<?, ?> map) {
		String out = "";
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			out += "{"+entry.getKey().toString()+":"+entry.getValue().toString()+"}";
		}
		return out;
	}
	
	public static String listToString(List<?> list) {
		String out = "[";
		for (int i = 0; i < list.size(); i++) {
			out += list.get(i).toString();
		}
		out += "]";
		return out;
	}
}