package harmonised.pmmo.api.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.codecs.CodecTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

public class SalvageEvent extends PlayerEvent {
    ItemStack inputStack;
    Map.Entry<ResourceLocation, CodecTypes.SalvageData> salvage;
    ItemStack outputStack;
    private APIUtils.SalvageBuilder salvageBuilder;

    public SalvageEvent(ServerPlayer player, ItemStack inputStack, Map.Entry<ResourceLocation, CodecTypes.SalvageData> salvage) {
        super(player);
        this.inputStack = inputStack;
        this.salvage = salvage;
        this.outputStack = new ItemStack(ForgeRegistries.ITEMS.getValue(salvage.getKey()));
        this.salvageBuilder = APIUtils.SalvageBuilder.start()
                .setChancePerLevel(salvage.getValue().chancePerLevel())
                .setLevelReq(salvage.getValue().levelReq())
                .setXpAward(salvage.getValue().xpAward())
                .setSalvageMax(salvage.getValue().salvageMax())
                .setBaseChance(salvage.getValue().baseChance())
                .setMaxChance(salvage.getValue().maxChance());
    }

    public Map.Entry<ResourceLocation, CodecTypes.SalvageData> getSalvage() {
        updateSalvage();
        return salvage;
    }

    public ItemStack getInputStack() {
        return inputStack;
    }

    public APIUtils.SalvageBuilder getBuilder() {
        return salvageBuilder;
    }

    public void updateSalvage() {
        this.salvage = new SimpleEntry<>(salvage.getKey(), salvageBuilder.build());
    }

    public void setChancePerLevel(Map<String, Double> chancePerLevel) {
        this.salvageBuilder.setChancePerLevel(chancePerLevel);
    }

    public void setLevelReq(Map<String, Integer> levelReq) {
        this.salvageBuilder.setLevelReq(levelReq);
    }

    public void setXpAward(Map<String, Long> xpAward) {
        this.salvageBuilder.setXpAward(xpAward);
    }

    public void setSalvageMax(int salvageMax) {
        this.salvageBuilder.setSalvageMax(salvageMax);
    }

    public void setBaseChance(double baseChance) {
        this.salvageBuilder.setBaseChance(baseChance);
    }

    public void setMaxChance(double maxChance) {
        this.salvageBuilder.setMaxChance(maxChance);
    }

    public ItemStack getOutputStack() {
        return this.outputStack;
    }

    public void setOutputStack(ItemStack outputStack) {
        this.outputStack = outputStack;
    }

    public void setSalvage(ResourceLocation itemID, CodecTypes.SalvageData salvageData) {
        this.salvage = new SimpleEntry<>(itemID, salvageData);
        this.outputStack = new ItemStack(ForgeRegistries.ITEMS.getValue(itemID));
        this.salvageBuilder = APIUtils.SalvageBuilder.start()
                .setChancePerLevel(salvageData.chancePerLevel())
                .setLevelReq(salvageData.levelReq())
                .setXpAward(salvageData.xpAward())
                .setSalvageMax(salvageData.salvageMax())
                .setBaseChance(salvageData.baseChance())
                .setMaxChance(salvageData.maxChance());
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}