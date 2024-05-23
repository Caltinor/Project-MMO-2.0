package harmonised.pmmo.client.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

public class ClientUtils {
	private static Minecraft mc = Minecraft.getInstance();
	public static List<ClientTooltipComponent> ctc(MutableComponent component, int width) {
		return mc.font.split(component, width).stream().map(fcs -> ClientTooltipComponent.create(fcs)).toList();
	}
	private static Map<String, Map<Integer, Map<ReqType, List<Component>>>> cache = new HashMap<>();
	public static void invalidateUnlocksCache() {cache.clear();}

	private static void cacheUnlocks() {
		LocalPlayer player = mc.player;
		Core core = Core.get(LogicalSide.CLIENT);
		Arrays.stream(ReqType.values()).forEach(reqType -> {
			if (reqType.itemApplicable) {
				ForgeRegistries.ITEMS.getValues().stream()
						.map(ItemStack::new)
						.forEach(stack ->
							core.getReqMap(reqType, stack, false).forEach((key, value) -> cache
                                    .computeIfAbsent(key, s -> new HashMap<>())
                                    .computeIfAbsent(value, v -> new HashMap<>())
                                    .computeIfAbsent(reqType, r -> new ArrayList<>())
                                    .add(stack.getDisplayName()))
						);
			}
			if (reqType.blockApplicable) {
				ForgeRegistries.BLOCKS.getValues()
						.forEach(block -> core.getCommonReqData(new HashMap<>(), ObjectType.BLOCK, RegistryUtil.getId(block), reqType, new CompoundTag())
								.forEach((key, value) -> cache
										.computeIfAbsent(key, s -> new HashMap<>())
										.computeIfAbsent(value, v -> new HashMap<>())
										.computeIfAbsent(reqType, r -> new ArrayList<>())
										.add(new ItemStack(block.asItem()).getDisplayName())
								)
						);
			}
			if (reqType.entityApplicable) {
				ForgeRegistries.ENTITY_TYPES.getValues().stream()
						.map(entity -> entity.create(player.level()))
						.forEach(entity -> core.getReqMap(reqType, entity).forEach((key, value) -> cache
								.computeIfAbsent(key, s -> new HashMap<>())
								.computeIfAbsent(value, v -> new HashMap<>())
								.computeIfAbsent(reqType, r -> new ArrayList<>())
								.add(entity.getDisplayName())));
			}
			if (reqType == ReqType.TRAVEL) {
				player.level().registryAccess().registryOrThrow(Registries.BIOME).entrySet().stream()
						.map(entry -> entry.getKey().location())
						.forEach(biomeID -> core.getCommonReqData(new HashMap<>(), ObjectType.BIOME, biomeID, reqType, new CompoundTag())
								.forEach((key, value) -> cache
										.computeIfAbsent(key, s -> new HashMap<>())
										.computeIfAbsent(value, v -> new HashMap<>())
										.computeIfAbsent(reqType, r -> new ArrayList<>())
										.add(Component.literal(biomeID.toString())))
						);
				player.connection.levels().stream()
						.map(ResourceKey::location)
						.forEach(dimID -> core.getCommonReqData(new HashMap<>(), ObjectType.DIMENSION, dimID, reqType, new CompoundTag())
								.forEach((key, value) -> cache
										.computeIfAbsent(key, s -> new HashMap<>())
										.computeIfAbsent(value, v -> new HashMap<>())
										.computeIfAbsent(reqType, r -> new ArrayList<>())
										.add(Component.literal(dimID.toString())))
						);
			}
		});
	}

	public static void sendLevelUpUnlocks(String skill, int level) {
		if (!Config.SKILLUP_UNLOCKS.get())
			return;
		//check if the
		if (cache.isEmpty())
			cacheUnlocks();
		//send unlocks to chat
		LocalPlayer player = mc.player;
		cache.getOrDefault(skill, new HashMap<>()).getOrDefault(level, new HashMap<>()).entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.forEach(entry -> {
					MutableComponent header = Component.translatable("pmmo.enum." + entry.getKey().name());
					header.setStyle(Style.EMPTY.applyFormats(ChatFormatting.GOLD, ChatFormatting.BOLD));
					player.sendSystemMessage(header);
					entry.getValue().forEach(player::sendSystemMessage);
				});
	}
}
