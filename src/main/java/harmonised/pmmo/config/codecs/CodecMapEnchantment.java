package harmonised.pmmo.config.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.util.Functions;

public record CodecMapEnchantment(
		boolean override,
		List<Map<String, Integer>> skillArray) {
	
	public static final Codec<CodecMapEnchantment> CODEC = RecordCodecBuilder.create(instance -> instance.group( 
			Codec.BOOL.optionalFieldOf("override").forGetter(cme -> Optional.of(cme.override())),
			CodecTypes.INTEGER_CODEC.listOf().fieldOf("levels").forGetter(CodecMapEnchantment::skillArray)
		).apply(instance, (o, map) -> new CodecMapEnchantment(o.orElse(false), map)));
	
	public static class Builder {
		boolean override = false;
		List<Map<String, Integer>> skillArray = new ArrayList<>();
		private Builder() {}
		public static Builder start() {return new Builder();}
		public Builder override(boolean bool) {this.override = bool; return this;}
		public Builder skillArray(List<Map<String, Integer>> arr) {this.skillArray = arr; return this;}
		public CodecMapEnchantment build() {return new CodecMapEnchantment(override, skillArray);}
	}
	
	public static CodecMapEnchantment combine(CodecMapEnchantment one, CodecMapEnchantment two) {
		List<Map<String, Integer>> skillArray = new ArrayList<>();
		
		BiConsumer<CodecMapEnchantment, CodecMapEnchantment> bothOrNeither = (o, t) -> {
			int largerList = o.skillArray.size() > t.skillArray.size() ? o.skillArray.size() : t.skillArray.size();
			for (int i = 0; i < largerList; i++) {
				Map<String, Integer> thisMap = new HashMap<>();
				if (o.skillArray.size() > i)
					thisMap.putAll(o.skillArray.get(i));
				if (t.skillArray.size() > i) {
					t.skillArray().get(i).forEach((skill, level) -> {
						thisMap.merge(skill, level, (oldValue, newValue) -> oldValue > newValue ? oldValue : newValue);
					});
				}
				skillArray.add(thisMap);
			}
		};
		Functions.biPermutation(one, two, one.override(), two.override(), 
				(o, t) -> {
					skillArray.addAll(o.skillArray().isEmpty() ? t.skillArray() : o.skillArray());
				}, 
				bothOrNeither, 
				bothOrNeither);
		
		return new CodecMapEnchantment(one.override() || two.override(), skillArray);
	}
}
