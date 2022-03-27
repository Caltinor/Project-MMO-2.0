package harmonised.pmmo.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import harmonised.pmmo.events.impl.FurnaceHandler;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceTileEntityShrinkMixin
{
    @Shadow
    protected NonNullList<ItemStack> items;

    @Inject(at = @At(
            	value = "INVOKE",
            	target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            method = "burn(Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z")
    public void projectmmo$$handleSmeltingShrink(@Nullable Recipe<?> p_155027_, NonNullList<ItemStack> p_155028_, int p_155029_, CallbackInfoReturnable<?> info)
    {
        Level world = ((AbstractFurnaceBlockEntity)(Object)this).getLevel();
        BlockPos pos = ((AbstractFurnaceBlockEntity)(Object)this).getBlockPos();
        FurnaceHandler.handle(items.get(0), world, pos);
    }
}