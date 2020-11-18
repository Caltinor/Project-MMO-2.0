//package harmonised.pmmo.mixin;
//
//import harmonised.pmmo.events.BrewHandler;
//import harmonised.pmmo.events.FurnaceHandler;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.crafting.IRecipe;
//import net.minecraft.tileentity.AbstractFurnaceTileEntity;
//import net.minecraft.tileentity.BrewingStandTileEntity;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.tileentity.TileEntityType;
//import net.minecraft.util.NonNullList;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin( BrewingStandTileEntity.class )
//public class BrewingStandTileEntityShrinkMixin
//{
//    @Shadow
//    private NonNullList<ItemStack> brewingItemStacks;
//
//    @Inject( at = @At( value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V" ), method = "brewPotions" )
//    public void projectmmo$$handleSmeltingShrink( CallbackInfo info )
//    {
//        World world = ((BrewingStandTileEntity)(Object)this).getWorld();
//        BlockPos pos = ((BrewingStandTileEntity)(Object)this).getPos();
//        BrewHandler.handlePotionBrew( brewingItemStacks, world, pos );
//    }
//}
//COUT MIXIN