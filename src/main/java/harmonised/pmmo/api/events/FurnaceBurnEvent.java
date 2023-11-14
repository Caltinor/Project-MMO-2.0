package harmonised.pmmo.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class FurnaceBurnEvent extends Event {
	ItemStack input;
	Level level;
	BlockPos pos;
	
	/**A Project MMO event for capturing when items are smelted in any sort
	 * of furnace or similar action.  For compatibility with your mod, whenever
	 * your smelting behavior successfully executes, post a new instance of this
	 * event to the EVENT_BUS and PMMO will handle the rest.
	 * <br><br>
	 * The Level and BlockPos arguments are used to check if the block at this
	 * location belongs to the player so that all experience gained is attributed
	 * to this player.  If your mechanic does not use a physical block in the world
	 * this event will not provide players with a smelting event they can configure.
	 * 
	 * @param input the item being smelted.  NOT the output item
	 * @param level the world/level the smelting is occurring in
	 * @param pos the position of the block smelting the item
	 */
	public FurnaceBurnEvent(ItemStack input, Level level, BlockPos pos) {
		this.input = input;
		this.level = level;
		this.pos = pos;
	}
	
	public ItemStack getInput() {return input;}
	public Level getLevel() {return level;}
	public BlockPos getPos() {return pos;}

}
