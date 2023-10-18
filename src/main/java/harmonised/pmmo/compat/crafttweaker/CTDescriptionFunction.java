package harmonised.pmmo.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.entity.player.Player;
import org.openzen.zencode.java.ZenCodeType;

import java.util.List;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.pmmo.CTDescriptionFunction")
@Document("mods/PMMO/CTDescriptionFunction")
public interface CTDescriptionFunction {
	/**This function consumes information about the perk
	 * and outputs text lines which give users specific
	 * details about what their perk is doing.  This info
	 * appears in the glossary and should be used to show
	 * players how much benefit they get from a perk such
	 * as the duration, power level, percent boost, etc
	 * they are currently receiving from this perk.
	 * 
	 * @param player perk executor
	 * @param nbt data provided by PMMO
	 * @return output text.
	 */
	@ZenCodeType.Method
	List<LiteralContents> apply(Player player, MapData nbt);
}
