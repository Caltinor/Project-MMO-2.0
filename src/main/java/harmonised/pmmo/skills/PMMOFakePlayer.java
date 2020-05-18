package harmonised.pmmo.skills;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class PMMOFakePlayer extends FakePlayer
{
    public PlayerEntity player;

    public PMMOFakePlayer( ServerWorld world, GameProfile name, PlayerEntity player )
    {
        super(world, name);
        this.player = player;
    }
}
