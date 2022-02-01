package harmonised.pmmo.config.readers.codecs;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CodecMapGlobals {
	private final Map<String, String> paths;
	private final Map<String, String> constants;
	
	public static final Codec<CodecMapGlobals> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("paths").forGetter(CodecMapGlobals::getPaths),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("constants").forGetter(CodecMapGlobals::getConstants)
			).apply(instance, CodecMapGlobals::new));
	
	public CodecMapGlobals(Map<String, String> paths, Map<String, String> constants) {
		this.paths = paths;
		this.constants = constants;
	}
	public Map<String, String> getPaths() {return paths;}
	public Map<String, String> getConstants() {return constants;}
}
