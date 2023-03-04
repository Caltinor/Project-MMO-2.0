package harmonised.pmmo.mixin;

import harmonised.pmmo.events.FurnaceHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireTileEntity.class)
public class CampfireTileEntitySetMixin
{
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/InventoryHelper;spawnItemStack(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"), method = "cookAndDrop", locals = LocalCapture.PRINT)
    public void projectmmo$$handleCampfireSpawnItem(CallbackInfo info, int i, ItemStack itemstack, int j, IInventory iInventory, ItemStack itemstack1)
    {
        World world = ((CampfireTileEntity)(Object)this).getWorld();
        BlockPos pos = ((CampfireTileEntity)(Object)this).getPos();
        FurnaceHandler.handleSmelted(itemstack1, world, pos, 1);
    }
}