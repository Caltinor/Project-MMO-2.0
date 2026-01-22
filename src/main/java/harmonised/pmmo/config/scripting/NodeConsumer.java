package harmonised.pmmo.config.scripting;

import harmonised.pmmo.api.enums.ObjectType;
import net.minecraft.resources.Identifier;

import java.util.Map;

@FunctionalInterface
public interface NodeConsumer {
    void consume(String param, Identifier id, ObjectType type, Map<String, String> value);
}
