package harmonised.pmmo.util;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.minecraft.nbt.CompoundTag;

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
}
