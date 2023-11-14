package harmonised.pmmo.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class EnchantEvent extends PlayerEvent {
	EnchantmentInstance enchant;
	ItemStack stack;
	
	public EnchantEvent(Player player, ItemStack stack, EnchantmentInstance enchantment) {
		super(player);
		this.stack = stack;
		this.enchant = enchantment;
	}

	public EnchantmentInstance getEnchantment() {return enchant;}
	public ItemStack getItem() {return stack;}
}
