package harmonised.pmmo.mixin;

import java.util.Set;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.storage.IChunkData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

@Mixin(Level.class)
public class ServerLevelMixin {

	@Inject(method="Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
			at = @At("RETURN"))
	public boolean destroyBlock(BlockPos pos, boolean p_46627_, @Nullable Entity entity, int p_46629_, CallbackInfoReturnable<?> ci) {
		if (!((Level)(Object)this instanceof ServerLevel))
			return ci.getReturnValueZ();
		System.out.println("ServerLevel Mixin Proc"); //TODO remove
		execute(pos, entity);
		return ci.getReturnValueZ();
	}
	
	private static void execute(BlockPos pos, Entity entity) {
		if (entity == null || !(entity instanceof Player)) 
			return;
		BlockState state = entity.getLevel().getBlockState(pos);
		IChunkData cap = entity.getLevel().getChunkAt(pos).getCapability(ChunkDataProvider.CHUNK_CAP).orElseGet(ChunkDataHandler::new);
		//TODO add in checkers for the neighboring blocks
		for (BlockPos neighbor : getNeighbors(pos)) {
			if (cap.getBreaker(neighbor, false).equals(entity.getUUID())) {
				if (!MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(entity.getLevel(), pos, state, (Player)entity)));
					cap.setBreaker(pos, entity.getUUID());
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
