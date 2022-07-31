package harmonised.pmmo.client.events;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.gui.StatsScreen;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.features.veinmining.VeinDataManager.VeinData;
import harmonised.pmmo.setup.ClientSetup;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.setup.datagen.LangProvider.Translation;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class TooltipHandler {
	public static boolean tooltipOn = true;

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		if(!tooltipOn)
            return;

        Player player = event.getEntity();

        if(player != null) {
        	Core core = Core.get(LogicalSide.CLIENT);
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();
            @SuppressWarnings("deprecation")
			ResourceLocation itemID = item.builtInRegistryHolder().unwrapKey().get().location();

            if(itemID == null)
                return;

            if(ClientSetup.OPEN_MENU.isDown()) {
                Minecraft.getInstance().setScreen(new StatsScreen(stack));
                return;
            }
            //TODO make this generate via loop
            Map<String, Integer> wearReq = getReqData(core, ReqType.WEAR, stack);
            Map<String, Integer> toolReq = getReqData(core, ReqType.TOOL, stack);
            Map<String, Integer> weaponReq = getReqData(core, ReqType.WEAPON, stack);
            Map<String, Integer> useReq = getReqData(core, ReqType.USE, stack);
            //Map<String, Integer> useEnchantmentReq = XP.getEnchantsUseReq(stack);
            Map<String, Integer> placeReq = getReqData(core, ReqType.PLACE, stack);
            Map<String, Integer> breakReq = getReqData(core, ReqType.BREAK, stack);
            Map<String, Long> xpValueBreaking = getXpGainData(core, itemID,EventType.BLOCK_BREAK, stack);
            Map<String, Long> xpValueCrafting = getXpGainData(core, itemID,EventType.CRAFT, stack);
            Map<String, Long> xpValueSmelting = getXpGainData(core, itemID,EventType.SMELT, stack);
            Map<String, Long> xpValueBrewing = getXpGainData(core, itemID,EventType.BREW, stack);
            Map<String, Long> xpValueGrowing = getXpGainData(core, itemID ,EventType.GROW, stack);
            Map<String, Long> xpValuePlacing = getXpGainData(core, itemID,EventType.BLOCK_PLACE, stack);
            //Map<String, Map<String, Double>> salvageInfo = JsonConfig.data2.get(JType.SALVAGE).getOrDefault(regKey, new HashMap<>());
            //Map<String, Map<String, Double>> salvageFrom = JsonConfig.data2.get(JType.SALVAGE_FROM).getOrDefault(regKey, new HashMap<>());
            Map<String, Double> heldItemXpBoost = getBonusData(core, itemID, ModifierDataType.HELD, stack);
            Map<String, Double> wornItemXpBoost = getBonusData(core, itemID, ModifierDataType.WORN, stack);
            //============VEIN MINER TOOLTIP DATA COLLECTION ========================
            VeinData veinData = VeinData.EMPTY;
            if (core.getVeinData().hasData(stack)) {
            	veinData = core.getVeinData().getData(stack);
            }
            
            //=====================REQUIREMENTS=========================
            if (wearReq.size() > 0 && Config.tooltipReqEnabled(ReqType.WEAR).get())	 {addRequirementTooltip(LangProvider.REQ_WEAR, event, wearReq, core);}
            if (toolReq.size() > 0 && Config.tooltipReqEnabled(ReqType.TOOL).get())  {addRequirementTooltip(LangProvider.REQ_TOOL, event, toolReq, core);}
            if (weaponReq.size() > 0 && Config.tooltipReqEnabled(ReqType.WEAPON).get()){addRequirementTooltip(LangProvider.REQ_WEAPON, event, weaponReq, core);}
            if (useReq.size() > 0 && Config.tooltipReqEnabled(ReqType.USE).get())   {addRequirementTooltip(LangProvider.REQ_USE, event, useReq, core);}
            if (placeReq.size() > 0 && Config.tooltipReqEnabled(ReqType.PLACE).get()) {addRequirementTooltip(LangProvider.REQ_PLACE, event, placeReq, core);}
            if (breakReq.size() > 0 && Config.tooltipReqEnabled(ReqType.BREAK).get()) {addRequirementTooltip(LangProvider.REQ_BREAK, event, breakReq, core);}
            //=====================XP VALUES============================
            if (xpValueBreaking.size() > 0 && Config.tooltipXpEnabled(EventType.BLOCK_BREAK).get()){addXpValueTooltip(LangProvider.XP_VALUE_BREAK, event, xpValueBreaking, core);}
            if (xpValueCrafting.size() > 0 && Config.tooltipXpEnabled(EventType.CRAFT).get()){addXpValueTooltip(LangProvider.XP_VALUE_CRAFT, event, xpValueCrafting, core);}
            if (xpValueSmelting.size() > 0 && Config.tooltipXpEnabled(EventType.SMELT).get()){addXpValueTooltip(LangProvider.XP_VALUE_SMELT, event, xpValueSmelting, core);}
            if (xpValueBrewing.size() > 0 && Config.tooltipXpEnabled(EventType.BREW).get()) {addXpValueTooltip(LangProvider.XP_VALUE_BREW, event, xpValueBrewing, core);}
            if (xpValueGrowing.size() > 0 && Config.tooltipXpEnabled(EventType.GROW).get()) {addXpValueTooltip(LangProvider.XP_VALUE_GROW, event, xpValueGrowing, core);}
            if (xpValuePlacing.size() > 0 && Config.tooltipXpEnabled(EventType.BLOCK_PLACE).get()) {addXpValueTooltip(LangProvider.XP_VALUE_PLACE, event, xpValuePlacing, core);}
            //=====================MODIFIERS============================
            if (heldItemXpBoost.size() > 0 && Config.tooltipBonusEnabled(ModifierDataType.HELD).get()) {addModifierTooltip(LangProvider.BOOST_HELD, event, heldItemXpBoost, core);}
            if (wornItemXpBoost.size() > 0 && Config.tooltipBonusEnabled(ModifierDataType.WORN).get()) {addModifierTooltip(LangProvider.BOOST_WORN, event, wornItemXpBoost, core);}
            //=====================VEIN DATA============================
            if (!veinData.equals(VeinData.EMPTY)) {addVeinTooltip(LangProvider.VEIN_TOOLTIP, event, veinData, stack.getItem() instanceof BlockItem);}
         }
	}
	
	private static void addRequirementTooltip(Translation header, ItemTooltipEvent event, Map<String, Integer> reqs, Core core) {
		event.getToolTip().add(header.asComponent());
		for (Map.Entry<String, Integer> req : reqs.entrySet()) {
			event.getToolTip().add(Component.translatable("pmmo."+req.getKey()).append(Component.literal(" "+String.valueOf(req.getValue()))).setStyle(core.getDataConfig().getSkillStyle(req.getKey())));
		}
	}
	
	private static void addXpValueTooltip(Translation header, ItemTooltipEvent event, Map<String, Long> values, Core core) {
		event.getToolTip().add(header.asComponent());
		for (Map.Entry<String, Long> value : values.entrySet()) {
			event.getToolTip().add(Component.translatable("pmmo."+value.getKey()).append(Component.literal(" "+String.valueOf(value.getValue()))).setStyle(core.getDataConfig().getSkillStyle(value.getKey())));
		}
	}
	
	private static void addModifierTooltip(Translation header, ItemTooltipEvent event, Map<String, Double> values, Core core) {
		event.getToolTip().add(header.asComponent());
		for (Map.Entry<String, Double> modifier: values.entrySet()) {
			event.getToolTip().add(Component.translatable("pmmo."+modifier.getKey()).append(Component.literal(" "+modifierPercent(modifier.getValue()))).setStyle(core.getDataConfig().getSkillStyle(modifier.getKey())));
		}
	}
	
	private static void addVeinTooltip(Translation header, ItemTooltipEvent event, VeinData data, boolean isBlockItem) {
		event.getToolTip().add(header.asComponent());
		event.getToolTip().add(LangProvider.VEIN_DATA.asComponent(
				data.chargeCap().orElse(0),
				DP.dp(data.chargeRate().orElse(0d) * 20d)));
		if (isBlockItem) {
			event.getToolTip().add(LangProvider.VEIN_BREAK.asComponent(
					data.consumeAmount().orElse(0)));
		}
	}
	
	private static String modifierPercent(Double value) {
		return DP.dp((value - 1d) * 100d) + "%";
	}
	
	private static Map<String, Integer> getReqData(Core core, ReqType type, ItemStack stack) {		
		//if Reqs are not enabled, ignore the getters and return an empty map
		//This will cause the map to be empty and result in no header being added.
		if (!Config.reqEnabled(type).get()) return new HashMap<>();
		//Gather req data and populate a map for return
		Map<String, Integer> map = core.getReqMap(type, stack);
		if (!Config.HIDE_MET_REQS.get())
			return map;
		//remove values that meet the requirement
		new HashMap<>(map).forEach((skill, level) -> {
			if (SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.Builder.getDefault()).isSkillGroup()) {
				int total = SkillsConfig.SKILLS.get().get(skill)
						.getGroup()
						.keySet()
						.stream()
						.map(groupskill-> core.getData().getPlayerSkillLevel(groupskill, null))
						.collect(Collectors.summingInt(Integer::intValue));
				if (level > total) {
					map.remove(skill);
				}
			}
			else if (core.getData().getPlayerSkillLevel(skill, null) >= level)
				map.remove(skill);
		});
		
		return map;
	}
	
	private static Map<String, Long> getXpGainData(Core core, ResourceLocation itemID, EventType type, ItemStack stack) {
		Map<String, Long> map = core.getTooltipRegistry().getItemXpGainTooltipData(itemID, type, stack);
		if (map.isEmpty() && core.getXpUtils().hasXpGainObjectEntry(type, itemID))
			map = core.getXpUtils().getObjectExperienceMap(type, itemID);
		if (map.isEmpty() && AutoValueConfig.ENABLE_AUTO_VALUES.get())
			map = AutoValues.getExperienceAward(type, itemID, ObjectType.ITEM);
		return core.processSkillGroupXP(map);
	}
	
	private static Map<String, Double> getBonusData(Core core, ResourceLocation itemID, ModifierDataType type, ItemStack stack) {
		Map<String, Double> map = core.getTooltipRegistry().getBonusTooltipData(itemID, type, stack);
		if (map.isEmpty() && core.getXpUtils().hasModifierObjectEntry(type, itemID))
			map = core.getXpUtils().getObjectModifierMap(type, itemID);
		return core.processSkillGroupBonus(map);
	}
}
