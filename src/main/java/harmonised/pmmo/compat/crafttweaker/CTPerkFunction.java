package harmonised.pmmo.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker_annotations.annotations.Document;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.pmmo.CTPerkFunction")
@Document("mods/pmmo/CTPerkFunction")
public interface CTPerkFunction {
	
	@ZenCodeType.Method
	MapData apply(Player player, MapData nbt, int level);
}
