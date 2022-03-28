package harmonised.pmmo.storage;

import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public interface IChunkData {
	 public void addPos(BlockPos blockPos, UUID uuid);
	 public void delPos(BlockPos blockPos);
	 public UUID checkPos(BlockPos pos);
	 public boolean playerMatchesPos(Player player, BlockPos pos);
	 public Map<BlockPos, UUID> getMap();
	 public void setMap(Map<BlockPos, UUID> map);
}
