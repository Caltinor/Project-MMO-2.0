package harmonised.pmmo.config.scripting;

import harmonised.pmmo.api.enums.ObjectType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import java.util.List;

@FunctionalInterface
public interface TargetSelector {
    record Selection(ObjectType type, List<Identifier> IDs) {}

    Selection read(String node, RegistryAccess access);
}
