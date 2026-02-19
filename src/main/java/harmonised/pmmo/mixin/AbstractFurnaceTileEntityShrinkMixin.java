package harmonised.pmmo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import harmonised.pmmo.api.events.FurnaceBurnEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceTileEntityShrinkMixin {
    @Invoker("canBurn")
    public static boolean invokingCanBurn(RegistryAccess access, RecipeHolder<? extends AbstractCookingRecipe> recipe, SingleRecipeInput recipeInput, NonNullList<ItemStack> items, int maxStackSize) {
        throw new AssertionError();
    }

    @Invoker("getItems")
    abstract NonNullList<ItemStack> invokeGetItems();

    @Inject(at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;setRecipeUsed(Lnet/minecraft/world/item/crafting/RecipeHolder;)V"
            ),
            method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)V")
    private static void projectmmo$$handleSmeltingShrink(ServerLevel level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity be, CallbackInfo info,
                                                         @Local SingleRecipeInput singlerecipeinput,
                                                         @Local RecipeHolder<? extends AbstractCookingRecipe> recipeholder,
                                                         @Local int i)
    {
        NeoForge.EVENT_BUS.post(new FurnaceBurnEvent(be.getItem(0), be.getItem(2), level, pos));
    }
}