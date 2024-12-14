package harmonised.pmmo.mixin;

import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import harmonised.pmmo.api.events.EnchantEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

@Mixin(EnchantedItemTrigger.class)
public class EnchantApplyMixin {
	@Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;I)V",
			at = @At("HEAD"))
	private void postEnchantEvent(ServerPlayer pPlayer, ItemStack pItem, int pLevelsSpent, CallbackInfo ci) {
		MinecraftForge.EVENT_BUS.post(new EnchantEvent(pPlayer, pItem, pLevelsSpent));
	}
}
