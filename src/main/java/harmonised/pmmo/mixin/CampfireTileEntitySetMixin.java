//package harmonised.pmmo.mixin;
//
//import harmonised.pmmo.events.FurnaceHandler;
//import net.minecraft.world.Container;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.entity.CampfireBlockEntity;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.state.BlockState;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.ModifyVariable;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
//
//@Mixin(CampfireBlockEntity.class)
//public class CampfireTileEntitySetMixin
//{
//    @Inject(at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"),
//            method = "cookTick")
//    private static void projectmmo$$handleCampfireSpawnItem(Level level, BlockPos pos, BlockState j, CampfireBlockEntity te, CallbackInfo ci)
//    {
//        FurnaceHandler.handleSmelted(te.getItems().get(0), te.getItems().get(0), level, pos, 1);
//    }
//}