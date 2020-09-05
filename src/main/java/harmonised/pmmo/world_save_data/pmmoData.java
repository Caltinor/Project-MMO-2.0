package harmonised.pmmo.world_save_data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.Objects;

public class pmmoData extends WorldSavedData
{
    public static String NAME = "pmmo";
    public pmmoData()
    {
        super( NAME );
    }

    @Override
    public void read( CompoundNBT data )
    {

    }

    @Override
    public CompoundNBT write( CompoundNBT data )
    {
        return data;
    }

    @Nonnull
    public static pmmoData create(@Nonnull final IWorld world )
    {
        if ( !(world instanceof ServerWorld) ) throw new IllegalArgumentException( "Unable to obtain a WorldSavedData instance on the client" );
        final ServerWorld overworld = Objects.requireNonNull( ((ServerWorld) world).getServer().getWorld( World.OVERWORLD ), "Overworld does not exist... how even?" );
        return overworld.getSavedData().getOrCreate( pmmoData::new, NAME );
    }
}
