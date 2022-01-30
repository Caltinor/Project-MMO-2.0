package harmonised.pmmo.config.datapack.codecs;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public class CodecTypeMobMultiplier {
	private final Map<ResourceLocation, Map<String, Double>> obj;
	
	public static final Codec<CodecTypeMobMultiplier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(ResourceLocation.CODEC, CodecTypeModifier.DOUBLE_CODEC).fieldOf("mob_multiplier").forGetter(CodecTypeMobMultiplier::getMap)
			).apply(instance, CodecTypeMobMultiplier::new));
	
	public CodecTypeMobMultiplier(Map<ResourceLocation, Map<String, Double>> obj) {this.obj = obj;}
	public Map<ResourceLocation, Map<String, Double>> getMap() {return obj != null ? obj : new HashMap<>();}
}
