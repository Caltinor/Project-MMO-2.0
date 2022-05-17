package harmonised.pmmo.util;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.util.TriConsumer;

import harmonised.pmmo.config.Config;

public enum MsLoggy {
	INFO((code, message, args) -> {
		if (Config.INFO_LOGGING.get().contains(code.code))
			LogManager.getLogger().info(message, args);
	}), 
	WARN((code, message, args) -> {
		if (Config.WARN_LOGGING.get().contains(code.code))
			LogManager.getLogger().warn(message, args);
	}),  
	DEBUG((code, message, args) -> {
		if (Config.DEBUG_LOGGING.get().contains(code.code))
			LogManager.getLogger().debug(message, args);
	}), 
	ERROR((code, message, args) -> {
		if (Config.ERROR_LOGGING.get().contains(code.code))
			LogManager.getLogger().error(message, args);
	}), 
	FATAL((code, message, args) -> {
		if (Config.FATAL_LOGGING.get().contains(code.code))
			LogManager.getLogger().fatal(message, args);
	});
		
	private TriConsumer<LOG_CODE, String, Object[]> logExecutor;
	public void log(LOG_CODE code, String message, Object... obj) {logExecutor.accept(code, message, obj);}
	MsLoggy(TriConsumer<LOG_CODE, String, Object[]> logger) {this.logExecutor = logger;}	
	
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
	
	//=============RETURNABLE LOGGERS=========================
	//These loggers let you inline your logging
	//========================================================
	public <VALUE> VALUE logAndReturn(VALUE value, LOG_CODE code, String message) {
		this.logExecutor.accept(code, message, List.of(value).toArray());
		return value;
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