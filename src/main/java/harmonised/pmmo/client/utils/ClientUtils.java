package harmonised.pmmo.client.utils;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.gui.glossary.Glossary;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClientUtils {
	private static Minecraft mc = Minecraft.getInstance();
	public static List<ClientTooltipComponent> ctc(MutableComponent component, int width) {
		return mc.font.split(component, width).stream().map(ClientTooltipComponent::create).toList();
	}
	private static Map<String, Map<Long, Map<ReqType, List<Component>>>> cache = new HashMap<>();
	public static void invalidateUnlocksCache() {cache.clear();}
	public static Glossary glossary = null;

	private static void cacheUnlocks() {
		LocalPlayer player = mc.player;
		Core core = Core.get(LogicalSide.CLIENT);
		Arrays.stream(ReqType.values()).forEach(reqType -> {
			List<Component> objects = new ArrayList<>();
			if (reqType.itemApplicable) {
				BuiltInRegistries.ITEM.stream()
						.map(ItemStack::new)
						.forEach(stack ->
								core.getReqMap(reqType, stack, player.level(), false).forEach((key, value) -> cache
										.computeIfAbsent(key, s -> new HashMap<>())
										.computeIfAbsent(value, v -> new HashMap<>())
										.computeIfAbsent(reqType, r -> new ArrayList<>())
										.add(stack.getDisplayName()))
						);
			}
			if (reqType.blockApplicable) {
				BuiltInRegistries.BLOCK.stream()
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
				BuiltInRegistries.ENTITY_TYPE.stream()
						.map(entity -> entity.create(player.level(), EntitySpawnReason.TRIGGERED))
						.filter(Objects::nonNull)
						.forEach(entity -> core.getReqMap(reqType, entity).forEach((key, value) -> cache
								.computeIfAbsent(key, s -> new HashMap<>())
								.computeIfAbsent(value, v -> new HashMap<>())
								.computeIfAbsent(reqType, r -> new ArrayList<>())
								.add(entity.getDisplayName())));
			}
			if (reqType == ReqType.TRAVEL) {
				player.level().registryAccess().lookupOrThrow(Registries.BIOME).entrySet().stream()
						.map(entry -> entry.getKey().identifier())
						.forEach(biomeID -> core.getCommonReqData(new HashMap<>(), ObjectType.BIOME, biomeID, reqType, new CompoundTag())
								.forEach((key, value) -> cache
										.computeIfAbsent(key, s -> new HashMap<>())
										.computeIfAbsent(value, v -> new HashMap<>())
										.computeIfAbsent(reqType, r -> new ArrayList<>())
										.add(Component.literal(biomeID.toString())))
						);
				player.connection.levels().stream()
						.map(ResourceKey::identifier)
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

	public static void sendLevelUpUnlocks(String skill, long oldLevel, long level) {
		if (!Config.SKILLUP_UNLOCKS.get())
			return;
		if (cache.isEmpty()) cacheUnlocks();
		LocalPlayer player = mc.player;
		Map<ReqType, List<Component>> reqMap = cache.getOrDefault(skill, new HashMap<>()).getOrDefault(level, new HashMap<>());
		boolean isEmpty = reqMap.entrySet().stream().allMatch(entry -> entry.getValue().isEmpty());
		if (isEmpty && Config.SKILLUP_UNLOCKS_STRICT.get()) return;
		player.displayClientMessage(isEmpty
				? LangProvider.LEVEL_UP_HEADER.asComponent(level, Component.translatable("pmmo."+skill))
				: LangProvider.LEVEL_UP_HEADER_WITH_UNLOCKS.asComponent(level, Component.translatable("pmmo."+skill)),
				false);
		reqMap.entrySet().stream()
				.filter(entry -> Config.server().requirements().isEnabled(entry.getKey()) && !entry.getValue().isEmpty())
				.forEach(entry -> {
					MutableComponent header = Component.translatable("pmmo.enum." + entry.getKey().name());
					header.setStyle(Style.EMPTY.applyFormats(ChatFormatting.GOLD, ChatFormatting.BOLD));
					player.displayClientMessage(header, false);
					entry.getValue().forEach(val -> player.displayClientMessage(val, false));
				});
	}
}
