package harmonised.pmmo.baubles;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.util.HashSet;
import java.util.Set;

public class BaublesHandler
{
    private static final boolean isBaublesLoaded = Loader.isModLoaded( "baubles" );

    public static Set<ItemStack> getBaublesItems( EntityPlayer player )
    {
        IBaublesItemHandler baublesItemHandler = BaublesApi.getBaublesHandler( player );
        Set<ItemStack> items = new HashSet<>();

        for( int i = 0; i < baublesItemHandler.getSlots(); ++i )
        {
            items.add( baublesItemHandler.getStackInSlot( i ) );
        }

        return items;
    }

    public static boolean isLoaded()
    {
        return isBaublesLoaded;
    }
}
