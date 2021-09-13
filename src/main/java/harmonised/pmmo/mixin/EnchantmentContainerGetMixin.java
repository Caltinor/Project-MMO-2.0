package harmonised.pmmo.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin( EnchantmentMenu.class )
public class EnchantmentContainerGetMixin
{
    @Inject( at = @At( value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false ), method = "enchantItem" )
    public void projectmmo$$EnchantmentContainerGetEnchantment( Player playerIn, int id, CallbackInfoReturnable<Boolean> cir )
    {
        System.out.println( "works? :o" );
    }
}
