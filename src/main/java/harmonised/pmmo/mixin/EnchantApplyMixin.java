package harmonised.pmmo.mixin;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import harmonised.pmmo.events.impl.EnchantHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

@Debug
@Mixin(EnchantmentMenu.class)
public class EnchantApplyMixin {	
	//EnchantmentInstance enchantment;
	//ItemStack stack;

	@Inject(at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/item/ItemStack;enchant(Lnet/minecraft/world/item/enchantment/Enchantment;I)V"),
			method = "clickMenuButton(Lnet/minecraft/world/entity/player/Player;I)Z",
			locals = LocalCapture.PRINT)
 	public void projectmmo$$enchantHandle(Player player, int selection, CallbackInfo info) {	
		System.out.println("Enchant Mixin Fired"); //TODO Remove
 		//EnchantHandler.handle(player, stack, enchantment);
 	}
}
