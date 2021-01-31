package harmonised.pmmo.mixin;

import harmonised.pmmo.events.FurnaceHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( AbstractFurnaceTileEntity.class )
public class AbstractFurnaceTileEntityShrinkMixin
{
    @Shadow
    protected NonNullList<ItemStack> items;

    @Inject( at = @At( value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V" ), method = "smelt" )
    public void projectmmo$$handleSmeltingShrink( IRecipe<?> p_214007_1_, CallbackInfo info )
    {
        World world = ((AbstractFurnaceTileEntity)(Object)this).getWorld();
        BlockPos pos = ((AbstractFurnaceTileEntity)(Object)this).getPos();
        FurnaceHandler.handleSmelted( items.get(0), items.get(2), world, pos, 0 );
        FurnaceHandler.handleSmelted( items.get(0), items.get(2), world, pos, 1 );
    }
}