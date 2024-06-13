package harmonised.pmmo.mixin;

import harmonised.pmmo.api.events.FurnaceBurnEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceTileEntityShrinkMixin
{
    @Inject(at = @At(
            	value = "INVOKE",
            	target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            method = "burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/RecipeHolder;Lnet/minecraft/core/NonNullList;ILnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)Z")
    private static void projectmmo$$handleSmeltingShrink(RegistryAccess p_266740_, @Nullable RecipeHolder<?> p_266780_, NonNullList<ItemStack> p_267073_, int p_267157_, AbstractFurnaceBlockEntity be, CallbackInfoReturnable<Boolean> info)
    {
        Level world = be.getLevel();
        BlockPos pos = be.getBlockPos();
        NeoForge.EVENT_BUS.post(new FurnaceBurnEvent(be.getItem(0), world, pos));
    }
}