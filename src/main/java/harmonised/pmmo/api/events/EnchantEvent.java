package harmonised.pmmo.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class EnchantEvent extends PlayerEvent{
	ItemStack stack;
	int levelsSpent;
	
	public EnchantEvent(Player player, ItemStack stack, int levelsSpent) {
		super(player);
		this.stack = stack;
		this.levelsSpent = levelsSpent;
	}
	
	@Override
	public boolean isCancelable() {return false;}

	public ItemStack getItem() {return stack;}
	public int getLevelsSpent() {return levelsSpent;}
}
