package harmonised.pmmo.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.EnchantmentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentContainer.class)
public class EnchantmentContainerGetMixin
{
    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false), method = "enchantItem")
    public void projectmmo$$EnchantmentContainerGetEnchantment(PlayerEntity playerIn, int id, CallbackInfoReturnable<Boolean> cir)
    {
        System.out.println("works? :o");
    }
}
