package harmonised.pmmo.mixin;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import harmonised.pmmo.api.events.EnchantEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.common.MinecraftForge;

@Mixin(EnchantmentMenu.class)
public class EnchantApplyMixin {
	@Inject(method = "lambda$clickMenuButton$1(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/player/Player;ILnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
	        at = @At(
	        	value = "INVOKE",
	        	target = "Lnet/minecraft/advancements/critereon/EnchantedItemTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;I)V"),
	        locals = LocalCapture.CAPTURE_FAILHARD)
	private void postEnchantEvents(ItemStack itemstack, int pId, Player pPlayer, int i, ItemStack itemstack1, Level p_39481_, BlockPos p_39482_, CallbackInfo ci, ItemStack itemstack2, List<EnchantmentInstance> list)
	{
		for (EnchantmentInstance enchantment : list) {
			MinecraftForge.EVENT_BUS.post(new EnchantEvent(pPlayer, itemstack2, enchantment));
		}
	}
}
