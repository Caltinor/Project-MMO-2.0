//package harmonised.pmmo.mixin;
//
//import harmonised.pmmo.events.BrewHandler;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.core.NonNullList;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
//
//@Mixin( BrewingStandBlockEntity.class )
//public class BrewingStandTileEntityShrinkMixin
//{
//    @Shadow
//    private NonNullList<ItemStack> brewingItemStacks;
//
//    @Inject( at = @At( value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V" ), method = "brewPotions" )
//    public void projectmmo$$handleSmeltingShrink( CallbackInfo info )
//    {
//        Level world = ((BrewingStandBlockEntity)(Object)this).getLevel();
//        BlockPos pos = ((BrewingStandBlockEntity)(Object)this).getBlockPos();
//        BrewHandler.handlePotionBrew( brewingItemStacks, world, pos );
//    }
//}