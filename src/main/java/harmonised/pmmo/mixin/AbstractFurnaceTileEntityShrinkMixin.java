package harmonised.pmmo.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import harmonised.pmmo.api.events.FurnaceBurnEvent;
import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceTileEntityShrinkMixin
{
    @Shadow
    protected NonNullList<ItemStack> items;

    @Inject(at = @At(
            	value = "INVOKE",
            	target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            method = "burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z")
    public void projectmmo$$handleSmeltingShrink(RegistryAccess p_266740_, @Nullable Recipe<?> p_266780_, NonNullList<ItemStack> p_267073_, int p_267157_, CallbackInfoReturnable<?> info)
    {
        if (p_266780_ == null) return;

        Level world = ((AbstractFurnaceBlockEntity)(Object)this).getLevel();
        BlockPos pos = ((AbstractFurnaceBlockEntity)(Object)this).getBlockPos();
        MinecraftForge.EVENT_BUS.post(new FurnaceBurnEvent(items.get(0), items.get(2), world, pos));
    }
}