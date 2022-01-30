package harmonised.pmmo.config.datapack.codecs;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.util.StringRepresentable;

public class CodecTypeReq {
	Map<ReqType, Map<String, Integer>> obj;
	
	public static final Codec<Map<String, Integer>> INTEGER_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);
	
	public static final Codec<CodecTypeReq> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ReqType.CODEC, INTEGER_CODEC, StringRepresentable.keys(ReqType.values())).forGetter(CodecTypeReq::getMap)
			).apply(instance, CodecTypeReq::new));
	
	public CodecTypeReq(Map<ReqType, Map<String, Integer>> obj) {this.obj = obj;}
	public Map<ReqType, Map<String, Integer>> getMap() {return obj != null ? obj : new HashMap<>();}
}
