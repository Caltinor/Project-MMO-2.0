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

    public SalvageEvent(ServerPlayer player, ItemStack inputStack, Map.Entry<ResourceLocation, CodecTypes.SalvageData> salvage)
    {
        super(player);
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

    public ItemStack getOutputStack() {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(salvage.getKey()));
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
