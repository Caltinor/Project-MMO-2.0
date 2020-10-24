package harmonised.pmmo.mixin;

import harmonised.pmmo.events.BrewHandler;
import harmonised.pmmo.events.FurnaceHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( BrewingStandTileEntity.class )
public class BrewingStandTileEntityShrinkMixin extends TileEntity
{
    @Shadow
    private NonNullList<ItemStack> brewingItemStacks;

    public BrewingStandTileEntityShrinkMixin(TileEntityType<?> p_i48289_1_)
    {
        super(p_i48289_1_);
    }

    @Inject( at = @At( value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V" ), method = "brewPotions" )
    private void projectmmo$$handleSmeltingShrink( CallbackInfo info )
    {
        BrewHandler.handlePotionBrew( brewingItemStacks, this.getWorld(), this.getPos() );
    }
}