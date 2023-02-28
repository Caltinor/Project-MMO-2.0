package harmonised.pmmo.compat.crafttweaker;

/*import org.openzen.zencode.java.ZenCodeType;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker_annotations.annotations.Document;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.pmmo.CTPerkFunction")
@Document("mods/pmmo/CTPerkFunction")*/
public interface CTPerkFunction {
	/**This function represents the logic being executed
	 * for a perk.  All perks are provided the player this
	 * perk is contextually applicable to, a map of settings
	 * and the players current level in the skill associated
	 * in the users' config.
	 * 
	 * @param player perk executor
	 * @param nbt data provided by PMMO
	 * @param level the current level of the player in the skill configured
	 * @return output data. not all events use output data, and your perk may not
	 * have anything to return.  In this case an empty map is sufficient.
	 */
	/*@ZenCodeType.Method
	MapData apply(Player player, MapData nbt, int level);*/
}
