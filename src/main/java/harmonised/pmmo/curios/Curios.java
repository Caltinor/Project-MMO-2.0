package harmonised.pmmo.curios;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.stream.Stream;

public class Curios
{
    private static final boolean isCuriosLoaded = ModList.get().isLoaded("curios");

    public static boolean isLoaded()
    {
        return isCuriosLoaded;
    }

    public static Stream<IItemHandler> getCurios(EntityPlayer player)
    {
        return isCuriosLoaded ? ActualCurios.getCurios(player) : Stream.empty();
    }

    private static class ActualCurios
    {
        public static Stream<IItemHandler> getCurios(EntityPlayer player)
        {
            return CuriosAPI.getCuriosHandler(player)
                    .map((curiosHandler) -> curiosHandler.getCurioMap().values().stream().map(c -> (IItemHandler) c))
                    .orElse(Stream.empty());
        }
    }
}
