package harmonised.pmmo.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import harmonised.pmmo.features.veinmining.VeinMiningLogic;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

@Mixin(ForgeHooksClient.class)
public class VeinBobMixin {

	@Inject(method="shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z"
			,at = @At("TAIL"),
			remap=false,
			cancellable=true)
	private static void shouldCauseReequipAnimation(@NotNull ItemStack from, @NotNull ItemStack to, int slot, CallbackInfoReturnable<Boolean> ci) {
		if (getCharge(from) != getCharge(to))
			ci.setReturnValue(false);
	}
	
	private static int getCharge(ItemStack stack) {
		return stack.getTag() != null ? stack.getTag().getInt(VeinMiningLogic.CURRENT_CHARGE) : 0;
	}
}
