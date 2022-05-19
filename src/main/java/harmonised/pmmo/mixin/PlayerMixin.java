package harmonised.pmmo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import harmonised.pmmo.api.events.EatFoodEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

@Mixin(Player.class)
public class PlayerMixin {
	
	@Inject(at = @At("HEAD"),
			method = "Lnet/minecraft/world/entity/player/Player;eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;")
	public void fireFoodConsumeEvent(Level level, ItemStack stack, CallbackInfoReturnable<?> ci) {
		MinecraftForge.EVENT_BUS.post(new EatFoodEvent(((Player)(Object)this), stack));
	}

}
