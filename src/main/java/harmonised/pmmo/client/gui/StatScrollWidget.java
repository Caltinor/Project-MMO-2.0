package harmonised.pmmo.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.CodecTypes.SalvageData;
import harmonised.pmmo.config.readers.ModifierDataType;
import harmonised.pmmo.core.Core;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ScrollPanel;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

public class StatScrollWidget extends ScrollPanel{
	private static record Element(Component text, int xOffset, int color, boolean isHeader, int headerColor) {
		public Element(String key, int value, int xOffset, int color) {
			this(new TranslatableComponent("pmmo."+key).append(new TextComponent(": "+value)), xOffset, color, false, 0);
		}
		public Element(String key, long value, int xOffset, int color) {
			this(new TranslatableComponent("pmmo."+key).append(new TextComponent(": "+value)), xOffset, color, false, 0);
		}
		public Element(String key, double value, int xOffset, int color) {
			this(new TranslatableComponent("pmmo."+key).append(new TextComponent(": "+DP.dp(value*100)+"%")), xOffset, color, false, 0);
		}
		public Element(Enum<?> type, int xOffset, int color, boolean isHeader, int headerColor) {
			this(new TranslatableComponent("pmmo.enum."+type.name()), xOffset, color, isHeader, headerColor);
		}
	}
	
	private ItemStack stack;
	private BlockPos blockPos;
	private Entity entity;
	private final List<Element> content = new ArrayList<>();

	public StatScrollWidget(int width, int height, int top, int left) {
		super(Minecraft.getInstance(), width, height, top, left, 4); 
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
	
	private void generateContent() {
		Core core = Core.get(LogicalSide.CLIENT);
		
		content.add(new Element(new TranslatableComponent("pmmo.event_header").withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
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
		
		content.add(new Element(new TranslatableComponent("pmmo.req_header").withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
		for (ReqType reqType : ReqType.values()) {
			if (!Config.reqEnabled(reqType).get()) continue;
			Map<String, Integer> reqMap = 
					entity != null ? core.getReqMap(reqType, entity) :
					blockPos != null ? core.getReqMap(reqType, blockPos, Minecraft.getInstance().level) :
					stack != null ? core.getReqMap(reqType, stack) : 
						new HashMap<>();
			if (!reqMap.isEmpty()) {
				content.add(new Element(reqType, 1, 0xFFFFFF, false, 0));
				for (Map.Entry<String, Integer> map : reqMap.entrySet()) {
					content.add(new Element(map.getKey(), map.getValue(), step(1), core.getDataConfig().getSkillColor(map.getKey())));
				}
			}
		}
		
		
		if (stack != null) {
			List<MobEffectInstance> reqEffects = core.getDataConfig().getItemEffect(stack.getItem().getRegistryName());
			if (reqEffects.size() > 0) {
				content.add(new Element(new TranslatableComponent("pmmo.req_effects_header"), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
				for (MobEffectInstance mei : reqEffects) {
					content.add(new Element(mei.getEffect().getDisplayName(), step(1), 0xFFFFFF, false, 0));
				}
			}
			
			content.add(new Element(new TranslatableComponent("pmmo.modifier_header").withStyle(ChatFormatting.BOLD), 1, 0xEEEEEE, true, Config.SECTION_HEADER_COLOR.get()));
			for (ModifierDataType mod : ModifierDataType.values()) {
				Map<String, Double> modifiers = core.getTooltipRegistry().bonusTooltipExists(stack.getItem().getRegistryName(), mod) ?
						core.getTooltipRegistry().getBonusTooltipData(stack.getItem().getRegistryName(), mod, stack) :
						core.getXpUtils().getObjectModifierMap(mod, stack.getItem().getRegistryName());
				if (!modifiers.isEmpty()) {
					content.add(new Element(mod, 1, 0xFFFFFF, false, 0));
					for (Map.Entry<String, Double> map : modifiers.entrySet()) {
						content.add(new Element(map.getKey(), map.getValue(), step(1), core.getDataConfig().getSkillColor(map.getKey())));
					}
				}
			}
			
			Map<ResourceLocation, SalvageData> salvage = core.getSalvageLogic().getSalvageData(stack.getItem().getRegistryName());
			if (!salvage.isEmpty()) {
				content.add(new Element(new TranslatableComponent("pmmo.salvage_header").withStyle(ChatFormatting.BOLD), 1, 0xFFFFFF, true, Config.SECTION_HEADER_COLOR.get()));
				for (Map.Entry<ResourceLocation, SalvageData> salvageEntry : salvage.entrySet()) {
					SalvageData data = salvageEntry.getValue();
					ItemStack resultStack = new ItemStack(ForgeRegistries.ITEMS.getValue(salvageEntry.getKey()));
					content.add(new Element(resultStack.getDisplayName(), step(1), 0xFFFFFF, true, Config.SALVAGE_ITEM_COLOR.get()));
					if (!data.levelReq().isEmpty()) {
						content.add(new Element(new TranslatableComponent("pmmo.salvage_levelreq").withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
						for (Map.Entry<String, Integer> req : data.levelReq().entrySet()) {
							content.add(new Element(req.getKey(), req.getValue(), step(2), core.getDataConfig().getSkillColor(req.getKey())));
						}
					}
					content.add(new Element(new TranslatableComponent("pmmo.salvage_chance", data.baseChance(), data.maxChance()).withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
					content.add(new Element(new TranslatableComponent("pmmo.salvage_max", data.salvageMax()).withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
					if (!data.chancePerLevel().isEmpty()) {
						content.add(new Element(new TranslatableComponent("pmmo.salvage_chance_modifier").withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
						for (Map.Entry<String, Double> perLevel : data.chancePerLevel().entrySet()) {
							content.add(new Element(perLevel.getKey(), perLevel.getValue(), step(2), core.getDataConfig().getSkillColor(perLevel.getKey())));
						}
					}
					if (!data.xpAward().isEmpty()) {
						content.add(new Element(new TranslatableComponent("pmmo.salvage_xpAward_header").withStyle(ChatFormatting.UNDERLINE), step(1), 0xFFFFFF, false, 0));
						for (Map.Entry<String, Long> award : data.xpAward().entrySet()) {
							content.add(new Element(award.getKey(), award.getValue(), step(2), core.getDataConfig().getSkillColor(award.getKey())));
						}
					}
				}
			}
			
			//TODO Vein Data
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
