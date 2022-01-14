package harmonised.pmmo.core.perks;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

public class PerksImpl {
	private static final CompoundTag NONE = new CompoundTag();
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DUMMY = (player, nbt, level) -> {
		return NONE;
	};
	
	//TODO find out how to match skill with tool
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> BREAK_SPEED = (player, nbt, level) -> {
		float speedIn = nbt.contains(APIUtils.BREAK_SPEED_INPUT_VALUE) ? nbt.getFloat(APIUtils.BREAK_SPEED_INPUT_VALUE) : player.getMainHandItem().getDestroySpeed(Blocks.OBSIDIAN.defaultBlockState());
		float speedBonus = nbt.contains(APIUtils.PER_LEVEL) ? nbt.getFloat(APIUtils.PER_LEVEL) : 1;
		float heightConfig = nbt.contains(APIUtils.MODIFIER) ? nbt.getFloat(APIUtils.MODIFIER) : 1000;
		BlockPos pos = nbt.contains(APIUtils.BLOCK_POS) ? BlockPos.of(nbt.getLong(APIUtils.BLOCK_POS)) : new BlockPos(0,0,0);
		float heightMultiplier = 1 - pos.getY()/heightConfig;
		return TagBuilder.start().withFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE, speedIn * (1 + level * speedBonus * heightMultiplier)).build();
	};
}
