package harmonised.pmmo.config.datapack.codecs;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.api.enums.EventType;
import net.minecraft.util.StringRepresentable;

//For use in parsing XP gain values from 
public class CodecTypeEvent {
	Map<EventType, Map<String, Long>> obj;
	
	public static final Codec<Map<String, Long>> LONG_CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG);

	public static final Codec<CodecTypeEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(EventType.CODEC, LONG_CODEC, StringRepresentable.keys(EventType.values())).forGetter(CodecTypeEvent::getMap)
			).apply(instance, CodecTypeEvent::new));
	
	public CodecTypeEvent(Map<EventType, Map<String, Long>> obj) {this.obj = obj;}			
	public Map<EventType, Map<String, Long>>  getMap() {return obj != null ? obj : new HashMap<>();}
}
