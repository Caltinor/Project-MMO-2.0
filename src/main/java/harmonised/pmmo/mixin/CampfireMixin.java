package harmonised.pmmo.mixin;

import harmonised.pmmo.api.events.FurnaceBurnEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireBlockEntity.class)
public class CampfireMixin {
    @Inject(at=@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"),
    method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V",
    locals = LocalCapture.CAPTURE_FAILHARD)
    private static void projectmmo$handleCampfireCook(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity, CallbackInfo ci, boolean flag, int i, ItemStack itemstack, int j, Container container, ItemStack itemstack1) {
        MinecraftForge.EVENT_BUS.post(new FurnaceBurnEvent(itemstack1, pLevel, pPos));
    }
}
