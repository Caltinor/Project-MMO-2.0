package harmonised.pmmo.client.gui.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.gui.GlossarySelectScreen.OBJECT;
import harmonised.pmmo.client.gui.GlossarySelectScreen.SELECTION;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

public class StatScrollWidget extends ScrollPanel{
	private static interface Element {public void render(PoseStack poseStack, int x, int y, int width, ItemRenderer itemRenderer);}
	
	private static record TextElement(Component text, int xOffset, int color, boolean isHeader, int headerColor) implements Element{
		public TextElement(String key, int value, int xOffset, int color) {
			this(Component.translatable("pmmo."+key).append(Component.literal(": "+value)), xOffset, color, false, 0);
		}
		public TextElement(String key, long value, int xOffset, int color) {
			this(Component.translatable("pmmo."+key).append(Component.literal(": "+value)), xOffset, color, false, 0);
		}
		public TextElement(String key, double value, int xOffset, int color) {
			this(Component.translatable("pmmo."+key).append(Component.literal(": "+DP.dp(value*100)+"%")), xOffset, color, false, 0);
		}
		public TextElement(Enum<?> type, int xOffset, int color, boolean isHeader, int headerColor) {
			this(Component.translatable("pmmo.enum."+type.name()), xOffset, color, isHeader, headerColor);
		}
		@SuppressWarnings("resource")
		@Override
		public void render(PoseStack poseStack, int x, int y, int width, ItemRenderer unused) {
			if (isHeader()) 
				GuiComponent.fill(poseStack, x, y, x+width, y+12, headerColor());
			GuiComponent.drawString(poseStack, Minecraft.getInstance().font, text(), x + xOffset(), y, color());
		}
	}
	
	private static record RenderableElement(Component text, int xOffset, int color, int headerColor, ItemStack stack, Block block, Entity entity) implements Element{
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
		public void render(PoseStack poseStack, int x, int y, int width, ItemRenderer itemRenderer) {
			GuiComponent.fill(poseStack, x, y, x+width, y+12, headerColor());
			@SuppressWarnings("resource")
			Font font = Minecraft.getInstance().font;
			if (stack() != null || block() != null) {
				ItemStack renderStack = stack() == null ? new ItemStack(block().asItem()) : stack();
				itemRenderer.renderAndDecorateItem(renderStack, x+width - 25, y);
				GuiComponent.drawString(poseStack, font, renderStack.getDisplayName(), x + 10, y, 0xFFFFFF);
			}
			else if (entity != null && entity instanceof LivingEntity) {
				int scale = Math.max(1, 10 / Math.max(1, (int) entity.getBoundingBox().getSize()));
				if (Config.RENDER_ENTITIES_IN_GUI.get())
					InventoryScreen.renderEntityInInventory(x+width - 20, y+12,  scale, (float)(x+ 51) - 100, (float)(y + 75 - 50) - 100, (LivingEntity) entity);
				GuiComponent.drawString(poseStack, font, this.entity.getDisplayName(), x, y, 0xFFFFFF);
			}
		}
	}
	
	Minecraft mc = Minecraft.getInstance();
	Core core = Core.get(LogicalSide.CLIENT);
	ItemRenderer itemRenderer = null;
	private final List<Element> content = new ArrayList<>();

	private StatScrollWidget(int width, int height, int top, int left) {
		super(Minecraft.getInstance(), width, height, top, left, 4);
	}
	public StatScrollWidget(int width, int height, int top, int left, int pointless) {
		this(width, height, top, left);
		populateLocation(List.of(mc.level.dimension().location()), new ReqType[] {ReqType.TRAVEL}, new ModifierDataType[] {ModifierDataType.DIMENSION}, "", false, true, true);
		populateLocation(List.of(mc.level.getBiome(mc.player.blockPosition()).unwrapKey().get().location()), new ReqType[] {ReqType.TRAVEL}, new ModifierDataType[] {ModifierDataType.BIOME}, "", true, true, true);
	}
	public StatScrollWidget(int width, int height, int top, int left, ItemStack stack, ItemRenderer itemRenderer) {
		this(width, height, top, left);
		this.itemRenderer = itemRenderer;
		EventType[] events = stack.getItem() instanceof BlockItem ? EventType.BLOCKITEM_APPLICABLE_EVENTS : EventType.ITEM_APPLICABLE_EVENTS;
		ReqType[] reqs = stack.getItem() instanceof BlockItem ? ReqType.BLOCKITEM_APPLICABLE_EVENTS : ReqType.ITEM_APPLICABLE_EVENTS;
		populateItems(List.of(stack), events, reqs, ModifierDataType.values(), "", true, true);
	}
	public StatScrollWidget(int width, int height, int top, int left, Entity entity) {
		this(width, height, top, left);
		populateEntity(List.of(entity), EventType.ENTITY_APPLICABLE_EVENTS, ReqType.ENTITY_APPLICABLE_EVENTS, entity instanceof Player, "");
	}
	public StatScrollWidget(int width, int height, int top, int left, BlockPos pos, ItemRenderer itemRenderer) {
		this(width, height, top, left);
		this.itemRenderer = itemRenderer;
		populateBlockFromWorld(pos, EventType.BLOCK_APPLICABLE_EVENTS, ReqType.BLOCK_APPLICABLE_EVENTS);
	}
	public StatScrollWidget(int width, int height, int top, int left, SELECTION selection, OBJECT object, String skill, GuiEnumGroup type, ItemRenderer itemRenderer) {
		this(width, height, top, left);
		this.itemRenderer = itemRenderer;
		generateGlossary(selection, object, skill, type);
	}
	
	//Utility method for uniform nesting indentation
	private int step(int level) {return level * 10;}
	
	public void generateGlossary(SELECTION selection, OBJECT object, String skill, GuiEnumGroup type) {
		switch (selection) {
		case REQS:{
			EventType[] events = new EventType[] {};
			ModifierDataType[] bonuses = new ModifierDataType[] {};
			switch (object) {
			case ITEMS: {
				populateItems(
					ForgeRegistries.ITEMS.getValues().stream()
						.flatMap(item -> {
							NonNullList<ItemStack> list = NonNullList.create();
							item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
							return list.stream();
						})
						.toList(),
					events,
					type == null ? ReqType.ITEM_APPLICABLE_EVENTS : new ReqType[] {(ReqType) type},
					bonuses,
					skill,
					false,
					false);
				break;}
			case BLOCKS: {
				populateBlocks(
					ForgeRegistries.BLOCKS.getValues(),
					events,
					type == null ? ReqType.BLOCK_APPLICABLE_EVENTS : new ReqType[] {(ReqType) type},
					false,
					skill);
				break;}
			case ENTITY: {
				populateEntity(
					ForgeRegistries.ENTITY_TYPES.getValues().stream().map(entityType -> entityType.create(mc.level)).filter(entity -> entity != null).toList(),
					events,
					type == null ? ReqType.ENTITY_APPLICABLE_EVENTS : new ReqType[] {(ReqType) type},
					false,
					skill);
				break;}
			case DIMENSIONS: {
				populateLocation(mc.player.connection.levels().stream().map(key -> key.location()).toList(),
					new ReqType[] {ReqType.TRAVEL}, bonuses, skill, false, false, false);
				break;}
			case BIOMES: {
				populateLocation(ForgeRegistries.BIOMES.getKeys().stream().toList(),
					new ReqType[] {ReqType.TRAVEL}, bonuses, skill, true, false, false);
				break;}
			case ENCHANTS: {
				populateEnchants(ForgeRegistries.ENCHANTMENTS.getValues().stream().map(ench -> RegistryUtil.getId(ench)).toList(), skill);
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
						ForgeRegistries.ITEMS.getValues().stream()
						.flatMap(item -> {
							NonNullList<ItemStack> list = NonNullList.create();
							item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
							return list.stream();
						})
						.toList(),
						type == null ? EventType.ITEM_APPLICABLE_EVENTS : new EventType[] {(EventType) type},
						reqs, bonuses, skill, false, false);
				break;}
			case BLOCKS: {
				populateBlocks(
						ForgeRegistries.BLOCKS.getValues(),
						type == null ? EventType.BLOCK_APPLICABLE_EVENTS : new EventType[] {(EventType) type},
						reqs, false, skill);
				break;}
			case ENTITY: {
				populateEntity(
						ForgeRegistries.ENTITY_TYPES.getValues().stream().map(entityType -> entityType.create(Minecraft.getInstance().level)).filter(entity -> entity != null).toList(),
						type == null ? EventType.ENTITY_APPLICABLE_EVENTS : new EventType[] {(EventType) type},
						reqs, false, skill);
				break;}
			case EFFECTS: {
				populateEffects(
						ForgeRegistries.MOB_EFFECTS.getValues(),
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
						ForgeRegistries.ITEMS.getValues().stream()
						.flatMap(item -> {
							NonNullList<ItemStack> list = NonNullList.create();
							item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
							return list.stream();
						})
						.toList(),
						events, reqs, 
						type == null ? ModifierDataType.values() : new ModifierDataType[] {(ModifierDataType) type}, 
						skill, false, false);
				break;}
			case DIMENSIONS: {
				populateLocation(mc.player.connection.levels().stream().map(key -> key.location()).toList(),
						reqs, type == null ? ModifierDataType.values() : new ModifierDataType[] {(ModifierDataType) type}, skill, false, false, false);
				break;}
			case BIOMES: {
				populateLocation(ForgeRegistries.BIOMES.getKeys().stream().toList(),
						reqs, type == null ? ModifierDataType.values() : new ModifierDataType[] {(ModifierDataType) type}, skill, true, false, false);
				break;}
			default:{}
			}
			break;}
		case SALVAGE: {
			if (object == OBJECT.ITEMS) {
				populateItems(
						ForgeRegistries.ITEMS.getValues().stream().map(item -> new ItemStack(item)).toList(),
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
						ForgeRegistries.ITEMS.getValues().stream()
						.flatMap(item -> {
							NonNullList<ItemStack> list = NonNullList.create();
							item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
							return list.stream();
						})
						.toList(),
						events, reqs, bonuses, skill, false, true);
				break;}
			case BLOCKS: {
				populateBlocks(
						ForgeRegistries.BLOCKS.getValues(),
						events,	reqs, true, skill);
				break;}
			case DIMENSIONS: {
				populateLocation(mc.player.connection.levels().stream().map(key -> key.location()).toList(),
						reqs, bonuses, skill, false, true, false);
				break;}
			case BIOMES: {
				populateLocation(ForgeRegistries.BIOMES.getKeys().stream().toList(),
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
				populateLocation(ForgeRegistries.BIOMES.getKeys().stream().toList(),
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
	
	@SuppressWarnings("deprecation")
	private void populateItems(List<ItemStack> items, EventType[] events, ReqType[] reqs, ModifierDataType[] modifiers, String skillFilter, boolean includeSalvage, boolean includeVein) {
		for (ItemStack stack : items) {
			int lengthBeforeProcessing = content.size() + 1;
			if (items.size() > 1) 
				content.add(new RenderableElement(stack.getDisplayName(), 1, stack.getRarity().color.getColor(), Config.SECTION_HEADER_COLOR.get(), stack));
			addEventSection((event -> {
				Map<String, Long> map = core.getExperienceAwards(event, stack, mc.player, new CompoundTag());
				if (stack.getItem() instanceof BlockItem)
					map = core.getCommonXpAwardData(new HashMap<>(), event, RegistryUtil.getId(stack), mc.player, ObjectType.BLOCK, TagUtils.stackTag(stack));
				return map;
				}), events, skillFilter);
			addReqSection((reqType -> {
				Map<String, Integer> reqMap = core.getReqMap(reqType, stack);
				if (reqType == ReqType.USE_ENCHANTMENT)
					core.getEnchantReqs(stack).forEach((skill, level) -> reqMap.merge(skill, level, (o,n) -> o>n ? o : n));
				if (stack.getItem() instanceof BlockItem)
					reqMap.putAll(core.getCommonReqData(new HashMap<>(), ObjectType.BLOCK, RegistryUtil.getId(stack), reqType, TagUtils.stackTag(stack)));
				return reqMap;
				}),	
				CoreUtils.getEffects(core.getLoader().getLoader(ObjectType.ITEM).getData(RegistryUtil.getId(stack)).getNegativeEffect(), true), 
				reqs, skillFilter);
			addModifierSection((mod -> core.getTooltipRegistry().bonusTooltipExists(RegistryUtil.getId(stack), mod) ?
						core.getTooltipRegistry().getBonusTooltipData(RegistryUtil.getId(stack), mod, stack) :
						core.getObjectModifierMap(ObjectType.ITEM, RegistryUtil.getId(stack), mod, TagUtils.stackTag(stack))
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
	private void populateBlockFromWorld(BlockPos block, EventType[] events, ReqType[] reqs) {
		addEventSection((event -> core.getExperienceAwards(event, block, Minecraft.getInstance().level, null, new CompoundTag())), events, "");
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
			content.add(new RenderableElement(stack.getDisplayName(), 1, stack.getRarity().color.getColor(), Config.SECTION_HEADER_COLOR.get(), block));
			addEventSection((event -> core.getTooltipRegistry().xpGainTooltipExists(id, event)
					? Collections.singletonMap(PREDICATE_KEY, 0l)
					: core.getObjectExperienceMap(ObjectType.BLOCK, id, event, new CompoundTag()))
				, events, skillFilter);
			addReqSection((reqType -> core.getPredicateRegistry().predicateExists(id, reqType)
					? Collections.singletonMap(PREDICATE_KEY, 0)
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
	
	private void populateEffects(Collection<MobEffect> effects, EventType[] events, ReqType[] reqs, String skillFilter) {
		for (MobEffect effect : effects) {
			int lengthBeforeProcessing = content.size() + 1;
			if (effects.size() > 1)
				content.add(new TextElement(effect.getDisplayName(), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
			List<TextElement> holder = new ArrayList<>();
			for (int lvl = 0; lvl <= getEffectHighestConfiguration(effect); lvl++) {
				Map<String, Long> xpMap = core.getExperienceAwards(new MobEffectInstance(effect, 30, lvl), null, new CompoundTag());
				if (!xpMap.isEmpty() && !xpMap.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
					holder.add(new TextElement(Component.literal(String.valueOf(lvl)), 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> map : xpMap.entrySet()) {
						if (map.getValue() == 0) continue;
						holder.add(new TextElement(map.getKey(), map.getValue(), step(1), CoreUtils.getSkillColor(map.getKey())));
					}
				}
			}
			if (holder.size() > 0) {
				content.add(new TextElement(LangProvider.EVENT_HEADER.asComponent().withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
			if (lengthBeforeProcessing == content.size())
				content.remove(content.size()-1);
		}
	}
	
	private int getEffectHighestConfiguration(MobEffect effect) {
		DataSource<?> data = core.getLoader().getLoader(ObjectType.EFFECT).getData().get(RegistryUtil.getId(effect));
		return data == null ? 0 : ((EnhancementsData)data).skillArray().keySet().stream().max(Comparator.naturalOrder()).orElse(-1);
	}
	
	private void populateLocation(List<ResourceLocation> locations, ReqType[] reqs, ModifierDataType[] modifiers, String skillFilter, boolean isBiome, boolean includeVein, boolean includeScaling) {
		locations.forEach(loc -> {
			int lengthBeforeProcessing = content.size() + 1;
			if (locations.size() > 1)
				content.add(new TextElement(Component.literal(loc.toString()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
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
				content.add(new TextElement(Component.literal(ench.toString()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
			List<TextElement> holder = new ArrayList<>();
			for (int i = 0; i <= ForgeRegistries.ENCHANTMENTS.getValue(ench).getMaxLevel(); i++) {
				Map<String, Integer> reqMap = core.getEnchantmentReqs(ench, i).entrySet().stream().filter(entry -> entry.getKey().contains(skillFilter)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));				
				if (!reqMap.isEmpty() && !reqMap.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
					holder.add(new TextElement(Component.literal(String.valueOf(i)), 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Integer> map : reqMap.entrySet()) {
						if (map.getValue() == 0) continue;
						holder.add(new TextElement(map.getKey(), map.getValue(), step(1), CoreUtils.getSkillColor(map.getKey())));
					}
				}
			}
			if (holder.size() > 0) {
				content.add(new TextElement(LangProvider.REQ_HEADER.asComponent().withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
			if (lengthBeforeProcessing == content.size())
				content.remove(content.size()-1);
		});
	}
	private final CompoundTag internalDefaults = TagBuilder.start()
			.withInt(APIUtils.MIN_LEVEL, 0)
			.withInt(APIUtils.MAX_LEVEL, Config.MAX_LEVEL.get()).build();
	private void populatePerks(String skillFilter) {
		for (EventType cause : EventType.values()) {
			List<TextElement> holder = new ArrayList<>();
			PerksConfig.PERK_SETTINGS.get().getOrDefault(cause, new HashMap<>()).forEach((skill, list) -> {
				if (!skill.contains(skillFilter)) 
					return;
				holder.add(new TextElement(Component.translatable("pmmo."+skill).withStyle(ChatFormatting.UNDERLINE),
						step(1), CoreUtils.getSkillStyle(skill).getColor().getValue(), false, 0));
				list.forEach(src -> {
					ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
					holder.add(new TextElement(Component.translatable("pmmo."+perkID.getNamespace()+"."+perkID.getPath()), 
							step(1), 0xFFFFFF, false, 0));
					CompoundTag newsrc = internalDefaults.copy().merge(core.getPerkRegistry().getProperties(perkID).merge(src));
					newsrc.getAllKeys().forEach(key -> {
						if (key.equals("perk")) 
							return;
						if (newsrc.get(key) instanceof ListTag) {
							ListTag innerList = newsrc.getList(key, ((ListTag)newsrc.get(key)).getElementType());
							holder.add(new TextElement(Component.literal(key +" = "), step(2), 0xAAFFFF, false, 0));
							innerList.forEach(tag -> {
								holder.add(new TextElement(Component.literal(tag.getAsString()), step(3), 0x7C83BC, false, 0));
							});
						}
						else 
							holder.add(new TextElement(Component.literal(key +" = " + newsrc.get(key).getAsString()), 
									step(2), 0xAAFFFF, false, 0));
						
					});
				});
			});
			if (holder.size() > 0) {
				content.add(new TextElement(cause, 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
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
					holder.add(new TextElement(LangProvider.ADDON_AFFECTED_ATTRIBUTE.asComponent(), 5, 0xFF9C03, false, 0x000000));
				else if (!xpAwards.isEmpty()) {
					holder.add(new TextElement(event, 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> map : xpAwards.entrySet()) {
						holder.add(new TextElement(map.getKey(), map.getValue(), 5, CoreUtils.getSkillColor(map.getKey())));
					}
				}
			}
			if (holder.size() > 0) {
				content.add(new TextElement(LangProvider.EVENT_HEADER.asComponent().withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
		}
	}
	
	private void addReqSection(Function<ReqType, Map<String, Integer>> reqSrc, List<MobEffectInstance> reqEffects, ReqType[] reqs, String skillFilter) {
		if (reqs.length > 0) {
			List<TextElement> holder = new ArrayList<>();
			for (ReqType reqType: reqs) {
				Map<String, Integer> reqMap = CoreUtils.processSkillGroupReqs(reqSrc.apply(reqType)).entrySet().stream().filter(entry -> entry.getKey().contains(skillFilter)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				if (!reqMap.isEmpty() && !reqMap.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
					holder.add(new TextElement(reqType, 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Integer> map : reqMap.entrySet()) {
						if (map.getValue() == 0) continue;
						holder.add(new TextElement(map.getKey(), map.getValue(), step(1), CoreUtils.getSkillColor(map.getKey())));
					}
				}
			}
			if (holder.size() > 0) {
				content.add(new TextElement(LangProvider.REQ_HEADER.asComponent().withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
				addReqEffectSection(reqEffects, true);
			}
		}
	}
	
	private void addReqEffectSection(List<MobEffectInstance> reqEffects, boolean isNegative) {
		if (reqEffects.size() > 0) {
			content.add(new TextElement(isNegative ? LangProvider.REQ_EFFECTS_HEADER.asComponent() : LangProvider.BIOME_EFFECT_POS.asComponent(), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			for (MobEffectInstance mei : reqEffects) {
				content.add(new TextElement(mei.getEffect().getDisplayName(), step(1), 0xFFFFFF, false, 0));
			}
		}
	}
	
	private void addModifierSection(Function<ModifierDataType, Map<String, Double>> bonusSrc, ModifierDataType[] mods, String skillFilter) {
		if (mods.length > 0) {
			List<TextElement> holder = new ArrayList<>();
			for (ModifierDataType mod : mods) {
				Map<String, Double> modifiers = bonusSrc.apply(mod).entrySet().stream().filter(entry -> entry.getKey().contains(skillFilter)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				if (!modifiers.isEmpty()) {
					content.add(new TextElement(mod, 1, 0xFFFFFF, false, 0));
					modifiers.forEach((key, value) 
							-> content.add(new TextElement(key, value, step(1), CoreUtils.getSkillColor(key))));
				}
			}
			if (holder.size() > 0) {
				content.add(new TextElement(LangProvider.MODIFIER_HEADER.asComponent().withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
				content.addAll(holder);
			}
		}
	}
	
	private void addSalvageSection(Map<ResourceLocation, SalvageData> salvage) {
		if (!salvage.isEmpty()) {
			content.add(new TextElement(LangProvider.SALVAGE_HEADER.asComponent().withStyle(ChatFormatting.BOLD), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			for (Map.Entry<ResourceLocation, SalvageData> salvageEntry : salvage.entrySet()) {
				SalvageData data = salvageEntry.getValue();
				ItemStack resultStack = new ItemStack(ForgeRegistries.ITEMS.getValue(salvageEntry.getKey()));
				content.add(new TextElement(resultStack.getDisplayName(), step(1), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
				if (!data.levelReq().isEmpty()) {
					content.add(new TextElement(LangProvider.SALVAGE_LEVEL_REQ.asComponent().withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
					for (Map.Entry<String, Integer> req : data.levelReq().entrySet()) {
						content.add(new TextElement(req.getKey(), req.getValue(), step(2), CoreUtils.getSkillColor(req.getKey())));
					}
				}
				content.add(new TextElement(LangProvider.SALVAGE_CHANCE.asComponent(data.baseChance(), data.maxChance()).withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
				content.add(new TextElement(LangProvider.SALVAGE_MAX.asComponent(data.salvageMax()).withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
				if (!data.chancePerLevel().isEmpty()) {
					content.add(new TextElement(LangProvider.SALVAGE_CHANCE_MOD.asComponent().withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
					for (Map.Entry<String, Double> perLevel : data.chancePerLevel().entrySet()) {
						content.add(new TextElement(perLevel.getKey(), perLevel.getValue(), step(2), CoreUtils.getSkillColor(perLevel.getKey())));
					}
				}
				if (!data.xpAward().isEmpty()) {
					content.add(new TextElement(LangProvider.SALVAGE_XP_AWARD.asComponent().withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
					for (Map.Entry<String, Long> award : data.xpAward().entrySet()) {
						content.add(new TextElement(award.getKey(), award.getValue(), step(2), CoreUtils.getSkillColor(award.getKey())));
					}
				}
			}
		}
	}
	
	private void addItemVeinSection(VeinData veinData, boolean isBlockItem) {
		if (!veinData.equals(VeinData.EMPTY)) {
			content.add(new TextElement(LangProvider.VEIN_HEADER.asComponent().withStyle(ChatFormatting.BOLD), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			content.add(new TextElement(LangProvider.VEIN_RATE.asComponent(veinData.chargeRate.orElse(0d) * 2d), step(1), 0xFFFFFF, false, 0));
			content.add(new TextElement(LangProvider.VEIN_CAP.asComponent(veinData.chargeCap.orElse(0)), step(1), 0xFFFFFF, false, 0));
			if (isBlockItem)
				content.add(new TextElement(LangProvider.VEIN_CONSUME.asComponent(veinData.consumeAmount.orElse(0)), step(1), 0xFFFFFF, false, 0));
		}
	}
	
	private void addBlockVeinSection(VeinData veinData) {
		if (veinData.consumeAmount != VeinData.EMPTY.consumeAmount) {
			content.add(new TextElement(LangProvider.VEIN_HEADER.asComponent().withStyle(ChatFormatting.BOLD), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			content.add(new TextElement(LangProvider.VEIN_CONSUME.asComponent(veinData.consumeAmount.orElse(0)), step(1), 0xFFFFFF, false, 0));
		}
	}
	
	private void addPlayerSection(Entity entity) {
		//Section for player-specific data as it expands
		content.add(new TextElement(LangProvider.PLAYER_HEADER.asComponent(), step(1), 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
		PlayerData data = core.getLoader().PLAYER_LOADER.getData(new ResourceLocation(entity.getUUID().toString()));
		content.add(new TextElement(LangProvider.PLAYER_IGNORE_REQ.asComponent(data.ignoreReq()), step(2), 0xFFFFFF, false, 0));
		if (!data.bonuses().isEmpty()) {
			content.add(new TextElement(LangProvider.PLAYER_BONUSES.asComponent(), step(2), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
			for (Map.Entry<String, Double> bonus : data.bonuses().entrySet()) {
				content.add(new TextElement(bonus.getKey(), bonus.getValue(), step(3), CoreUtils.getSkillColor(bonus.getKey())));
			}
		}
		
		//Section for skills
		Map<String, Long> rawXp = core.getData().getXpMap(entity.getUUID());
		LinkedHashMap<String, Integer> orderedMap = new LinkedHashMap<>();
		List<String> skillKeys = new ArrayList<>(rawXp.keySet().stream().toList());
		skillKeys.sort(Comparator.<String>comparingLong(a -> rawXp.get(a)).reversed());
		skillKeys.forEach(skill -> {
			orderedMap.put(skill, core.getData().getLevelFromXP(rawXp.get(skill)));
		});
		content.add(new TextElement(LangProvider.SKILL_LIST_HEADER.asComponent(), step(1), 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
		for (Map.Entry<String, Integer> rawMap : orderedMap.entrySet()) {
			content.add(new TextElement(rawMap.getKey(), rawMap.getValue(), step(2), CoreUtils.getSkillColor(rawMap.getKey())));
		}
	}
	
	private void addVeinBlacklistSection(ObjectType type, ResourceLocation location) {
		LocationData loader = (LocationData) core.getLoader().getLoader(type).getData(location);
		if (!loader.veinBlacklist().isEmpty()) {
			content.add(new TextElement(LangProvider.VEIN_BLACKLIST_HEADER.asComponent().withStyle(ChatFormatting.BOLD), step(1), 0xFFFFFF, false, 0));
			for (ResourceLocation blockID : loader.veinBlacklist()) {
				content.add(new TextElement(Component.literal(blockID.toString()), step(2), 0xEEEEEE, false, 0));
			}
		}
	}
	
	private void addMobModifierSection(ObjectType type, ResourceLocation location) {
		if (type != ObjectType.BIOME && type != ObjectType.DIMENSION) 
			return;
		LocationData loader = (LocationData) core.getLoader().getLoader(type).getData(location);
		if (!loader.mobModifiers().isEmpty()) {
			content.add(new TextElement(LangProvider.MOB_MODIFIER_HEADER.asComponent().withStyle(ChatFormatting.BOLD), step(1), 0xFFFFFF, false, 0));
			for (Map.Entry<ResourceLocation, Map<String, Double>> mobMap : loader.mobModifiers().entrySet()) {
				Entity entity = ForgeRegistries.ENTITY_TYPES.getValue(mobMap.getKey()).create(mc.level);
				content.add(new RenderableElement(entity.getName(), step(1), 0xFFFFFF, Config.SALVAGE_ITEM_COLOR.get(), entity));
				for (Map.Entry<String, Double> map : mobMap.getValue().entrySet()) {
					Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(map.getKey()));
					MutableComponent text = attribute == null ? Component.literal(map.getKey()) : Component.translatable(attribute.getDescriptionId());
					text.append(Component.literal(": "+map.getValue()));
					content.add(new TextElement(text, step(2), 0xFFFFFF, false, 0xFFFFFF));
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
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		return super.mouseScrolled(mouseX, mouseY, scroll);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int partialTicks) {
		return super.mouseClicked(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawPanel(PoseStack poseStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
		for (int i = 0; i < content.size(); i++) {
			content.get(i).render(poseStack, this.left, (int)(relativeY + (i*12) - scrollDistance), this.width, this.itemRenderer);			
		}
	}


}
