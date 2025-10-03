package harmonised.pmmo.util;

import harmonised.pmmo.config.Config;
import org.apache.logging.log4j.LogManager;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public enum MsLoggy {
	INFO(code -> Config.INFO_LOGGING.get().contains(code.code),
		(message, args) -> LogManager.getLogger().info(message, args)), 
	WARN(code -> Config.WARN_LOGGING.get().contains(code.code),
		(message, args) -> LogManager.getLogger().warn(message, args)),  
	DEBUG(code -> Config.DEBUG_LOGGING.get().contains(code.code),
		(message, args) -> LogManager.getLogger().debug(message, args)), 
	ERROR(code -> Config.ERROR_LOGGING.get().contains(code.code),
		(message, args) -> LogManager.getLogger().error(message, args)), 
	FATAL(code -> Config.FATAL_LOGGING.get().contains(code.code),
		(message, args) -> LogManager.getLogger().fatal(message, args));
		
	private Predicate<LOG_CODE> validator;
	private BiConsumer<String, Object[]> logExecutor;
	MsLoggy(Predicate<LOG_CODE> validator, BiConsumer<String, Object[]> logger) {
		this.validator = validator; 
		this.logExecutor = logger;
	}	
	
	/**<p>These codes are used exclusively by {@link MsLoggy} to
	 * specify which game function category to log.  This combines
	 * with the logging level to create a granular array of logging
	 * options for users.</p>
	 * <p>Internally this enum maps a code-referable value to a
	 * config string.</p>
	 * 
	 * @author Caltinor
	 *
	 */
	public static enum LOG_CODE {
		API("api"),
		AUTO_VALUES("autovalues"),
		CHUNK("chunk"),
		DATA("data"),
		EVENT("event"),
		FEATURE("feature"),
		PERKS("perks"),
		GUI("gui"),
		LOADING("loading"),
		NETWORK("network"),
		XP("xp"),
		REQ("req"),
		//unused default
		NONE("none");
		
		/**The config equivalent string for this logging code.*/
		public String code;
		LOG_CODE(String code) {this.code = code;}
	}
	
	/**<p>Logs the provided string.</p>
	 * <p>The string can be a formatted string, in which case
	 * the varargs will be used as format arguments.</p>
	 * 
	 * @param code the code category this should log for
	 * @param message the message to display
	 * @param obj format arguments
	 */
	public void log(LOG_CODE code, String message, Object... obj) {
		if (validator.test(code))
			logExecutor.accept(message, obj);
	}
	
	/**<p>For every entry in the collection, logs the provided string.</p>
	 * <p>The string can be a formatted string, in which case
	 * the varargs will be used as format arguments.</p>
	 * <p><b>IMPORTANT: The entry value will always be the first vararg</b></p>
	 * 
	 * @param <T> the entry type
	 * @param code the code category this should log for
	 * @param array the collection being logged
	 * @param message the message to display per entry
	 * @param obj any static arguments supplied to each message
	 */
	public <T> void log(LOG_CODE code, Collection<T> array, String message, Object... obj) {
		if (validator.test(code)) 
			array.forEach(entry -> {
				Object[] params = new Object[obj.length+1];
				params[0] = entry;
				for (int i = 0; i < obj.length; i++) {params[i+1] = obj[i];}
				logExecutor.accept(message, params);
			});
		
	}
	
	/**<p>For every entry in the map, logs the provided string.</p>
	 * <p>The string can be a formatted string, in which case
	 * the varargs will be used as format arguments.</p>
	 * <p><b>IMPORTANT: The entry key and value will always be the 
	 * first two varargs</b></p>
	 * 
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param code the code category this should log for
	 * @param map the map being logged
	 * @param message the message to display for each entry
	 * @param obj any static arguments supplied to each message
	 */
	public <K, V> void log(LOG_CODE code, Map<K, V> map, String message, Object... obj) {
		if (validator.test(code))
			map.forEach((key, value) -> {
				Object[] params = new Object[obj.length+2];
				params[0] = key;
				params[1] = value;
				for (int i = 0; i < obj.length; i++) {params[i+2] = obj[i];}
				logExecutor.accept(message, params);
			});
	}
	
	//=============RETURNABLE LOGGERS=========================
	//These loggers let you inline your logging
	//========================================================
	public <VALUE> VALUE logAndReturn(VALUE value, LOG_CODE code, String message, Object... obj) {
		if (validator.test(code)) {
			Object[] params = new Object[obj.length+1];
			params[0] = value;
			for (int i = 0; i < obj.length; i++) {params[i+1] = obj[i];}
			this.logExecutor.accept(message, params);
		}
		return value;
	}
	//=============PRINTING UTILITIES=========================
	/**Creates a readable string from a map instance
	 * 
	 * @param map the map being parsed
	 * @return the string of key-value pairs
	 */
	public static String mapToString(Map<?, ?> map) {
		String out = "";
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			out += "{"+entry.getKey().toString()+":"+entry.getValue().toString()+"}";
		}
		return out;
	}
	
	/**Creates a readable string from a collection instance
	 * 
	 * @param list the collection being parsed
	 * @return the string of entries
	 */
	public static <T> String listToString(Collection<T> list) {
		String out = "[";
		for (T entry : list) {
			out += entry.toString() +", ";
		}
		out += "]";
		return out;
	}
}