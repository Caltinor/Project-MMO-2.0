package harmonised.pmmo.api.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.codecs.CodecTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

public class SalvageEvent extends PlayerEvent {
    ItemStack inputStack;
    CodecTypes.SalvageData salvage;
    ItemStack outputStack;
    private APIUtils.SalvageBuilder salvageBuilder;

    public SalvageEvent(ServerPlayer player, ItemStack inputStack, Map.Entry<ResourceLocation, CodecTypes.SalvageData> salvage) {
        super(player);
        this.inputStack = inputStack;
        this.salvage = salvage.getValue();
        this.outputStack = new ItemStack(player.level().registryAccess().registryOrThrow(Registries.ITEM).get(salvage.getKey()));
        this.salvageBuilder = APIUtils.SalvageBuilder.start()
                .setChancePerLevel(salvage.getValue().chancePerLevel())
                .setLevelReq(salvage.getValue().levelReq())
                .setXpAward(salvage.getValue().xpAward())
                .setSalvageMax(salvage.getValue().salvageMax())
                .setBaseChance(salvage.getValue().baseChance())
                .setMaxChance(salvage.getValue().maxChance());
    }

    public CodecTypes.SalvageData getSalvage() {
        return salvageBuilder.build();
    }

    public ItemStack getInputStack() {
        return inputStack;
    }

    public APIUtils.SalvageBuilder getBuilder() {
        return salvageBuilder;
    }

    public ItemStack getOutputStack() {
        return this.outputStack;
    }

    public void setOutputStack(ItemStack outputStack) {
        this.outputStack = outputStack;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}