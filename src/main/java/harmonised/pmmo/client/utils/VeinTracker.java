package harmonised.pmmo.client.utils;

import java.util.Collections;
import java.util.Set;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinShapeData;
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
	
	public static void setTarget(BlockPos pos) {		
		currentTarget = currentTarget == null ? pos : currentTarget.equals(pos) ? BlockPos.ZERO : pos;
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
		Block block = player.level.getBlockState(currentTarget).getBlock();
		int maxBlocks = getCurrentCharge()/Core.get(LogicalSide.CLIENT).getVeinData().getBlockConsume(block);
		vein = new VeinShapeData(player.level, currentTarget, maxBlocks).getVein();
	}
}
