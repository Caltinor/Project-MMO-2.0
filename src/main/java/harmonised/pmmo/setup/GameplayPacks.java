package harmonised.pmmo.setup;

import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.List;

public class GameplayPacks {
    public static final PackHolder DEFAULT = new PackHolder("default", LangProvider.PACK_DEFAULT, true, PackType.SERVER_DATA);
    public static final PackHolder EASY = new PackHolder("easy", LangProvider.PACK_EASY, false, PackType.SERVER_DATA);
    public static final PackHolder HARDCORE = new PackHolder("hardcore", LangProvider.PACK_HARD, false, PackType.SERVER_DATA);

    public static List<PackHolder> getPacks() {
        return List.of(DEFAULT, EASY, HARDCORE);
    }

    public record PackHolder(ResourceLocation id, LangProvider.Translation titleKey, boolean required, PackType type) {
        public PackHolder(String id, LangProvider.Translation titleKey, boolean required, PackType type) {
            this(new ResourceLocation(Reference.MOD_ID, id), titleKey, required, type);
        }
    }
}
