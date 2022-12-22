package harmonised.pmmo.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker_annotations.annotations.Document;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.pmmo.CTPerkPredicate")
@Document("mods/pmmo/CTPerkPredicate")
public interface CTPerkPredicate {
	
	@ZenCodeType.Method
	boolean test(Player player, MapData nbt, int level);
}
