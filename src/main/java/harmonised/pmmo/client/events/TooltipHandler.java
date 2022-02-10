package harmonised.pmmo.client.events;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.readers.ModifierDataType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.autovalues.AutoValues;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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

        Player player = event.getPlayer();

        if(player != null) {
        	Core core = Core.get(LogicalSide.CLIENT);
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();

            if(item.getRegistryName() == null)
                return;

            /*if(ClientHandler.OPEN_MENU.isDown())
            {
                GlossaryScreen.setButtonsToKey(regKey);
                Minecraft.getInstance().setScreen(new GlossaryScreen(Minecraft.getInstance().player.getUUID(), new TranslatableComponent("pmmo.glossary"), false));
                return;
            }*/

            Map<String, Integer> craftReq = getReqData(core, item.getRegistryName(), ReqType.CRAFT, stack);
            Map<String, Integer> wearReq = getReqData(core, item.getRegistryName(), ReqType.WEAR, stack);
            Map<String, Integer> toolReq = getReqData(core, item.getRegistryName(), ReqType.TOOL, stack);
            Map<String, Integer> weaponReq = getReqData(core, item.getRegistryName(), ReqType.WEAPON, stack);
            Map<String, Integer> useReq = getReqData(core, item.getRegistryName(), ReqType.USE, stack);
            //Map<String, Integer> useEnchantmentReq = XP.getEnchantsUseReq(stack);
            Map<String, Integer> placeReq = getReqData(core, item.getRegistryName(), ReqType.PLACE, stack);
            Map<String, Integer> breakReq = getReqData(core, item.getRegistryName(), ReqType.BREAK, stack);
            Map<String, Long> xpValueBreaking = getXpGainData(core, item.getRegistryName(),EventType.BLOCK_BREAK, stack);
            Map<String, Long> xpValueCrafting = getXpGainData(core, item.getRegistryName(),EventType.CRAFT, stack);
            Map<String, Long> xpValueSmelting = getXpGainData(core, item.getRegistryName(),EventType.SMELT, stack);
            Map<String, Long> xpValueCooking = getXpGainData(core, item.getRegistryName(),EventType.COOK, stack);
            Map<String, Long> xpValueBrewing = getXpGainData(core, item.getRegistryName(),EventType.BREW, stack);
            Map<String, Long> xpValueGrowing = getXpGainData(core, item.getRegistryName() ,EventType.GROW, stack);
            Map<String, Long> xpValuePlacing = getXpGainData(core, item.getRegistryName(),EventType.BLOCK_PLACE, stack);
            //Map<String, Map<String, Double>> salvageInfo = JsonConfig.data2.get(JType.SALVAGE).getOrDefault(regKey, new HashMap<>());
            //Map<String, Map<String, Double>> salvageFrom = JsonConfig.data2.get(JType.SALVAGE_FROM).getOrDefault(regKey, new HashMap<>());
            Map<String, Double> heldItemXpBoost = getBonusData(core, item.getRegistryName(), ModifierDataType.HELD, stack);
            Map<String, Double> wornItemXpBoost = getBonusData(core, item.getRegistryName(), ModifierDataType.WORN, stack);
            
            //=====================REQUIREMENTS=========================
            if (craftReq.size() > 0) {addRequirementTooltip("pmmo.toCraft", event, craftReq, core);}
            if (wearReq.size() > 0)	 {addRequirementTooltip("pmmo.toWear", event, wearReq, core);}
            if (toolReq.size() > 0)  {addRequirementTooltip("pmmo.tool", event, toolReq, core);}
            if (weaponReq.size() > 0){addRequirementTooltip("pmmo.weapon", event, weaponReq, core);}
            if (useReq.size() > 0)   {addRequirementTooltip("pmmo.use", event, useReq, core);}
            if (placeReq.size() > 0) {addRequirementTooltip("pmmo.place", event, placeReq, core);}
            if (breakReq.size() > 0) {addRequirementTooltip("pmmo.break", event, breakReq, core);}
            //=====================XP VALUES============================
            if (xpValueBreaking.size() > 0){addXpValueTooltip("pmmo.xpValueBreak", event, xpValueBreaking, core);}
            if (xpValueCrafting.size() > 0){addXpValueTooltip("pmmo.xpValueCraft", event, xpValueCrafting, core);}
            if (xpValueSmelting.size() > 0){addXpValueTooltip("pmmo.xpValueSmelt", event, xpValueSmelting, core);}
            if (xpValueCooking.size() > 0) {addXpValueTooltip("pmmo.xpValueCook", event, xpValueCooking, core);}
            if (xpValueBrewing.size() > 0) {addXpValueTooltip("pmmo.xpValueBrew", event, xpValueBrewing, core);}
            if (xpValueGrowing.size() > 0) {addXpValueTooltip("pmmo.xpValueGrow", event, xpValueGrowing, core);}
            if (xpValuePlacing.size() > 0) {addXpValueTooltip("pmmo.xpValuePlace", event, xpValuePlacing, core);}
            //=====================MODIFIERS============================
            if (heldItemXpBoost.size() > 0) {addModifierTooltip("pmmo.itemXpBoostHeld", event, heldItemXpBoost, core);}
            if (wornItemXpBoost.size() > 0) {addModifierTooltip("pmmo.itemXpBoostWorn", event, wornItemXpBoost, core);}
         }
	}
	
	private static void addRequirementTooltip(String header, ItemTooltipEvent event, Map<String, Integer> reqs, Core core) {
		event.getToolTip().add(new TranslatableComponent(header));
		for (Map.Entry<String, Integer> req : reqs.entrySet()) {
			event.getToolTip().add(new TranslatableComponent("pmmo."+req.getKey()).append(new TextComponent(" "+String.valueOf(req.getValue()))).setStyle(core.getDataConfig().getSkillStyle(req.getKey())));
		}
	}
	
	private static void addXpValueTooltip(String header, ItemTooltipEvent event, Map<String, Long> values, Core core) {
		event.getToolTip().add(new TranslatableComponent(header));
		for (Map.Entry<String, Long> value : values.entrySet()) {
			event.getToolTip().add(new TranslatableComponent("pmmo."+value.getKey()).append(new TextComponent(" "+String.valueOf(value.getValue()))).setStyle(core.getDataConfig().getSkillStyle(value.getKey())));
		}
	}
	
	private static void addModifierTooltip(String header, ItemTooltipEvent event, Map<String, Double> values, Core core) {
		event.getToolTip().add(new TranslatableComponent(header));
		for (Map.Entry<String, Double> modifier: values.entrySet()) {
			event.getToolTip().add(new TranslatableComponent("pmmo."+modifier.getKey()).append(new TextComponent(" "+modifierPercent(modifier.getValue()))).setStyle(core.getDataConfig().getSkillStyle(modifier.getKey())));
		}
	}
	
	private static String modifierPercent(Double value) {
		return DP.dp((value - 1d) * 100d) + "%";
	}
	
	private static Map<String, Integer> getReqData(Core core, ResourceLocation itemID, ReqType type, ItemStack stack) {
		//if Reqs are not enabled, ignore the getters and return an empty map
		//This will cause the map to be empty and result in no header being added.
		if (!Config.reqEnabled(type).get()) return new HashMap<>();
		//Gather req data and populate a map for return
		Map<String, Integer> map = core.getTooltipRegistry().getItemRequirementTooltipData(itemID, type, stack);
		if (map.isEmpty() && core.getSkillGates().doesObjectReqExist(type, itemID))
			map = core.getSkillGates().getObjectSkillMap(type, itemID);
		if (map.isEmpty())
			map = AutoValues.getRequirements(type, itemID, ObjectType.ITEM);
		return map;
	}
	
	private static Map<String, Long> getXpGainData(Core core, ResourceLocation itemID, EventType type, ItemStack stack) {
		Map<String, Long> map = core.getTooltipRegistry().getItemXpGainTooltipData(itemID, type, stack);
		if (map.isEmpty() && core.getXpUtils().hasXpGainObjectEntry(type, itemID))
			map = core.getXpUtils().getObjectExperienceMap(type, itemID);
		if (map.isEmpty())
			map = AutoValues.getExperienceAward(type, itemID, ObjectType.ITEM);
		return map;
	}
	
	private static Map<String, Double> getBonusData(Core core, ResourceLocation itemID, ModifierDataType type, ItemStack stack) {
		Map<String, Double> map = core.getTooltipRegistry().getBonusTooltipData(itemID, type, stack);
		if (map.isEmpty() && core.getXpUtils().hasModifierObjectEntry(type, itemID))
			map = core.getXpUtils().getObjectModifierMap(type, itemID);
		return map;
	}
}
