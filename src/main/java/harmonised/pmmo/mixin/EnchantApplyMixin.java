package harmonised.pmmo.mixin;

import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import harmonised.pmmo.api.events.EnchantEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public class EnchantApplyMixin {
	@Inject(method = "clickMenuButton(Lnet/minecraft/world/entity/player/Player;I)Z",
	        at = @At(
	        	value = "INVOKE", 
	        	target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"))
	private void projectmmo$$enchantHandle(Player player, int pId, CallbackInfoReturnable<Boolean> cir) {
		EnchantmentMenu menu = ((EnchantmentMenu)(Object)this);
		ItemStack stack = menu.enchantSlots.getItem(0);
		for (EnchantmentInstance instance : menu.getEnchantmentList(stack, pId, menu.costs[pId])) {
			MinecraftForge.EVENT_BUS.post(new EnchantEvent(player, stack, instance));
		}
 	}
}
