package harmonised.pmmo.client.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

	public static void sendLevelUpUnlocks(String skill, int oldLevel, int level) {
		if (!Config.SKILLUP_UNLOCKS.get())
			return;
		LocalPlayer player = mc.player;
		Core core = Core.get(LogicalSide.CLIENT);
		player.sendSystemMessage(LangProvider.LEVEL_UP_HEADER.asComponent(level, Component.translatable("pmmo."+skill)));
		Arrays.stream(ReqType.values()).forEach(reqType -> {
			List<Component> objects = new ArrayList<>();
			if (reqType.itemApplicable) {
				ForgeRegistries.ITEMS.getValues().stream()
						.map(ItemStack::new)
						.filter(stack -> {
							int objectLevel = core.getReqMap(reqType, stack, false).getOrDefault(skill, Integer.MAX_VALUE);
							return objectLevel > oldLevel && objectLevel <= level;
						})
						.map(ItemStack::getDisplayName)
						.forEach(objects::add);
			}
			if (reqType.blockApplicable) {
				ForgeRegistries.BLOCKS.getValues().stream()
						.filter(block -> {
							int objectLevel = core.getCommonReqData(new HashMap<>(), ObjectType.BLOCK, RegistryUtil.getId(block), reqType, new CompoundTag()).getOrDefault(skill, Integer.MAX_VALUE);
							return objectLevel > oldLevel && objectLevel <= level;
						})
						.map(block -> new ItemStack(block.asItem()).getDisplayName())
						.forEach(objects::add);
			}
			if (reqType.entityApplicable) {
				ForgeRegistries.ENTITY_TYPES.getValues().stream()
						.map(entity -> entity.create(player.level()))
						.filter(entity -> {
							int objectLevel = core.getReqMap(reqType, entity).getOrDefault(skill, Integer.MAX_VALUE);
							return objectLevel > oldLevel && objectLevel <= level;
						})
						.map(Entity::getDisplayName)
						.forEach(objects::add);
			}
			if (reqType == ReqType.TRAVEL) {
				player.level().registryAccess().registryOrThrow(Registries.BIOME).entrySet().stream()
						.map(entry -> entry.getKey().location())
						.filter(id -> {
							int objectLevel = core.getCommonReqData(new HashMap<>(), ObjectType.BIOME, id, reqType, new CompoundTag()).getOrDefault(skill, Integer.MAX_VALUE);
							return objectLevel > oldLevel && objectLevel <= level;
						})
						.map(id -> Component.literal(id.toString()))
						.forEach(objects::add);
				player.connection.levels().stream()
						.map(ResourceKey::location)
						.filter(id -> {
							int objectLevel = core.getCommonReqData(new HashMap<>(), ObjectType.DIMENSION, id, reqType, new CompoundTag()).getOrDefault(skill, Integer.MAX_VALUE);
							return objectLevel > oldLevel && objectLevel <= level;
						})
						.map(id -> Component.literal(id.toString()))
						.forEach(objects::add);
			}
			if (!objects.isEmpty()) {
				MutableComponent header = Component.translatable("pmmo.enum." + reqType.name());
				header.setStyle(Style.EMPTY.applyFormats(ChatFormatting.GOLD, ChatFormatting.BOLD));
				player.sendSystemMessage(header);
				objects.forEach(player::sendSystemMessage);
			}
		});
	}
}
