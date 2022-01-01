package harmonised.pmmo.core;

import java.util.UUID;

import harmonised.pmmo.storage.PmmoSavedData;

public class XpUtils {
	
	public static void setXpDiff(UUID playerID, String skillName, long change) {
		long newValue = PmmoSavedData.get().getXpRaw(playerID, skillName) + change;
		PmmoSavedData.get().setXpRaw(playerID, skillName, newValue);
	}

}
