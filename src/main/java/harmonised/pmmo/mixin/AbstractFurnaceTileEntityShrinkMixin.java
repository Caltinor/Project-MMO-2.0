package harmonised.pmmo.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.NeoForge;
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
            method = "burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/core/NonNullList;I)Z")
    public void projectmmo$$handleSmeltingShrink(RegistryAccess p_266740_, @Nullable RecipeHolder<?> p_266780_, NonNullList<ItemStack> p_267073_, int p_267157_, CallbackInfoReturnable<?> info)
    {
        Level world = ((AbstractFurnaceBlockEntity)(Object)this).getLevel();
        BlockPos pos = ((AbstractFurnaceBlockEntity)(Object)this).getBlockPos();
        NeoForge.EVENT_BUS.post(new FurnaceBurnEvent(items.get(0), world, pos));
    }
}