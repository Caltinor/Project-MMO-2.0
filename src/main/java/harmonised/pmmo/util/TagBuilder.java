package harmonised.pmmo.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

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
	
	public TagBuilder withFloat(@NonNull String key, @NonNull float value) {
		nbt.putFloat(key, value);
		return this;
	}
	
	public TagBuilder withList(@NonNull String key, @NonNull ListTag list) {
		nbt.put(key, list);
		return this;
	}
	
	public TagBuilder withList(@NonNull String key, Tag...tags) {
		ListTag list = new ListTag();
		list.addAll(Arrays.stream(tags).toList());
		nbt.put(key, list);
		return this;
	}
	
	public TagBuilder withInt(@NonNull String key, @NonNull int value) {
		nbt.putInt(key, value);
		return this;
	}
	
	public TagBuilder withDouble(@NonNull String key, @NonNull double value) {
		nbt.putDouble(key, value);
		return this;
	}
	
	public TagBuilder withLong(@NonNull String key, @NonNull long value) {
		nbt.putLong(key, value);
		return this;
	}
}
