package harmonised.pmmo.config.writers;

public class ConfigWriter {
	
	public static void writeDataToConfigFiles() {
		if (AutoValueWriter.writeAutoValues)
			AutoValueWriter.writeGeneratedAutoValuesToConfigFiles();
	}

}
