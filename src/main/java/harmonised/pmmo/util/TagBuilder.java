package harmonised.pmmo.util;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class TagBuilder {
	private CompoundTag nbt;
	
	private TagBuilder() {
		nbt = new CompoundTag();
	}	
	
	public static TagBuilder start() {
		return new TagBuilder();
	}
	
	public CompoundTag build() {return nbt;}
	
	public TagBuilder withString(@NonNull String key, @NonNull String value) {
		nbt.putString(key, value);
		return this;
	}
	
	public TagBuilder withBool(@NonNull String key, @NonNull boolean value) {
		nbt.putBoolean(key, value);
		return this;
	}
	
	public TagBuilder withList(@NonNull String key, @NonNull ListTag list) {
		nbt.put(key, list);
		return this;
	}
}
