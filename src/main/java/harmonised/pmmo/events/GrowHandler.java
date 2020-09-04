package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;

import java.util.Map;
import java.util.UUID;

public class GrowHandler
{
    public static void handleSaplingGrow( SaplingGrowTreeEvent event )
    {
        World world = (World) event.getWorld();
        BlockPos pos = event.getPos();
        UUID uuid = ChunkDataHandler.checkPos( event.getWorld().getDimension().getType().getRegistryName(), pos );
        PlayerEntity player = world.getServer().getPlayerList().getPlayerByUUID( uuid );

        if( player != null )
        {
            ResourceLocation resLoc = event.getWorld().getBlockState( pos ).getBlock().getRegistryName();
            Map<String, Double> award = XP.getXp( resLoc, JType.XP_VALUE_GROW );

            if( award.size() > 0 )
                XP.awardXpMapDouble( player, award, "Growing a Tree at " + pos, true, false );
        }
    }
}
