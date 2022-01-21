package harmonised.pmmo.core.perks;

import java.util.Set;
import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class PerksImpl {
	private static final CompoundTag NONE = new CompoundTag();
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DUMMY = (player, nbt, level) -> {
		return NONE;
	};	
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> BREAK_SPEED = (player, nbt, level) -> {
		float speedIn = nbt.contains(APIUtils.BREAK_SPEED_INPUT_VALUE) ? nbt.getFloat(APIUtils.BREAK_SPEED_INPUT_VALUE) : player.getMainHandItem().getDestroySpeed(Blocks.OBSIDIAN.defaultBlockState());
		float speedBonus = getRatioForTool(player.getMainHandItem(), nbt);
		if (speedBonus == 0) return NONE;
		float heightConfig = nbt.contains(APIUtils.MODIFIER) ? nbt.getFloat(APIUtils.MODIFIER) : 1000;
		BlockPos pos = nbt.contains(APIUtils.BLOCK_POS) ? BlockPos.of(nbt.getLong(APIUtils.BLOCK_POS)) : new BlockPos(0,0,0);
		float heightMultiplier = 1 - pos.getY()/heightConfig;
		float newSpeed = speedIn * Math.min(0, 1 + level * speedBonus * heightMultiplier);
		return TagBuilder.start().withFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE, newSpeed).build();
	};
	
	private static Set<ToolAction> DIG_ACTIONS = Set.of(ToolActions.PICKAXE_DIG, ToolActions.AXE_DIG, ToolActions.SHOVEL_DIG, ToolActions.HOE_DIG, ToolActions.SHEARS_DIG, ToolActions.SWORD_DIG);
	
	private static float getRatioForTool(ItemStack tool, CompoundTag nbt) {
		float ratio = 0f;
		for (ToolAction action : DIG_ACTIONS) {
			if (tool.canPerformAction(action) && nbt.contains(action.name()))
				ratio += nbt.getFloat(action.name());
		}
		return ratio;
	}
}
