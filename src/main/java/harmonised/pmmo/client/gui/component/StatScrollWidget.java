package harmonised.pmmo.client.gui.component;

import com.mojang.blaze3d.vertex.Tesselator;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.gui.GlossarySelectScreen.OBJECT;
import harmonised.pmmo.client.gui.GlossarySelectScreen.SELECTION;
import harmonised.pmmo.client.utils.ClientUtils;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StatScrollWidget extends ScrollPanel {
	private interface Element {void render(GuiGraphics graphics, int x, int y, int width, Tesselator tess);}
	
	private record TextElement(ClientTooltipComponent text, int xOffset, int color, boolean isHeader, int headerColor) implements Element{
		public static List<TextElement> build(Component component, int width, int xOffset, int color, boolean isHeader, int headerColor) {
			return format(component.copy(), width, xOffset, color, isHeader, headerColor);
		}
		
		public static List<TextElement> build(String key, int value, int width, int xOffset, int color) {
			return format(Component.translatable("pmmo."+key).append(Component.literal(": "+value)), width, xOffset, color, false, 0);
		}
		public static List<TextElement> build(String key, long value, int width, int xOffset, int color) {
			return format(Component.translatable("pmmo."+key).append(Component.literal(": "+value)), width, xOffset, color, false, 0);
		}
		public static List<TextElement> build(String key, double value, int width, int xOffset, int color) {
			return format(Component.translatable("pmmo."+key).append(Component.literal(": "+DP.dp(value*100)+"%")), width, xOffset, color, false, 0);
		}
		public static List<TextElement> build(Enum<?> type, int width, int xOffset, int color, boolean isHeader, int headerColor) {
			return format(Component.translatable("pmmo.enum."+type.name()), width - xOffset, xOffset, color, isHeader, headerColor);	
		}
		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, Tesselator tess) {
			if (isHeader()) 
				graphics.fill(RenderType.gui(), x, y, x+width, y+12, headerColor());
			MultiBufferSource.BufferSource buffer = graphics.bufferSource();
			text().renderText(Minecraft.getInstance().font, x + xOffset(), y, graphics.pose().last().pose(), buffer);
			buffer.endBatch();
		}
		
		private static List<TextElement> format(MutableComponent component, int width, int xOffset, int color, boolean isHeader, int headerColor) {
			return ClientUtils.ctc(component.withStyle(component.getStyle().withColor(color)), width).stream()
					.map(line -> new TextElement(line, xOffset, color, isHeader, headerColor)).toList();
		}
	}
	
	private record RenderableElement(Component text, int xOffset, int color, int headerColor, ItemStack stack, Block block, Entity entity) implements Element{
		RenderableElement(Component text, int xOffset, int color, int headerColor, ItemStack stack) {
			this(text, xOffset, color, headerColor, stack, null, null);
		}
		RenderableElement(Component text, int xOffset, int color, int headerColor, Block block) {
			this(text, xOffset, color, headerColor, null, block, null);
		}
		RenderableElement(Component text, int xOffset, int color, int headerColor, Entity entity) {
			this(text, xOffset, color, headerColor, null, null, entity);
		}
		@Override
		public void render(GuiGraphics graphics, int x, int y, int width, Tesselator tess) {
			graphics.fill(RenderType.gui(), x, y, x+width, y+12, headerColor());
			Font font = Minecraft.getInstance().font;
			if (stack() != null || block() != null) {
				ItemStack renderStack = stack() == null ? new ItemStack(block().asItem()) : stack();
				graphics.renderItem(renderStack, x+width - 25, y);
				graphics.drawString(font, renderStack.getDisplayName(), x + 10, y, 0xFFFFFF);
			}
			else if (entity instanceof LivingEntity renderEntity) {
				int scale = Math.max(1, 10 / Math.max(1, (int) renderEntity.getBoundingBox().getSize()));
				int px = x+width - 30;
				InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, px, y, px+30, y+30, scale, 0, 0f, 0f, renderEntity);
				graphics.drawString(font, renderEntity.getDisplayName(), x, y, 0xFFFFFF);
			}
		}
	}
	
	Minecraft mc = Minecraft.getInstance();
	Core core = Core.get(LogicalSide.CLIENT);
	private final List<Element> content = new ArrayList<>();

	private StatScrollWidget(int width, int height, int top, int left) {
		super(Minecraft.getInstance(), width, height, top, left, 4);
		CreativeModeTabs.tryRebuildTabContents(mc.player.connection.enabledFeatures(), mc.player.canUseGameMasterBlocks(), mc.player.clientLevel.registryAccess());
	}
	public StatScrollWidget(int width, int height, int top, int left, int pointless) {
		this(width, height, top, left);
		populateLocation(List.of(mc.level.dimension().location()), new ReqType[] {ReqType.TRAVEL}, new ModifierDataType[] {ModifierDataType.DIMENSION}, "", false, true, true);
		populateLocation(List.of(mc.level.getBiome(mc.player.blockPosition()).unwrapKey().get().location()), new ReqType[] {ReqType.TRAVEL}, new ModifierDataType[] {ModifierDataType.BIOME}, "", true, true, true);
	}
	public StatScrollWidget(int width, int height, int top, int left, ItemStack stack) {
		this(width, height, top, left);
		EventType[] events = stack.getItem() instanceof BlockItem ? EventType.BLOCKITEM_APPLICABLE_EVENTS : EventType.ITEM_APPLICABLE_EVENTS;
		ReqType[] reqs = stack.getItem() instanceof BlockItem ? ReqType.BLOCKITEM_APPLICABLE_EVENTS : ReqType.ITEM_APPLICABLE_EVENTS;
		populateItems(List.of(stack), events, reqs, ModifierDataType.values(), "", true, true);
	}
	public StatScrollWidget(int width, int height, int top, int left, Entity entity) {
		this(width, height, top, left);
		populateEntity(List.of(entity), EventType.ENTITY_APPLICABLE_EVENTS, ReqType.ENTITY_APPLICABLE_EVENTS, entity instanceof Player, "");
	}
	public StatScrollWidget(int width, int height, int top, int left, BlockPos pos) {
		this(width, height, top, left);
		populateBlockFromWorld(pos, ReqType.BLOCK_APPLICABLE_EVENTS);
	}
	public StatScrollWidget(int width, int height, int top, int left, SELECTION selection, OBJECT object, String skill, GuiEnumGroup type) {
		this(width, height, top, left);
		generateGlossary(selection, object, skill, type);
	}

	//Utility method for uniform nesting indentation
	private int step(int level) {return level * 10;}
	
	private final Supplier<List<ItemStack>> itemSupplier = () -> CreativeModeTabs.searchTab().getDisplayItems().stream().toList();
	
	public void generateGlossary(SELECTION selection, OBJECT object, String skill, GuiEnumGroup type) {
		switch (selection) {
		case REQS:{
			EventType[] events = new EventType[] {};
			ModifierDataType[] bonuses = new ModifierDataType[] {};
			switch (object) {
			case ITEMS: {
				populateItems(
					itemSupplier.get(),
					events,
					type == null ? ReqType.ITEM_APPLICABLE_EVENTS : new ReqType[] {(ReqType) type},
					bonuses,
					skill,
					false,
					false);
				break;}
			case BLOCKS: {
				populateBlocks(
						BuiltInRegistries.BLOCK.stream().toList(),
					events,
					type == null ? ReqType.BLOCK_APPLICABLE_EVENTS : new ReqType[] {(ReqType) type},
					false,
					skill);
				break;}
			case ENTITY: {
				populateEntity(
					BuiltInRegistries.ENTITY_TYPE.stream().map(entityType -> entityType.create(mc.level)).filter(Objects::nonNull).toList(),
					events,
					type == null ? ReqType.ENTITY_APPLICABLE_EVENTS : new ReqType[] {(ReqType) type},
					false,
					skill);
				break;}
			case DIMENSIONS: {
				populateLocation(mc.player.connection.levels().stream().map(ResourceKey::location).toList(),
					new ReqType[] {ReqType.TRAVEL}, bonuses, skill, false, false, false);
				break;}
			case BIOMES: {
				populateLocation(mc.player.level().registryAccess().registryOrThrow(Registries.BIOME).keySet().stream().toList(),
					new ReqType[] {ReqType.TRAVEL}, bonuses, skill, true, false, false);
				break;}
			case ENCHANTS: {
				var reg = mc.player.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
				populateEnchants(new ArrayList<>(reg.keySet()), skill);
				break;}
			default:{}
			}
			break;
		}
		case XP:{
			ReqType[] reqs = new ReqType[] {};
			ModifierDataType[] bonuses = new ModifierDataType[] {};
			switch (object) {
			case ITEMS: {
				populateItems(
						itemSupplier.get(),
						type == null ? EventType.ITEM_APPLICABLE_EVENTS : new EventType[] {(EventType) type},
						reqs, bonuses, skill, false, false);
				break;}
			case BLOCKS: {
				populateBlocks(
						BuiltInRegistries.BLOCK.stream().toList(),
						type == null ? EventType.BLOCK_APPLICABLE_EVENTS : new EventType[] {(EventType) type},
						reqs, false, skill);
				break;}
			case ENTITY: {
				populateEntity(
						BuiltInRegistries.ENTITY_TYPE.stream().map(entityType -> entityType.create(Minecraft.getInstance().level)).filter(entity -> entity != null).toList(),
						type == null ? EventType.ENTITY_APPLICABLE_EVENTS : new EventType[] {(EventType) type},
						reqs, false, skill);
				break;}
			case EFFECTS: {
				populateEffects(
						BuiltInRegistries.MOB_EFFECT.holders().collect(Collectors.toSet()),
						new EventType[] {EventType.EFFECT},
						reqs, skill);
				break;}
			default:{}
			}
			break;
		}
		case BONUS:{
			ReqType[] reqs = new ReqType[] {};
			EventType[] events = new EventType[] {};
			switch (object) {
			case ITEMS: {
				populateItems(
						itemSupplier.get(),
						events, reqs, 
						type == null ? ModifierDataType.values() : new ModifierDataType[] {(ModifierDataType) type}, 
						skill, false, false);
				break;}
			case DIMENSIONS: {
				populateLocation(mc.player.connection.levels().stream().map(key -> key.location()).toList(),
						reqs, type == null ? ModifierDataType.values() : new ModifierDataType[] {(ModifierDataType) type}, skill, false, false, false);
				break;}
			case BIOMES: {
				populateLocation(mc.player.level().registryAccess().registryOrThrow(Registries.BIOME).keySet().stream().toList(),
						reqs, type == null ? ModifierDataType.values() : new ModifierDataType[] {(ModifierDataType) type}, skill, true, false, false);
				break;}
			default:{}
			}
			break;}
		case SALVAGE: {
			if (object == OBJECT.ITEMS) {
				populateItems(
						itemSupplier.get(),
						new EventType[] {}, new ReqType[] {}, new ModifierDataType[] {}, skill, true, false);
			}
			break;}
		case VEIN: {
			ReqType[] reqs = new ReqType[] {};
			EventType[] events = new EventType[] {};
			ModifierDataType[] bonuses = new ModifierDataType[] {};
			switch (object) {
			case ITEMS: {
				populateItems(
						itemSupplier.get(),
						events, reqs, bonuses, skill, false, true);
				break;}
			case BLOCKS: {
				populateBlocks(
						BuiltInRegistries.BLOCK.stream().toList(),
						events,	reqs, true, skill);
				break;}
			case DIMENSIONS: {
				populateLocation(mc.player.connection.levels().stream().map(key -> key.location()).toList(),
						reqs, bonuses, skill, false, true, false);
				break;}
			case BIOMES: {
				populateLocation(mc.player.level().registryAccess().registryOrThrow(Registries.BIOME).keySet().stream().toList(),
						reqs, bonuses, skill, true, true, false);
				break;}
			default:{}
			}
			break;}
		case MOB_SCALING: {
			ReqType[] reqs = new ReqType[] {};
			ModifierDataType[] bonuses = new ModifierDataType[] {};
			switch(object) {
			case DIMENSIONS: {
				populateLocation(mc.player.connection.levels().stream().map(key -> key.location()).toList(),
						reqs, bonuses, skill, false, false, true);
				break;}
			case BIOMES: {
				populateLocation(mc.player.level().registryAccess().registryOrThrow(Registries.BIOME).keySet().stream().toList(),
						reqs, bonuses, skill, true, false, true);
				break;}
			default:{}
			}
			break;}
		case PERKS: {
			populatePerks(skill);
			break;}
		default:{}
		}
	}

	private void populateItems(List<ItemStack> items, EventType[] events, ReqType[] reqs, ModifierDataType[] modifiers, String skillFilter, boolean includeSalvage, boolean includeVein) {
		for (ItemStack stack : items) {
			int lengthBeforeProcessing = content.size() + 1;
			if (items.size() > 1) 
				content.add(new RenderableElement(stack.getDisplayName(), 1, stack.getRarity().color().getColor(), Config.SECTION_HEADER_COLOR.get(), stack));
			addEventSection((event -> {
				Map<String, Long> map = core.getExperienceAwards(event, stack, mc.player, new CompoundTag());
				if (stack.getItem() instanceof BlockItem)
					map = core.getCommonXpAwardData(new HashMap<>(), event, RegistryUtil.getId(stack), mc.player, ObjectType.BLOCK, TagUtils.stackTag(stack, mc.level));
				return map;
				}), events, skillFilter);
			addReqSection((reqType -> {
				Map<String, Long> reqMap = core.getReqMap(reqType, stack, mc.level, true);
				if (reqType == ReqType.USE_ENCHANTMENT)
					core.getEnchantReqs(stack).forEach((skill, level) -> reqMap.merge(skill, level, (o,n) -> o>n ? o : n));
				if (stack.getItem() instanceof BlockItem)
					reqMap.putAll(core.getCommonReqData(new HashMap<>(), ObjectType.BLOCK, RegistryUtil.getId(stack), reqType, TagUtils.stackTag(stack, mc.level)));
				return reqMap;
				}),	
				CoreUtils.getEffects(core.getLoader().getLoader(ObjectType.ITEM).getData(RegistryUtil.getId(stack)).getNegativeEffect(), true), 
				reqs, skillFilter);
			addModifierSection((mod -> core.getTooltipRegistry().bonusTooltipExists(RegistryUtil.getId(stack), mod) ?
						core.getTooltipRegistry().getBonusTooltipData(RegistryUtil.getId(stack), mod, stack) :
						core.getObjectModifierMap(ObjectType.ITEM, RegistryUtil.getId(stack), mod, TagUtils.stackTag(stack, mc.level))
				), modifiers, skillFilter);
			if (includeSalvage)
				addSalvageSection(core.getLoader().ITEM_LOADER.getData(RegistryUtil.getId(stack)).salvage());
			if (includeVein)
				addItemVeinSection(core.getLoader().ITEM_LOADER.getData(RegistryUtil.getId(stack)).veinData(), stack.getItem() instanceof BlockItem);
			if (lengthBeforeProcessing == content.size())
				content.remove(content.size()-1);
		}
	}
	
	@SuppressWarnings("resource")
	private void populateBlockFromWorld(BlockPos block, ReqType[] reqs) {
		addEventSection((event -> core.getExperienceAwards(event, block, Minecraft.getInstance().level, null, new CompoundTag())), EventType.BLOCK_APPLICABLE_EVENTS, "");
		addReqSection((reqType -> core.getReqMap(reqType, block, Minecraft.getInstance().level)), new ArrayList<>(), reqs, "");
		addBlockVeinSection(core.getLoader().BLOCK_LOADER.getData(RegistryUtil.getId(Minecraft.getInstance().level.getBlockState(block))).veinData());
	}
	
	private static final String PREDICATE_KEY = "usesPredicate";
	@SuppressWarnings("deprecation")
	private void populateBlocks(Collection<Block> blocks, EventType[] events, ReqType[] reqs, boolean includeVein, String skillFilter) {
		for (Block block : blocks) {
			int lengthBeforeProcessing = content.size() + 1;
			ItemStack stack = new ItemStack(block.asItem());
			ResourceLocation id = RegistryUtil.getId(block);
			content.add(new RenderableElement(stack.getDisplayName(), 1, stack.getRarity().color().getColor(), Config.SECTION_HEADER_COLOR.get(), block));
			addEventSection((event -> core.getTooltipRegistry().xpGainTooltipExists(id, event)
					? Collections.singletonMap(PREDICATE_KEY, 0L)
					: core.getObjectExperienceMap(ObjectType.BLOCK, id, event, new CompoundTag()))
				, events, skillFilter);
			addReqSection((reqType -> core.getPredicateRegistry().predicateExists(id, reqType)
					? Collections.singletonMap(PREDICATE_KEY, 0L)
					: core.getObjectSkillMap(ObjectType.BLOCK, id, reqType, new CompoundTag()))
				, new ArrayList<>()
				, reqs, skillFilter);
			if (includeVein)
				addBlockVeinSection(core.getLoader().BLOCK_LOADER.getData(RegistryUtil.getId(block)).veinData());
			if (lengthBeforeProcessing == content.size())
				content.remove(content.size()-1);
		}
	}
	
	private void populateEntity(List<? extends Entity> entities, EventType[] events, ReqType[] reqs, boolean isPlayer, String skillFilter) {
		for (Entity entity : entities) {
			int lengthBeforeProcessing = content.size() + 1;
			if (entities.size() > 1)
				content.add(new RenderableElement(entity.getDisplayName(), 1, 0xEEEEEE, Config.SECTION_HEADER_COLOR.get(), entity));
			addEventSection((event -> core.getExperienceAwards(event, entity, null, new CompoundTag())), events, skillFilter);
			addReqSection((reqType -> core.getReqMap(reqType, entity)), new ArrayList<>(), reqs, skillFilter);
			if (isPlayer)
				addPlayerSection(entity);
			if (lengthBeforeProcessing == content.size())
				content.remove(content.size()-1);
		}
	}
	
	private void populateEffects(Collection<Holder<MobEffect>> effects, EventType[] events, ReqType[] reqs, String skillFilter) {
		for (Holder<MobEffect> effect : effects) {
			int lengthBeforeProcessing = content.size() + 1;
			if (effects.size() > 1)
				content.addAll(TextElement.build(effect.value().getDisplayName(), this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
			List<TextElement> holder = new ArrayList<>();
			for (int lvl = 0; lvl <= getEffectHighestConfiguration(effect); lvl++) {
				Map<String, Long> xpMap = core.getExperienceAwards(new MobEffectInstance(effect, 30, lvl), null, new CompoundTag());
				if (!xpMap.isEmpty() && !xpMap.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
					holder.addAll(TextElement.build(Component.literal(String.valueOf(lvl)), this.width, 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> map : xpMap.entrySet()) {
						if (map.getValue() == 0) continue;
						holder.addAll(TextElement.build(map.getKey(), map.getValue(), this.width, step(1), CoreUtils.getSkillColor(map.getKey())));
					}
				}
			}
			if (!holder.isEmpty()) {
				content.addAll(TextElement.build(LangProvider.EVENT_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
			if (lengthBeforeProcessing == content.size())
				content.remove(content.size()-1);
		}
	}
	
	private int getEffectHighestConfiguration(Holder<MobEffect> effect) {
		DataSource<?> data = core.getLoader().getLoader(ObjectType.EFFECT).getData().get(effect.unwrapKey().get().location());
		return data == null ? 0 : ((EnhancementsData)data).skillArray().keySet().stream().max(Comparator.naturalOrder()).orElse(-1);
	}
	
	private void populateLocation(List<ResourceLocation> locations, ReqType[] reqs, ModifierDataType[] modifiers, String skillFilter, boolean isBiome, boolean includeVein, boolean includeScaling) {
		locations.forEach(loc -> {
			int lengthBeforeProcessing = content.size() + 1;
			if (locations.size() > 1)
				content.addAll(TextElement.build(Component.literal(loc.toString()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
			addReqSection((reqType -> core.getObjectSkillMap(isBiome ? ObjectType.BIOME : ObjectType.DIMENSION, 
					loc, reqType, new CompoundTag())), 
					isBiome 
						? CoreUtils.getEffects(core.getLoader().getLoader(ObjectType.BIOME).getData(loc).getNegativeEffect(), true) 
						: new ArrayList<>(), 
					reqs, skillFilter);
			if (reqs.length > 0 && isBiome)
				addReqEffectSection(CoreUtils.getEffects(isBiome 
						? core.getLoader().getLoader(ObjectType.BIOME).getData(loc).getPositiveEffect()
						: core.getLoader().getLoader(ObjectType.DIMENSION).getData(loc).getPositiveEffect(), false), false);
			addModifierSection((mod -> core.getObjectModifierMap(isBiome ? ObjectType.BIOME : ObjectType.DIMENSION, loc, mod, new CompoundTag())), modifiers, skillFilter);
			if (includeVein)
				addVeinBlacklistSection(isBiome ? ObjectType.BIOME : ObjectType.DIMENSION, loc);
			if (includeScaling)
				addMobModifierSection(isBiome ? ObjectType.BIOME : ObjectType.DIMENSION, loc);
			if (lengthBeforeProcessing == content.size())
				content.remove(content.size()-1);
		});
	}
	
	private void populateEnchants(List<ResourceLocation> enchants, String skillFilter) {
		enchants.forEach(ench -> {
			int lengthBeforeProcessing = content.size() + 1;
			if (enchants.size() > 1)
				content.addAll(TextElement.build(Component.literal(ench.toString()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
			List<TextElement> holder = new ArrayList<>();
			for (int i = 0; i <= mc.player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).get(ench).getMaxLevel(); i++) {
				Map<String, Long> reqMap = core.getEnchantmentReqs(ench, i).entrySet().stream().filter(entry -> entry.getKey().contains(skillFilter)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				if (!reqMap.isEmpty() && !reqMap.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
					holder.addAll(TextElement.build(Component.literal(String.valueOf(i)), this.width, 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> map : reqMap.entrySet()) {
						if (map.getValue() == 0) continue;
						holder.addAll(TextElement.build(map.getKey(), map.getValue(), this.width, step(1), CoreUtils.getSkillColor(map.getKey())));
					}
				}
			}
			if (holder.size() > 0) {
				content.addAll(TextElement.build(LangProvider.REQ_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
			if (lengthBeforeProcessing == content.size())
				content.remove(content.size()-1);
		});
	}

	@SuppressWarnings("resource")
	private void populatePerks(String skillFilter) {
		for (EventType cause : EventType.values()) {
			List<TextElement> holder = new ArrayList<>();
			Player player = Minecraft.getInstance().player;
			Config.perks().perks().getOrDefault(cause, new ArrayList<>()).forEach(nbt -> {
				if (nbt.contains(APIUtils.SKILLNAME) && !nbt.getString(APIUtils.SKILLNAME).equals(skillFilter)) return;
				ResourceLocation perkID = Reference.of(nbt.getString("perk"));
				nbt.putLong(APIUtils.SKILL_LEVEL, nbt.contains(APIUtils.SKILLNAME)
						? Core.get(player.level()).getData().getLevel(nbt.getString(APIUtils.SKILLNAME), player.getUUID())
						: 0);
				holder.addAll(TextElement.build(Component.translatable("perk."+perkID.getNamespace()+"."+perkID.getPath()), 
						this.width,	step(1), 0x00ff00, false, 0x00ff00));
				holder.addAll(TextElement.build(core.getPerkRegistry().getDescription(perkID).copy(), 
						this.width,	step(1), 0x99ccff, false, 0x99ccff));
				for (MutableComponent line : core.getPerkRegistry().getStatusLines(perkID, player, nbt)) {
					holder.addAll(TextElement.build(line, this.width, step(2), 0xAAFFFF, false, 0xAAFFFF));
				}
			});
			if (holder.size() > 0) {
				content.addAll(TextElement.build(cause, this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
		}
	}
	
	private void addEventSection(Function<EventType, Map<String,Long>> xpSrc, EventType[] events, String skillFilter) {
		if (events.length > 0) {
			List<TextElement> holder = new ArrayList<>();
			for (EventType event : events) {
				Map<String, Long> xpAwards = CoreUtils.processSkillGroupXP(xpSrc.apply(event)).entrySet().stream().filter(entry -> entry.getKey().contains(skillFilter)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				if (xpAwards.containsKey(PREDICATE_KEY))
					holder.addAll(TextElement.build(LangProvider.ADDON_AFFECTED_ATTRIBUTE.asComponent(), this.width, 5, 0xFF9C03, false, 0x000000));
				else if (!xpAwards.isEmpty()) {
					holder.addAll(TextElement.build(event, this.width, 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> map : xpAwards.entrySet()) {
						holder.addAll(TextElement.build(map.getKey(), map.getValue(), this.width, 5, CoreUtils.getSkillColor(map.getKey())));
					}
				}
			}
			if (!holder.isEmpty()) {
				content.addAll(TextElement.build(LangProvider.EVENT_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
		}
	}
	
	private void addReqSection(Function<ReqType, Map<String, Long>> reqSrc, List<MobEffectInstance> reqEffects, ReqType[] reqs, String skillFilter) {
		if (reqs.length > 0) {
			List<TextElement> holder = new ArrayList<>();
			for (ReqType reqType: reqs) {
				Map<String, Long> reqMap = CoreUtils.processSkillGroupReqs(reqSrc.apply(reqType)).entrySet().stream().filter(entry -> entry.getKey().contains(skillFilter)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				if (!reqMap.isEmpty() && !reqMap.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
					holder.addAll(TextElement.build(reqType, this.width, 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> map : reqMap.entrySet()) {
						if (map.getValue() == 0) continue;
						holder.addAll(TextElement.build(map.getKey(), map.getValue(), this.width, step(1), CoreUtils.getSkillColor(map.getKey())));
					}
				}
			}
			if (!holder.isEmpty()) {
				content.addAll(TextElement.build(LangProvider.REQ_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
				addReqEffectSection(reqEffects, true);
			}
		}
	}
	
	private void addReqEffectSection(List<MobEffectInstance> reqEffects, boolean isNegative) {
		if (!reqEffects.isEmpty()) {
			content.addAll(TextElement.build(isNegative ? LangProvider.REQ_EFFECTS_HEADER.asComponent() : LangProvider.BIOME_EFFECT_POS.asComponent(), this.width, 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			for (MobEffectInstance mei : reqEffects) {
				content.addAll(TextElement.build(mei.getEffect().value().getDisplayName(), this.width, step(1), 0xFFFFFF, false, 0));
			}
		}
	}
	
	private void addModifierSection(Function<ModifierDataType, Map<String, Double>> bonusSrc, ModifierDataType[] mods, String skillFilter) {
		if (mods.length > 0) {
			List<TextElement> holder = new ArrayList<>();
			for (ModifierDataType mod : mods) {
				Map<String, Double> modifiers = bonusSrc.apply(mod).entrySet().stream().filter(entry -> entry.getKey().contains(skillFilter)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				if (!modifiers.isEmpty()) {
					holder.addAll(TextElement.build(mod, this.width, 1, 0xFFFFFF, false, 0));
					modifiers.forEach((key, value) 
							-> holder.addAll(TextElement.build(key, value, this.width, step(1), CoreUtils.getSkillColor(key))));
				}
			}
			if (!holder.isEmpty()) {
				content.addAll(TextElement.build(LangProvider.MODIFIER_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
		}
	}
	
	private void addSalvageSection(Map<ResourceLocation, SalvageData> salvage) {
		if (!salvage.isEmpty()) {
			content.addAll(TextElement.build(LangProvider.SALVAGE_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			for (Map.Entry<ResourceLocation, SalvageData> salvageEntry : salvage.entrySet()) {
				SalvageData data = salvageEntry.getValue();
				ItemStack resultStack = new ItemStack(BuiltInRegistries.ITEM.get(salvageEntry.getKey()));
				content.addAll(TextElement.build(resultStack.getDisplayName(), this.width, step(1), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
				if (!data.levelReq().isEmpty()) {
					content.addAll(TextElement.build(LangProvider.SALVAGE_LEVEL_REQ.asComponent().withStyle(ChatFormatting.UNDERLINE), this.width, step(1), 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> req : data.levelReq().entrySet()) {
						content.addAll(TextElement.build(req.getKey(), req.getValue(), this.width, step(2), CoreUtils.getSkillColor(req.getKey())));
					}
				}
				content.addAll(TextElement.build(LangProvider.SALVAGE_CHANCE.asComponent(data.baseChance(), data.maxChance()).withStyle(ChatFormatting.UNDERLINE), this.width, step(1), 0xFFFFFF, false, 0));
				content.addAll(TextElement.build(LangProvider.SALVAGE_MAX.asComponent(data.salvageMax()).withStyle(ChatFormatting.UNDERLINE), this.width, step(1), 0xFFFFFF, false, 0));
				if (!data.chancePerLevel().isEmpty()) {
					content.addAll(TextElement.build(LangProvider.SALVAGE_CHANCE_MOD.asComponent().withStyle(ChatFormatting.UNDERLINE), this.width, step(1), 0xFFFFFF, false, 0));
					for (Map.Entry<String, Double> perLevel : data.chancePerLevel().entrySet()) {
						content.addAll(TextElement.build(perLevel.getKey(), perLevel.getValue(), this.width, step(2), CoreUtils.getSkillColor(perLevel.getKey())));
					}
				}
				if (!data.xpAward().isEmpty()) {
					content.addAll(TextElement.build(LangProvider.SALVAGE_XP_AWARD.asComponent().withStyle(ChatFormatting.UNDERLINE), this.width, step(1), 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> award : data.xpAward().entrySet()) {
						content.addAll(TextElement.build(award.getKey(), award.getValue(), this.width, step(2), CoreUtils.getSkillColor(award.getKey())));
					}
				}
			}
		}
	}
	
	private void addItemVeinSection(VeinData veinData, boolean isBlockItem) {
		if (!veinData.isEmpty()) {
			content.addAll(TextElement.build(LangProvider.VEIN_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			content.addAll(TextElement.build(LangProvider.VEIN_RATE.asComponent(veinData.chargeRate.orElse(0d) * 2d), this.width, step(1), 0xFFFFFF, false, 0));
			content.addAll(TextElement.build(LangProvider.VEIN_CAP.asComponent(veinData.chargeCap.orElse(0)), this.width, step(1), 0xFFFFFF, false, 0));
			if (isBlockItem)
				content.addAll(TextElement.build(LangProvider.VEIN_CONSUME.asComponent(veinData.consumeAmount.orElse(0)), this.width, step(1), 0xFFFFFF, false, 0));
		}
	}
	
	private void addBlockVeinSection(VeinData veinData) {
		if (veinData.consumeAmount.orElse(0) != 0) {
			content.addAll(TextElement.build(LangProvider.VEIN_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			content.addAll(TextElement.build(LangProvider.VEIN_CONSUME.asComponent(veinData.consumeAmount.orElse(0)), this.width, step(1), 0xFFFFFF, false, 0));
		}
	}
	
	private void addPlayerSection(Entity entity) {
		//Section for player-specific data as it expands
		content.addAll(TextElement.build(LangProvider.PLAYER_HEADER.asComponent(), this.width, step(1), 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
		PlayerData data = core.getLoader().PLAYER_LOADER.getData(Reference.mc(entity.getUUID().toString()));
		content.addAll(TextElement.build(LangProvider.PLAYER_IGNORE_REQ.asComponent(data.ignoreReq()), this.width, step(2), 0xFFFFFF, false, 0));
		if (!data.bonuses().isEmpty()) {
			content.addAll(TextElement.build(LangProvider.PLAYER_BONUSES.asComponent(), this.width, step(2), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
			for (Map.Entry<String, Double> bonus : data.bonuses().entrySet()) {
				content.addAll(TextElement.build(bonus.getKey(), bonus.getValue(), this.width, step(3), CoreUtils.getSkillColor(bonus.getKey())));
			}
		}
		
		//Section for skills
		Map<String, Experience> rawXp = core.getData().getXpMap(entity.getUUID());
		LinkedHashMap<String, Long> orderedMap = new LinkedHashMap<>();
		List<String> skillKeys = new ArrayList<>(rawXp.keySet().stream().toList());
		skillKeys.stream().sorted(Comparator.<String>comparingLong(a -> rawXp.get(a).getLevel().getLevel()).reversed());
		skillKeys.forEach(skill -> {
			orderedMap.put(skill, core.getData().getLevel(skill, null));
		});
		content.addAll(TextElement.build(LangProvider.SKILL_LIST_HEADER.asComponent(), step(1), this.width, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
		for (Map.Entry<String, Long> rawMap : orderedMap.entrySet()) {
			content.addAll(TextElement.build(rawMap.getKey(), rawMap.getValue(), this.width, step(2), CoreUtils.getSkillColor(rawMap.getKey())));
		}
	}
	
	private void addVeinBlacklistSection(ObjectType type, ResourceLocation location) {
		LocationData loader = (LocationData) core.getLoader().getLoader(type).getData(location);
		if (!loader.veinBlacklist().isEmpty()) {
			content.addAll(TextElement.build(LangProvider.VEIN_BLACKLIST_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, step(1), 0xFFFFFF, false, 0));
			for (ResourceLocation blockID : loader.veinBlacklist()) {
				content.addAll(TextElement.build(Component.literal(blockID.toString()), this.width, step(2), 0xEEEEEE, false, 0));
			}
		}
	}
	
	private void addMobModifierSection(ObjectType type, ResourceLocation location) {
		if (type != ObjectType.BIOME && type != ObjectType.DIMENSION) 
			return;
		LocationData loader = (LocationData) core.getLoader().getLoader(type).getData(location);
		if (!loader.mobModifiers().isEmpty()) {
			content.addAll(TextElement.build(LangProvider.MOB_MODIFIER_HEADER.asComponent().withStyle(ChatFormatting.BOLD), this.width, step(1), 0xFFFFFF, false, 0));
			for (Map.Entry<ResourceLocation, Map<String, Double>> mobMap : loader.mobModifiers().entrySet()) {
				Entity entity = BuiltInRegistries.ENTITY_TYPE.get(mobMap.getKey()).create(mc.level);
				content.add(new RenderableElement(entity.getName(), step(1), 0xFFFFFF, Config.SALVAGE_ITEM_COLOR.get(), entity));
				for (Map.Entry<String, Double> map : mobMap.getValue().entrySet()) {
					Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(Reference.of(map.getKey()));
					MutableComponent text = attribute == null ? Component.literal(map.getKey()) : Component.translatable(attribute.getDescriptionId());
					text.append(Component.literal(": "+map.getValue()));
					content.addAll(TextElement.build(text, this.width, step(2), 0xFFFFFF, false, 0xFFFFFF));
				}
			}
		}
	}

	@Override
	public NarrationPriority narrationPriority() { return NarrationPriority.NONE; }

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {}

	@Override
	protected int getContentHeight() {return content.size() * 12;}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll, double delta) {
		return super.mouseScrolled(mouseX, mouseY, scroll, delta);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int partialTicks) {
		return super.mouseClicked(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
		for (int i = 0; i < content.size(); i++) {
			content.get(i).render(guiGraphics, this.left, (int)(relativeY + (i*12) - scrollDistance), this.width, tess);			
		}
	}


}
