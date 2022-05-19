package harmonised.pmmo.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class EatFoodEvent extends PlayerEvent{
	ItemStack food;

	public EatFoodEvent(Player player, ItemStack foodItem) {
		super(player);
		food = foodItem;
	}
	
	@Override
	public boolean isCancelable() {return false;}

	public ItemStack getFood() {return food;}
}
