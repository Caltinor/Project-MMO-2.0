package harmonised.pmmo.mixin;

import harmonised.pmmo.api.events.FurnaceBurnEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
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
    private static void projectmmo$handleCampfireCook(Level p_155307_, BlockPos p_155308_, BlockState p_155309_, CampfireBlockEntity p_155310_, CallbackInfo ci, boolean flag, int i, ItemStack itemstack, Container container, ItemStack itemstack1) {
        NeoForge.EVENT_BUS.post(new FurnaceBurnEvent(itemstack1, p_155307_, p_155308_));
    }
}
