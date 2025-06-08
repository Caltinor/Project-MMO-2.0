package harmonised.pmmo.mixin;

import harmonised.pmmo.storage.DataAttachmentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**This mixin is named "ServerLevel" Mixin because we are checking first and 
 * foremost if the instance of level is an instance of ServerLevel before 
 * executing our logic.  Therefore, even though we are technically mixing into
 * {@link net.minecraft.world.level.Level Level}, we are only ever executing within
 * ServerLevel.  This was necessary because you cannot inject into inherited
 * methods with mixin.
 * 
 * @author Caltinor
 *
 */
@Mixin(Level.class)
public class ServerLevelMixin {
	
	@Inject(method="destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
			at = @At(
				value="INVOKE",
				target="Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"))
	public void setBlockInvocation(BlockPos pos, boolean p_46627_, @Nullable Entity entity, int p_46629_, CallbackInfoReturnable<?> ci) {
		if ((Level)(Object)this instanceof ServerLevel)
			execute(pos, (Level)(Object)this);
	}
	
	private static void execute(BlockPos pos, Level level) {
		BlockState state = level.getBlockState(pos);
		var breakMap = level.getChunkAt(pos).getData(DataAttachmentTypes.BREAK_MAP.get());
		for (BlockPos neighbor : getNeighbors(pos)) {
			UUID playerID = breakMap.get(neighbor);
			Player player;
			if (playerID != null && (player = level.getServer().getPlayerList().getPlayer(playerID)) != null) {
				NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player));
				breakMap.put(neighbor, playerID);
				level.getChunkAt(pos).setUnsaved(true);
				break;
			}
		}
	}
	
	private static Set<BlockPos> getNeighbors(BlockPos src) {
		return Set.of(
			src.above(),
			src.below(),
			src.north(),
			src.south(),
			src.west(),
			src.east()
		);
	}
}
