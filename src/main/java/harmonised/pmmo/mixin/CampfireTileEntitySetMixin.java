//package harmonised.pmmo.mixin;
//
//import harmonised.pmmo.events.FurnaceHandler;
//import net.minecraft.world.Container;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.entity.CampfireBlockEntity;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
//
//@Mixin( CampfireBlockEntity.class )
//public class CampfireTileEntitySetMixin
//{
//    @Inject( at = @At( value = "INVOKE", target = "Lnet/minecraft/inventory/InventoryHelper;spawnItemStack(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V" ), method = "cookAndDrop", locals = LocalCapture.PRINT )
//    public void projectmmo$$handleCampfireSpawnItem( CallbackInfo info, int i, ItemStack itemstack, int j, Container iInventory, ItemStack itemstack1 )
//    {
//        Level world = ((CampfireBlockEntity)(Object)this).getLevel();
//        BlockPos pos = ((CampfireBlockEntity)(Object)this).getBlockPos();
//        FurnaceHandler.handleSmelted( itemstack, itemstack1, world, pos, 1 );
//    }
//}