package harmonised.pmmo.mixin;

import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NeoForgeEventHandler.class)
public interface NeoEventHandlerMixin {
    @Accessor("LOOT_MODIFIER_MANAGER")
    public static LootModifierManager getINSTANCE() {
        throw new AssertionError();
    }
}
