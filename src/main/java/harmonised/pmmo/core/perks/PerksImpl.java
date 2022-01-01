package harmonised.pmmo.core.perks;

import org.apache.commons.lang3.function.TriFunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PerksImpl {
	private static final CompoundTag NONE = new CompoundTag();
	
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> DUMMY = (player, nbt, level) -> {
		return NONE;
	};
}
