package harmonised.pmmo.config;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import net.minecraftforge.common.ForgeConfigSpec;

public class GlobalsConfig {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	static {
		generateDefaults();
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

		buildGlobals(SERVER_BUILDER);
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	public static ConfigObject<Map<String, String>> PATHS;
	public static ConfigObject<Map<String, String>> CONSTANTS;
	
	private static Map<String, String> pathDefaults;
	private static Map<String, String> constantDefaults;
	
	private static void buildGlobals(ForgeConfigSpec.Builder builder) {
		builder.comment("Configuration for commonly used NBT global variables").push("Globals");
		
		PATHS = TomlConfigHelper.defineObject(builder, "paths", Codec.unboundedMap(Codec.STRING, Codec.STRING), pathDefaults);
		CONSTANTS = TomlConfigHelper.defineObject(builder, "constants", Codec.unboundedMap(Codec.STRING, Codec.STRING), constantDefaults);
		
		builder.pop();
	}
	
	private static void generateDefaults() {
		pathDefaults = new HashMap<>();
		pathDefaults.put("tmat0", "tic_materials[0]");
		pathDefaults.put("tmat1", "tic_materials[1]");
		pathDefaults.put("tmat2", "tic_materials[2]");
		pathDefaults.put("tmat3", "tic_materials[3]");
		pathDefaults.put("sgmats", "SGear_Data{}.Construction{}.Parts[].Item{}.tag{}.Materials[].ID");
		
		constantDefaults = new HashMap<>();
		constantDefaults.put("example", "value");
	}
}
