package harmonised.pmmo.api.events;

import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalvageEvent extends PlayerEvent {
    ItemStack inputStack;
    Map.Entry<ResourceLocation, CodecTypes.SalvageData> salvage;
    ItemStack outputStack;

    public SalvageEvent(ServerPlayer player, ItemStack inputStack, Map.Entry<ResourceLocation, CodecTypes.SalvageData> salvage) {
        super(player);
        this.outputStack = new ItemStack(ForgeRegistries.ITEMS.getValue(salvage.getKey()));
    }

    public Map.Entry<ResourceLocation, CodecTypes.SalvageData> getSalvage() {
        return salvage;
    }

    public ItemStack getInputStack() {
        return inputStack;
    }
    public void setSalvage(Map.Entry<ResourceLocation, CodecTypes.SalvageData> salvage) {
        this.salvage = salvage;
    }

    public void setSalvageData(CodecTypes.SalvageData salvageData) {
        this.salvage = new HashMap.SimpleEntry<>(salvage.getKey(), salvageData);
    }

    public void setChancePerLevel(Map<String, Double> chancePerLevel) {
        CodecTypes.SalvageData data
                = new CodecTypes.SalvageData(chancePerLevel,
                salvage.getValue().levelReq(),
                salvage.getValue().xpAward(),
                salvage.getValue().salvageMax(),
                salvage.getValue().baseChance(),
                salvage.getValue().maxChance());
        this.salvage = new HashMap.SimpleEntry<>(salvage.getKey(), data);
    }

    public void setLevelReq(Map<String, Integer> levelReq) {
        CodecTypes.SalvageData data
                = new CodecTypes.SalvageData(salvage.getValue().chancePerLevel(),
                levelReq,
                salvage.getValue().xpAward(),
                salvage.getValue().salvageMax(),
                salvage.getValue().baseChance(),
                salvage.getValue().maxChance());
        this.salvage = new HashMap.SimpleEntry<>(salvage.getKey(), data);
    }

    public void setXpAward(Map<String, Long> xpAward) {
        CodecTypes.SalvageData data
                = new CodecTypes.SalvageData(salvage.getValue().chancePerLevel(),
                salvage.getValue().levelReq(),
                xpAward,
                salvage.getValue().salvageMax(),
                salvage.getValue().baseChance(),
                salvage.getValue().maxChance());
        this.salvage = new HashMap.SimpleEntry<>(salvage.getKey(), data);
    }

    public void setSalvageMax(int salvageMax) {
        CodecTypes.SalvageData data
                = new CodecTypes.SalvageData(salvage.getValue().chancePerLevel(),
                salvage.getValue().levelReq(),
                salvage.getValue().xpAward(),
                salvageMax,
                salvage.getValue().baseChance(),
                salvage.getValue().maxChance());
        this.salvage = new HashMap.SimpleEntry<>(salvage.getKey(), data);
    }

    public void setBaseChance(float baseChance) {
        CodecTypes.SalvageData data
                = new CodecTypes.SalvageData(salvage.getValue().chancePerLevel(),
                salvage.getValue().levelReq(),
                salvage.getValue().xpAward(),
                salvage.getValue().salvageMax(),
                baseChance,
                salvage.getValue().maxChance());
        this.salvage = new HashMap.SimpleEntry<>(salvage.getKey(), data);
    }

    public void setMaxChance(float maxChance) {
        CodecTypes.SalvageData data
                = new CodecTypes.SalvageData(salvage.getValue().chancePerLevel(),
                salvage.getValue().levelReq(),
                salvage.getValue().xpAward(),
                salvage.getValue().salvageMax(),
                salvage.getValue().baseChance(),
                maxChance);
        this.salvage = new HashMap.SimpleEntry<>(salvage.getKey(), data);
    }

    public ItemStack getOutputStack() {
        return this.outputStack;
    }

    public void setOutputStack(ItemStack inputStack) {
        this.outputStack = inputStack;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
