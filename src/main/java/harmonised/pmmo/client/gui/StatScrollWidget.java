package harmonised.pmmo.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.CodecMapPlayer.PlayerData;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

public class StatScrollWidget extends ScrollPanel{
	private static record Element(Component text, int xOffset, int color, boolean isHeader, int headerColor) {
		public Element(String key, int value, int xOffset, int color) {
			this(Component.translatable("pmmo."+key).append(Component.literal(": "+value)), xOffset, color, false, 0);
		}
		public Element(String key, long value, int xOffset, int color) {
			this(Component.translatable("pmmo."+key).append(Component.literal(": "+value)), xOffset, color, false, 0);
		}
		public Element(String key, double value, int xOffset, int color) {
			this(Component.translatable("pmmo."+key).append(Component.literal(": "+DP.dp(value*100)+"%")), xOffset, color, false, 0);
		}
		public Element(Enum<?> type, int xOffset, int color, boolean isHeader, int headerColor) {
			this(Component.translatable("pmmo.enum."+type.name()), xOffset, color, isHeader, headerColor);
		}
	}
	
	private ItemStack stack = null;
	private BlockPos blockPos = null;
	private Entity entity = null;
	private final List<Element> content = new ArrayList<>();

	private StatScrollWidget(int width, int height, int top, int left) {
		super(Minecraft.getInstance(), width, height, top, left, 4);
	}
	public StatScrollWidget(int width, int height, int top, int left, int pointless) {
		this(width, height, top, left);
		generateGlossary();
	}
	public StatScrollWidget(int width, int height, int top, int left, ItemStack stack) {
		this(width, height, top, left);
		this.stack = stack;
		generateContent();
	}
	public StatScrollWidget(int width, int height, int top, int left, Entity entity) {
		this(width, height, top, left);
		this.entity = entity;
		generateContent();
	}
	public StatScrollWidget(int width, int height, int top, int left, BlockPos pos) {
		this(width, height, top, left);
		this.blockPos = pos;
		generateContent();
	}
	
	//Utility method for uniform nesting indentation
	private int step(int level) {return level * 10;}
	
	@SuppressWarnings("deprecation")
	private void generateContent() {
		Core core = Core.get(LogicalSide.CLIENT);
		
		content.add(new Element(Component.translatable("pmmo.event_header").withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
		for (EventType event : EventType.values()) {
			Map<String, Long> xpAwards = 
					entity != null ? core.getExperienceAwards(event, entity, null, new CompoundTag()) :
					blockPos != null ? core.getBlockExperienceAwards(event, blockPos, Minecraft.getInstance().level, null, new CompoundTag()) :
					stack != null ? core.getExperienceAwards(event, stack, null, new CompoundTag()) : 
						new HashMap<>();
			if (!xpAwards.isEmpty()) {
				content.add(new Element(event, 1, 0xFFFFFF, false, 0));
				for (Map.Entry<String, Long> map : xpAwards.entrySet()) {
					content.add(new Element(map.getKey(), map.getValue(), 5, core.getDataConfig().getSkillColor(map.getKey())));
				}
			}
		}
		
		content.add(new Element(Component.translatable("pmmo.req_header").withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
		for (ReqType reqType : ReqType.values()) {
			if (!Config.reqEnabled(reqType).get()) continue;
			Map<String, Integer> reqMap = 
					entity != null ? core.getReqMap(reqType, entity) :
					blockPos != null ? core.getReqMap(reqType, blockPos, Minecraft.getInstance().level) :
					stack != null ? core.getReqMap(reqType, stack) : 
						new HashMap<>();
			if (!reqMap.isEmpty() && !reqMap.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
				content.add(new Element(reqType, 1, 0xFFFFFF, false, 0));
				for (Map.Entry<String, Integer> map : reqMap.entrySet()) {
					if (map.getValue() == 0) continue;
					content.add(new Element(map.getKey(), map.getValue(), step(1), core.getDataConfig().getSkillColor(map.getKey())));
				}
			}
		}
		
		
		if (stack != null) {
			List<MobEffectInstance> reqEffects = core.getDataConfig().getItemEffect(stack.getItem().builtInRegistryHolder().unwrapKey().get().location());
			if (reqEffects.size() > 0) {
				content.add(new Element(Component.translatable("pmmo.req_effects_header"), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
				for (MobEffectInstance mei : reqEffects) {
					content.add(new Element(mei.getEffect().getDisplayName(), step(1), 0xFFFFFF, false, 0));
				}
			}
			
			content.add(new Element(Component.translatable("pmmo.modifier_header").withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
			for (ModifierDataType mod : ModifierDataType.values()) {
				Map<String, Double> modifiers = core.getTooltipRegistry().bonusTooltipExists(stack.getItem().builtInRegistryHolder().unwrapKey().get().location(), mod) ?
						core.getTooltipRegistry().getBonusTooltipData(stack.getItem().builtInRegistryHolder().unwrapKey().get().location(), mod, stack) :
						core.getXpUtils().getObjectModifierMap(mod, stack.getItem().builtInRegistryHolder().unwrapKey().get().location());
				if (!modifiers.isEmpty()) {
					content.add(new Element(mod, 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Double> map : modifiers.entrySet()) {
						content.add(new Element(map.getKey(), map.getValue(), step(1), core.getDataConfig().getSkillColor(map.getKey())));
					}
				}
			}
			
			Map<ResourceLocation, SalvageData> salvage = core.getSalvageLogic().getSalvageData(stack.getItem().builtInRegistryHolder().unwrapKey().get().location());
			if (!salvage.isEmpty()) {
				content.add(new Element(Component.translatable("pmmo.salvage_header").withStyle(ChatFormatting.BOLD), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
				for (Map.Entry<ResourceLocation, SalvageData> salvageEntry : salvage.entrySet()) {
					SalvageData data = salvageEntry.getValue();
					ItemStack resultStack = new ItemStack(ForgeRegistries.ITEMS.getValue(salvageEntry.getKey()));
					content.add(new Element(resultStack.getDisplayName(), step(1), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
					if (!data.levelReq().isEmpty()) {
						content.add(new Element(Component.translatable("pmmo.salvage_levelreq").withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
						for (Map.Entry<String, Integer> req : data.levelReq().entrySet()) {
							content.add(new Element(req.getKey(), req.getValue(), step(2), core.getDataConfig().getSkillColor(req.getKey())));
						}
					}
					content.add(new Element(Component.translatable("pmmo.salvage_chance", data.baseChance(), data.maxChance()).withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
					content.add(new Element(Component.translatable("pmmo.salvage_max", data.salvageMax()).withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
					if (!data.chancePerLevel().isEmpty()) {
						content.add(new Element(Component.translatable("pmmo.salvage_chance_modifier").withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
						for (Map.Entry<String, Double> perLevel : data.chancePerLevel().entrySet()) {
							content.add(new Element(perLevel.getKey(), perLevel.getValue(), step(2), core.getDataConfig().getSkillColor(perLevel.getKey())));
						}
					}
					if (!data.xpAward().isEmpty()) {
						content.add(new Element(Component.translatable("pmmo.salvage_xpAward_header").withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
						for (Map.Entry<String, Long> award : data.xpAward().entrySet()) {
							content.add(new Element(award.getKey(), award.getValue(), step(2), core.getDataConfig().getSkillColor(award.getKey())));
						}
					}
				}
			}
			
			VeinData veinData = core.getVeinData().getData(stack);
			if (!veinData.equals(VeinData.EMPTY)) {
				content.add(new Element(Component.translatable("pmmo.vein_header").withStyle(ChatFormatting.BOLD), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
				content.add(new Element(Component.translatable("pmmo.veindata_rate", veinData.chargeRate().orElse(0d)), step(1), 0xFFFFFF, false, 0));
				content.add(new Element(Component.translatable("pmmo.veindata_cap", veinData.chargeCap().orElse(0)), step(1), 0xFFFFFF, false, 0));
				if (stack.getItem() instanceof BlockItem)
					content.add(new Element(Component.translatable("pmmo.veindata_consume", veinData.consumeAmount().get()), step(1), 0xFFFFFF, false, 0));
			}
		}
		
		if (blockPos != null) {
			@SuppressWarnings("resource")
			VeinData veinData = core.getVeinData().getData(new ItemStack(Minecraft.getInstance().level.getBlockState(blockPos).getBlock().asItem()));
			if (veinData.consumeAmount() != VeinData.EMPTY.consumeAmount()) {
				content.add(new Element(Component.translatable("pmmo.vein_header").withStyle(ChatFormatting.BOLD), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
				content.add(new Element(Component.translatable("pmmo.veindata_consume", veinData.consumeAmount().get()), step(1), 0xFFFFFF, false, 0));
			}
		}
		
		if (entity != null && entity.getType().equals(EntityType.PLAYER)) {
			//Section for player-specific data as it expands
			content.add(new Element(Component.translatable("pmmo.playerspecific_header"), step(1), 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			PlayerData data = core.getDataConfig().getPlayerData(entity.getUUID());
			content.add(new Element(Component.translatable("pmmo.playerspecific.ignorereq", data.ignoreReq()), step(2), 0xFFFFFF, false, 0));
			if (!data.bonus().isEmpty()) {
				content.add(new Element(Component.translatable("pmmo.playerspecific.bonus"), step(2), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
				for (Map.Entry<String, Double> bonus : data.bonus().entrySet()) {
					content.add(new Element(bonus.getKey(), bonus.getValue(), step(3), core.getDataConfig().getSkillColor(bonus.getKey())));
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
			content.add(new Element(Component.translatable("pmmo.skilllist_header"), step(1), 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
			for (Map.Entry<String, Integer> rawMap : orderedMap.entrySet()) {
				content.add(new Element(rawMap.getKey(), rawMap.getValue(), step(2), core.getDataConfig().getSkillColor(rawMap.getKey())));
			}
		}
	}
	
	public void generateGlossary() {
		Minecraft mc = Minecraft.getInstance();
		Core core = Core.get(LogicalSide.CLIENT);
		ResourceLocation dimension = mc.player.level.dimension().location();
		ResourceLocation biome = mc.player.level.getBiome(mc.player.blockPosition()).unwrapKey().get().location();
		//DIMENSION DATA
		content.add(new Element(Component.translatable("pmmo.dimension_header", dimension).withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
		Map<String, Integer> travelReqs = core.getSkillGates().getObjectSkillMap(ReqType.TRAVEL, dimension);
		if (!travelReqs.isEmpty() && !travelReqs.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
			content.add(new Element(ReqType.TRAVEL, step(1), 0xFFFFFF, false, 0));
			for (Map.Entry<String, Integer> travelReq : travelReqs.entrySet()) {
				content.add(new Element(travelReq.getKey(), travelReq.getValue(), step(2), core.getDataConfig().getSkillColor(travelReq.getKey())));
			}
		}
		if (core.getXpUtils().hasModifierObjectEntry(ModifierDataType.DIMENSION, dimension)) {
			content.add(new Element(ModifierDataType.DIMENSION, step(1), 0xFFFFFF, false, 0));
			for (Map.Entry<String, Double> bonus : core.getXpUtils().getObjectModifierMap(ModifierDataType.DIMENSION, dimension).entrySet()) {
				content.add(new Element(bonus.getKey(), bonus.getValue(), step(2), core.getDataConfig().getSkillColor(bonus.getKey())));
			}
		}
		if (!core.getDataConfig().getVeinBlacklist(dimension).isEmpty()) {
			content.add(new Element(Component.translatable("pmmo.vein_blacklist_header").withStyle(ChatFormatting.BOLD), step(1), 0xFFFFFF, false, 0));
			for (ResourceLocation blockID : core.getDataConfig().getVeinBlacklist(dimension)) {
				content.add(new Element(Component.literal(blockID.toString()), step(2), 0xEEEEEE, false, 0));
			}
		}
		if (!core.getDataConfig().getMobModifierMap(dimension).isEmpty()) {
			content.add(new Element(Component.translatable("pmmo.mob_modifier_header").withStyle(ChatFormatting.BOLD), step(1), 0xFFFFFF, false, 0));
			for (Map.Entry<ResourceLocation, Map<String, Double>> mobMap : core.getDataConfig().getMobModifierMap(dimension).entrySet()) {
				content.add(new Element(Component.literal(mobMap.getKey().toString()), step(1), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
				for (Map.Entry<String, Double> map : mobMap.getValue().entrySet()) {
					content.add(new Element(map.getKey(), map.getValue(), step(2), 0xFFFFFF));
				}
			}
		}
		//BIOME DATA
		content.add(new Element(Component.translatable("pmmo.biome_header", biome).withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
		travelReqs = core.getSkillGates().getObjectSkillMap(ReqType.TRAVEL, biome);
		if (!travelReqs.isEmpty() && !travelReqs.entrySet().stream().allMatch(entry -> entry.getValue() == 0)) {
			content.add(new Element(ReqType.TRAVEL, step(1), 0xFFFFFF, false, 0));			
			for (Map.Entry<String, Integer> travelReq : travelReqs.entrySet()) {
				if (travelReq.getValue() == 0) continue;
				content.add(new Element(travelReq.getKey(), travelReq.getValue(), step(2), core.getDataConfig().getSkillColor(travelReq.getKey())));
			}
			//negative effects only matter if there is a req to meet.  positive effects will apply always if no req is present
			if (!core.getDataConfig().getLocationEffect(false, biome).isEmpty()) {
				content.add(new Element(Component.translatable("pmmo.biome_negative").withStyle(ChatFormatting.BOLD), step(1), 0xEEEEEE, false, 0));
				for (MobEffectInstance mei : core.getDataConfig().getLocationEffect(false, biome)) {
					content.add(new Element(
							Component.literal("").append(mei.getEffect().getDisplayName()).append(": "+(mei.getAmplifier()+1))
							, step(2), 0xEEEEEE, false, 0));
				}
			}
		}
		if (!core.getDataConfig().getLocationEffect(true, biome).isEmpty()) {
			content.add(new Element(Component.translatable("pmmo.biome_positive").withStyle(ChatFormatting.BOLD), step(1), 0xEEEEEE, false, 0));
			for (MobEffectInstance mei : core.getDataConfig().getLocationEffect(true, biome)) {
				content.add(new Element(
						Component.literal("").append(mei.getEffect().getDisplayName()).append(": "+(mei.getAmplifier()+1))
						, step(2), 0xEEEEEE, false, 0));
			}
		}
		if (core.getXpUtils().hasModifierObjectEntry(ModifierDataType.BIOME, biome)) {
			content.add(new Element(ModifierDataType.BIOME, step(1), 0xFFFFFF, false, 0));
			for (Map.Entry<String, Double> bonus : core.getXpUtils().getObjectModifierMap(ModifierDataType.BIOME, biome).entrySet()) {
				content.add(new Element(bonus.getKey(), bonus.getValue(), step(2), core.getDataConfig().getSkillColor(bonus.getKey())));
			}
		}
		if (!core.getDataConfig().getVeinBlacklist(biome).isEmpty()) {
			content.add(new Element(Component.translatable("pmmo.vein_blacklist_header").withStyle(ChatFormatting.BOLD), step(1), 0xFFFFFF, false, 0));
			for (ResourceLocation blockID : core.getDataConfig().getVeinBlacklist(biome)) {
				content.add(new Element(Component.literal(blockID.toString()), step(2), 0xEEEEEE, false, 0));
			}
		}
		if (!core.getDataConfig().getMobModifierMap(biome).isEmpty()) {
			content.add(new Element(Component.translatable("pmmo.mob_modifier_header").withStyle(ChatFormatting.BOLD), step(1), 0xFFFFFF, false, 0));
			for (Map.Entry<ResourceLocation, Map<String, Double>> mobMap : core.getDataConfig().getMobModifierMap(biome).entrySet()) {
				content.add(new Element(Component.literal(mobMap.getKey().toString()), step(1), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
				for (Map.Entry<String, Double> map : mobMap.getValue().entrySet()) {
					content.add(new Element(map.getKey(), map.getValue(), step(2), 0xFFFFFF));
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

	@SuppressWarnings("resource")
	@Override
	protected void drawPanel(PoseStack poseStack, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
		for (int i = 0; i < content.size(); i++) {
			Element element = content.get(i);
			int y = (int)(relativeY + (i*12) - scrollDistance);
			if (element.isHeader()) 
				GuiComponent.fill(poseStack, this.left, (int) y, this.left+this.width, y+12, element.headerColor());
			GuiComponent.drawString(poseStack, Minecraft.getInstance().font, element.text(), this.left + element.xOffset(), y, element.color);
		}
	}


}
