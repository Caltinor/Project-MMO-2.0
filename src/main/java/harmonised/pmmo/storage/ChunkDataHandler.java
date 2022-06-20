package harmonised.pmmo.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;


public class ChunkDataHandler implements IChunkData{
	private Map<BlockPos, UUID> placedMap = new HashMap<>();

	@Override
    public void addPos(BlockPos blockPos, UUID uuid) {placedMap.put(blockPos, uuid);}
	@Override
    public void delPos(BlockPos blockPos) {placedMap.remove(blockPos);}
	@Override
    public UUID checkPos(BlockPos pos) {return placedMap.getOrDefault(pos, Reference.NIL);}
    @Override
    public boolean playerMatchesPos(Player player, BlockPos pos) {
    	return placedMap.containsKey(pos) && placedMap.get(pos).equals(player.getUUID());
    }
    
	@Override
	public Map<BlockPos, UUID> getMap() {return placedMap;}
	@Override
	public void setMap(Map<BlockPos, UUID> map) {placedMap = map;}
}
