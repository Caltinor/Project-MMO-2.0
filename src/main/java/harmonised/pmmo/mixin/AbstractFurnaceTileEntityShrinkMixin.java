package harmonised.pmmo.mixin;

import harmonised.pmmo.events.FurnaceHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( AbstractFurnaceTileEntity.class )
public class AbstractFurnaceTileEntityShrinkMixin extends TileEntity
{
    @Shadow
    protected NonNullList<ItemStack> items;

    public AbstractFurnaceTileEntityShrinkMixin(TileEntityType<?> p_i48289_1_)
    {
        super(p_i48289_1_);
    }

    @Inject( at = @At( value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V" ), method = "func_214007_c" )
    private void projectmmo$$handleSmeltingShrink( IRecipe<?> p_214007_1_, CallbackInfo info )
    {
        FurnaceHandler.handleSmelted( items.get(0), items.get(2), this.getWorld(), 0 );
        FurnaceHandler.handleSmelted( items.get(0), items.get(2), this.getWorld(), 1 );
    }
}