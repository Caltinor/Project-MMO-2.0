package harmonised.pmmo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

public class TagUtils {

	/**an enhanced version of {@link net.minecraft.nbt.CompoundTag#merge(CompoundTag) CompoundTag.merge}
	 * which is adds values for shared keys instead of complete replacement
	 * 
	 * @param tag1 a CompoundTag instance
	 * @param tag2 a different CompoundTag instance
	 * @return a merged tag
	 */
	public static CompoundTag mergeTags(CompoundTag tag1, CompoundTag tag2) {
		CompoundTag output = new CompoundTag();
        List<String> allKeys = new ArrayList<>(tag1.getAllKeys());
		for (String key : tag2.getAllKeys()) {
			if (!allKeys.contains(key) && key != null)
				allKeys.add(key);
		}
		for (String key : allKeys) {
			if (tag1.contains(key) && tag2.contains(key)) {
				if (tag1.get(key) instanceof NumericTag numTag) {
					if (numTag instanceof DoubleTag)
						output.putDouble(key, tag1.getDouble(key) + tag2.getDouble(key));
					else if (numTag instanceof FloatTag)
						output.putFloat(key, tag1.getFloat(key) + tag2.getFloat(key));
					else if (numTag instanceof IntTag)
						output.putInt(key, tag1.getInt(key) + tag2.getInt(key));
					else if (numTag instanceof LongTag)
						output.putLong(key, tag1.getLong(key) + tag2.getLong(key));
					else if (numTag instanceof ShortTag) 
						output.putShort(key, (short)(tag1.getShort(key) + tag2.getShort(key)));
					else
						output.put(key, tag1.get(key));
				}
				else
					output.put(key, tag1.get(key));
			}
			else if (tag1.contains(key) && !tag2.contains(key))
				output.put(key, tag1.get(key));
			else if (!tag1.contains(key) && tag2.contains(key))
				output.put(key, tag2.get(key));				
		}
		return output;
	}

	public static CompoundTag stackTag(ItemStack stack, Level level) {
		return stackTag(stack, level.registryAccess());
	}
	/**safely obtain the NBT tag or get a new instance
	 *
	 * @param stack the item whose NBT is being obtained
	 * @return an associated tag or new instance
	 */
	public static CompoundTag stackTag(ItemStack stack, RegistryAccess access) {
		if (stack.isEmpty()) return new CompoundTag();
		if (stack.getCount() > 99) {
			var clone = stack.copy();
			clone.setCount(99);
			return (CompoundTag) clone.save(access);
		}
		return (CompoundTag) stack.save(access);
	}
	 /**safely obtain the NBT tag or get a new instance
	  * 
	  * @param entity the entity whose NBT is being obtained
	  * @return an associated tag or new instance
	  */
	public static CompoundTag entityTag(Entity entity) {
		CompoundTag data = new CompoundTag();
		return entity == null ? data : entity.saveWithoutId(data);
	}
	
	/**safely obtain the NBT tag or get a new instance
	 * 
	 * @param tile the BlockEntity whose NBT is being obtained
	 * @return an associated tag or new instance
	 */
	public static CompoundTag tileTag(BlockEntity tile) {
		return tile == null	? new CompoundTag()	: tile.saveWithFullMetadata(tile.getLevel().registryAccess());
	}
	
	public static CompoundTag stateTag(BlockState state) {
		CompoundTag dataOut = new CompoundTag();
		state.getProperties().forEach(prop -> dataOut.putString(prop.getName(), state.getValue(prop).toString()));
		return dataOut;
	}
	
	public static float getFloat(CompoundTag nbt, String key, float ifAbsent) {
		return nbt.contains(key) ? nbt.getFloat(key) : ifAbsent;
	}
	
	public static BlockPos getBlockPos(CompoundTag nbt, String key, BlockPos ifAbsent) {
		return nbt.contains(key) ? BlockPos.of(nbt.getLong(key)) : ifAbsent;
	}
}
