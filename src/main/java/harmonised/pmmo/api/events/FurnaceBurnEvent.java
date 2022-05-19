package harmonised.pmmo.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class FurnaceBurnEvent extends Event{
	ItemStack input;
	Level level;
	BlockPos pos;
	
	public FurnaceBurnEvent(ItemStack input, Level level, BlockPos pos) {
		this.input = input;
		this.level = level;
		this.pos = pos;
	}
	
	@Override
	public boolean isCancelable() {return false;}
	
	public ItemStack getInput() {return input;}
	public Level getLevel() {return level;}
	public BlockPos getPos() {return pos;}

}
