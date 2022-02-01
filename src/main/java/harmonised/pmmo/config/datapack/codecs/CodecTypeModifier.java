package harmonised.pmmo.config.datapack.codecs;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.config.readers.ModifierDataType;
import net.minecraft.util.StringRepresentable;

public class CodecTypeModifier {
	Map<ModifierDataType, Map<String, Double>> obj;
	
	public static final Codec<Map<String, Double>> DOUBLE_CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);
	
	public static final Codec<CodecTypeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ModifierDataType.CODEC, DOUBLE_CODEC, StringRepresentable.keys(ModifierDataType.values())).forGetter(CodecTypeModifier::getMap)
			).apply(instance, CodecTypeModifier::new));
	
	public CodecTypeModifier(Map<ModifierDataType, Map<String, Double>> obj) {this.obj = obj;}
	
	public Map<ModifierDataType, Map<String, Double>> getMap() {return obj != null ? obj : new HashMap<>();}
}
