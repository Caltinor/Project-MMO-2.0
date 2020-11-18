//package harmonised.pmmo.mixin;
//
//import harmonised.pmmo.events.AirSupplyDecreaseHandler;
//import net.minecraft.entity.EntityLiving;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin( EntityLiving.class )
//public class EntityLivingChangeRespirationMixin
//{
//    @Redirect( at = @At( value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getRespirationModifier(Lnet/minecraft/entity/EntityLiving;)I" ), method = "decreaseAirSupply" )
//    public int projectmmo$$changeEntityLivingRespiration(EntityLiving entity )
//    {
//        return AirSupplyDecreaseHandler.returnPmmoAffectedRespiration( entity );
//    }
//}
//COUT MIXIN