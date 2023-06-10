package harmonised.pmmo.client.utils;

import java.util.Collections;
import java.util.Set;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinShapeData;
import harmonised.pmmo.features.veinmining.VeinShapeData.ShapeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.LogicalSide;

public class VeinTracker {
	private static Set<BlockPos> vein;
	public static BlockPos currentTarget;
	public static double currentCharge;
	public static ShapeType mode = ShapeType.AOE;
	
	public static void setTarget(BlockPos pos) {		
		currentTarget = currentTarget == null ? pos : currentTarget.equals(pos) ? BlockPos.ZERO : pos;
	}
	
	public static void nextMode() {
		mode = mode.ordinal() == ShapeType.values().length-1 ? ShapeType.values()[0] : ShapeType.values()[mode.ordinal()+1];
	}
	
	public static boolean isLookingAtVeinTarget(HitResult hitResult) {
		if (!(hitResult instanceof BlockHitResult) || currentTarget == null)
			return false;
		BlockHitResult bhr = (BlockHitResult)hitResult;
		if (currentTarget.equals(bhr.getBlockPos()))
			return true;
		return false;
	}
	
	public static Set<BlockPos> getVein() {
		if (vein == null)
			return Collections.emptySet();
		return vein;
	}
	
	public static int getCurrentCharge() {return (int)currentCharge;}
	
	public static void updateVein(Player player) {
		Block block = player.level().getBlockState(currentTarget).getBlock();
		int perBlock = Core.get(LogicalSide.CLIENT).getBlockConsume(block);
		int maxBlocks = perBlock <= 0 ? 0 : getCurrentCharge()/perBlock;
		vein = new VeinShapeData(player.level(), currentTarget, maxBlocks, mode, player.getDirection()).getVein();
	}
}
