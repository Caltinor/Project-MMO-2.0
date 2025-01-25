package harmonised.pmmo.compat.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotResult;

import java.util.ArrayList;
import java.util.List;

public class CuriosCompat {
    public static boolean hasCurio = false;
    //provide curio data to various methods
    public static List<ItemStack> getItems(Player player) {
        var cap = player.getCapability(CuriosCapability.INVENTORY);
        return cap == null ? new ArrayList<>() : cap.findCurios(b -> true).stream().map(SlotResult::stack).toList();
    }
}
