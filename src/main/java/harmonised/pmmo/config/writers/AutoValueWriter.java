package harmonised.pmmo.config.writers;

/* DESIGN:  
 * This was originally intended to be a class for adding runtime values to the configuration files based on a command-toggled
 * setting to do so.  It would accumulate settings and then write them to file when the server shut down.  Since the move to
 * datapacks, this is rather impractical, however players will likely still appreciate having a system for exporting the runtime
 * settings to a file which they can then use in a datapack since we cannot commit the changes directly to the configuration. 
 */
public class AutoValueWriter {	
	
	public static void dumpObjectConfigToFile() {
		
	}
}
