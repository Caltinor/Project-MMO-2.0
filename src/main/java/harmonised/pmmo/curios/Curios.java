package harmonised.pmmo.curios;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
//import top.theillusivec4.curios.api.CuriosApi;
//import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.stream.Stream;

public class Curios
{
    /*private static final boolean isCuriosLoaded = ModList.get().isLoaded("curios");

    public static boolean isLoaded()
    {
        return isCuriosLoaded;
    }

    public static Stream<ICurioStacksHandler> getCurios(PlayerEntity player)
    {
        return isCuriosLoaded ? ActualCurios.getCurios(player) : Stream.empty();
    }

    private static class ActualCurios
    {
        public static Stream<ICurioStacksHandler> getCurios(PlayerEntity player)
        {
            return CuriosApi.getCuriosHelper().getCuriosHandler(player)
                    .map((curiosHandler) -> curiosHandler.getCurios().values().stream())
                    .orElse(Stream.empty());
        }
    }*/
}