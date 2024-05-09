package harmonised.pmmo.mixin;

import harmonised.pmmo.api.events.EnchantEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Debug
@Mixin(EnchantmentMenu.class)
public class EnchantApplyMixin {	

	@Inject(method = "lambda$clickMenuButton$1(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/player/Player;ILnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
	        at = @At(
	        	value = "INVOKE", 
	        	target = "Lnet/minecraft/world/item/ItemStack;enchant(Lnet/minecraft/world/item/enchantment/Enchantment;I)V"), 
	        locals = LocalCapture.CAPTURE_FAILSOFT)
	private void projectmmo$$enchantHandle(ItemStack $$stack, int $$1x, Player player, int $$3x, ItemStack $$4x, Level $$5, BlockPos $$6, CallbackInfo ci, ItemStack stack, List<?> $$8, Iterator<?> $$11, EnchantmentInstance instance) {
		NeoForge.EVENT_BUS.post(new EnchantEvent(player, stack, instance));
 	}
}
