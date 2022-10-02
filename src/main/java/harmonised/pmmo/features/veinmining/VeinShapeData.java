package harmonised.pmmo.features.veinmining;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class VeinShapeData {
	public static enum ShapeType {AOE, TUNNEL, BIG_TUNNEL}
	
	private Level level;
	private BlockPos center;
	private int maxBlocks;
	private final Map<BlockPos, Node> map = new HashMap<>();
	private final ShapeType mode;
	private final Direction face;
	
	public VeinShapeData(Level level, BlockPos center, int maxBlocks, ShapeType mode, Direction playerFacing) {
		this.level = level;
		this.center = center;
		this.maxBlocks = maxBlocks;
		this.mode = mode;
		this.face = playerFacing;
		map.put(center, new Node(0, false, false));
	}
	
	public Set<BlockPos> getVein() {
		Block block = level.getBlockState(center).getBlock();
		return switch (mode) {
		//STANDARD TRACING SHAPE
		case AOE -> {
			int ring = 0;
			while (maxBlocks > 0) {
				addNodesForRing(ring, block);
				ring++;
			}
			map.remove(center);
			yield map.keySet();
		}
		//TWO-HIGH TUNNEL
		case TUNNEL -> {
			final BlockState centerState = level.getBlockState(center);
			BlockPos lastPos = center;
			while (maxBlocks > 0 && !lastPos.equals(BlockPos.ZERO)) {
				lastPos = stepShape(lastPos, centerState, false);
			}
			map.remove(center);
			yield map.keySet();
		}
		case BIG_TUNNEL -> {
			final BlockState centerState = level.getBlockState(center);
			BlockPos lastPos = center;
			while (maxBlocks > 0 && !lastPos.equals(BlockPos.ZERO)) {
				lastPos = stepShape(lastPos, centerState, true);
			}
			map.remove(center);
			yield map.keySet();
		}
		default -> map.keySet();};
		
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
		public static Node NONE = new Node(0, false ,false);
	}
	
	private BlockPos stepShape(BlockPos from, BlockState centerState, boolean isBig) {
		boolean allFalse = true;
		if (level.getBlockState(from).getBlock().equals(centerState.getBlock())) {
			map.put(from, Node.NONE);
			maxBlocks--;
			allFalse = false;
		}
		if (maxBlocks > 0 && level.getBlockState(from.below()).getBlock().equals(centerState.getBlock())) {
			map.put(from.below(), Node.NONE);
			maxBlocks--;
			allFalse = false;
		}	
		
		if (isBig) {
			if (maxBlocks > 0 && level.getBlockState(from.above()).getBlock().equals(centerState.getBlock())) {
				map.put(from.above(), Node.NONE);
				maxBlocks--;
				allFalse = false;
			}
			if (maxBlocks > 0 && level.getBlockState(getAdjacent(from, true)).getBlock().equals(centerState.getBlock())) {
				map.put(getAdjacent(from, true), Node.NONE);
				maxBlocks--;
				allFalse = false;
			}
			if (maxBlocks > 0 && level.getBlockState(getAdjacent(from, true).above()).getBlock().equals(centerState.getBlock())) {
				map.put(getAdjacent(from, true).above(), Node.NONE);
				maxBlocks--;
				allFalse = false;
			}
			if (maxBlocks > 0 && level.getBlockState(getAdjacent(from, true).below()).getBlock().equals(centerState.getBlock())) {
				map.put(getAdjacent(from, true).below(), Node.NONE);
				maxBlocks--;
				allFalse = false;
			}
			if (maxBlocks > 0 && level.getBlockState(getAdjacent(from, false)).getBlock().equals(centerState.getBlock())) {
				map.put(getAdjacent(from, false), Node.NONE);
				maxBlocks--;
				allFalse = false;
			}
			if (maxBlocks > 0 && level.getBlockState(getAdjacent(from, false).above()).getBlock().equals(centerState.getBlock())) {
				map.put(getAdjacent(from, false).above(), Node.NONE);
				maxBlocks--;
				allFalse = false;
			}
			if (maxBlocks > 0 && level.getBlockState(getAdjacent(from, false).below()).getBlock().equals(centerState.getBlock())) {
				map.put(getAdjacent(from, false).below(), Node.NONE);
				maxBlocks--;
				allFalse = false;
			}
		}
		if (allFalse) return BlockPos.ZERO;
		return from.relative(face);
	}
	
	private BlockPos getAdjacent(BlockPos pos, boolean left) {
		return switch (face) {
		case NORTH -> {yield left ? pos.west() : pos.east();}
		case SOUTH -> {yield left ? pos.east() : pos.west();}
		case WEST -> {yield left ? pos.south() : pos.north();}
		case EAST -> {yield left ? pos.north() : pos.south();}
		default -> {yield pos;}};
	}
}
