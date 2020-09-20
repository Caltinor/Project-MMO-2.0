package harmonised.pmmo.mixin;

import harmonised.pmmo.events.AirSupplyDecreaseHandler;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin( LivingEntity.class )
public class LivingEntityChangeRespirationMixin
{
    @Redirect( at = @At( value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getRespirationModifier(Lnet/minecraft/entity/LivingEntity;)I" ), method = "decreaseAirSupply" )
    private int projectmmo$$changeLivingEntityRespiration(LivingEntity entity )
    {
        return AirSupplyDecreaseHandler.returnPmmoAffectedRespiration( entity );
    }
}