package harmonised.pmmo.setup;

import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;

import java.util.List;

public class GameplayPacks {
    public static final PackHolder DEFAULT = new PackHolder("default", LangProvider.PACK_DEFAULT, false, PackType.SERVER_DATA, PackSource.BUILT_IN);
    public static final PackHolder EASY = new PackHolder("easy", LangProvider.PACK_EASY, false, PackType.SERVER_DATA, PackSource.FEATURE);
    public static final PackHolder HARDCORE = new PackHolder("hardcore", LangProvider.PACK_HARD, false, PackType.SERVER_DATA, PackSource.FEATURE);

    public static List<PackHolder> getPacks() {
        return List.of(DEFAULT, EASY, HARDCORE);
    }

    public record PackHolder(Identifier id, LangProvider.Translation titleKey, boolean required, PackType type, PackSource source) {
        public PackHolder(String id, LangProvider.Translation titleKey, boolean required, PackType type, PackSource source) {
            this(Reference.rl(id), titleKey, required, type, source);
        }
    }
}
