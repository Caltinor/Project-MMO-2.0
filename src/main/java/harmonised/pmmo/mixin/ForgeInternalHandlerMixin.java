package harmonised.pmmo.mixin;

import net.minecraftforge.common.ForgeInternalHandler;
import net.minecraftforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ForgeInternalHandler.class, remap = false)
public interface ForgeInternalHandlerMixin {
    @Accessor("INSTANCE")
    public static LootModifierManager getINSTANCE() {
        throw new AssertionError();
    }
}