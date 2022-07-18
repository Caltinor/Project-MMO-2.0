package harmonised.pmmo.features.veinmining;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class VeinShapeData {
	private Level level;
	private BlockPos center;
	private int maxBlocks;
	private final Map<BlockPos, Node> map = new HashMap<>();
	
	public VeinShapeData(Level level, BlockPos center, int maxBlocks) {
		this.level = level;
		this.center = center;
		this.maxBlocks = maxBlocks;
		map.put(center, new Node(0, false, false));
	}
	
	public Set<BlockPos> getVein() {
		Block block = level.getBlockState(center).getBlock();
		int ring = 0;
		while (maxBlocks > 0) {
			addNodesForRing(ring, block);
			ring++;
		}
		map.remove(center);
		return map.keySet();
	}
	
	private void addNodesForRing(int ring, Block block) {
		Map<BlockPos, Node> ringMap = new HashMap<>();
		map.forEach((pos, node) -> {
			if (node.ring() == ring && !node.scanned() && !node.isTerminal()) 
				ringMap.put(pos, node);
		});
		if (ringMap.isEmpty()) {
			maxBlocks = 0;
			return;
		}
		ringMap.forEach((pos, node) -> {
			map.put(pos, node.setScanned());
			Map<BlockPos, Node> newRingMap = new HashMap<>();
			outer:
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if (maxBlocks <= 0)
							break outer;
						BlockPos currentPos = pos.offset(x, y, z);
						if (map.containsKey(currentPos)) continue; 
						if (level.getBlockState(currentPos).getBlock().equals(block)) {
							newRingMap.put(currentPos, new Node(ring+1, false, false));
							maxBlocks--;
						}
					}
				}
			}
			if (newRingMap.isEmpty())
				map.put(pos, node.setScanned().setTerminal());
			else
				map.putAll(newRingMap);
		});	
	}
	
	private record Node(int ring, boolean scanned, boolean isTerminal) {
		public Node setScanned() {return new Node(this.ring(), true, this.isTerminal());}
		public Node setTerminal() {return new Node(this.ring(), this.scanned(), true);}
	}
}
