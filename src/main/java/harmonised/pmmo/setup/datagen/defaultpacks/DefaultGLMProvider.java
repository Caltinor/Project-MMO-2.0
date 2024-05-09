package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.setup.datagen.GLMProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DefaultGLMProvider extends GLMProvider {
    private static final Path path = Path.of("resourcepacks/default/data");
    public DefaultGLMProvider(PackOutput gen, CompletableFuture<HolderLookup.Provider> registries) {
        super(gen, path, registries);
    }

    @Override
    protected void start() {

    }
}
