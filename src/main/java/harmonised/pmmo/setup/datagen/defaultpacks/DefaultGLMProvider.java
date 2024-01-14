package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.setup.datagen.GLMProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;

public class DefaultGLMProvider extends GLMProvider {
    private static final Path path = Path.of("resourcepacks/default/data");
    public DefaultGLMProvider(PackOutput gen) {
        super(gen, path);
    }

    @Override
    protected void start() {

    }
}
