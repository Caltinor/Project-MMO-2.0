package harmonised.pmmo.config.scripting;

import harmonised.pmmo.api.enums.ObjectType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@FunctionalInterface
public interface TargetSelector {
    record Selection(ObjectType type, List<ResourceLocation> IDs) {}

    Selection read(String node, RegistryAccess access);
}
