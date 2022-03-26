package harmonised.pmmo.events.impl;

import harmonised.pmmo.core.Core;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class EnchantHandler {

	public static void handle(Player player, ItemStack stack, EnchantmentInstance enchantment) {
		Core core = Core.get(player.level);
		CompoundTag eventHook = new CompoundTag();
		double proportion = enchantment.level / enchantment.enchantment.getMaxLevel();
		
	}
}
