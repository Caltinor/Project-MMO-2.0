package harmonised.pmmo.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.world.entity.player.Player;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.pmmo.CTTickFunction")
@Document("mods/PMMO/CTTickFunction")
public interface CTTickFunction {
	/**This function represents the logic being executed
	 * for a perk during the tick stage.  If a perk has a
	 * duration, this will execute each tick.  the number
	 * of ticks elapsed is included to allow for tracking
	 * duration between executions.
	 *
	 * @param player perk executor
	 * @param nbt data provided by PMMO
	 * @param ticksElapsed the current number of elapsed ticks
	 * @return output data. not all events use output data, and your perk may not
	 * have anything to return.  In this case an empty map is sufficient.
	 */
	@ZenCodeType.Method
	MapData apply(Player player, MapData nbt, int ticksElapsed);
}
