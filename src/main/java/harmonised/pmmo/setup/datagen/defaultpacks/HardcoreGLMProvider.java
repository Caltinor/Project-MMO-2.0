package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.setup.datagen.GLMProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;

public class HardcoreGLMProvider extends GLMProvider {
    private static final Path path = Path.of("resourcepacks/hardcore/data");
    public HardcoreGLMProvider(PackOutput gen) {
        super(gen, path);
    }

    @Override
    protected void start() {

    }
}
