package harmonised.pmmo.config.scripting;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.core.Core;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Functions {
    //Map of keywords and their "registered" consumers
    public static final Map<String, NodeConsumer> KEYWORDS = new HashMap<>();
    public static final Map<String, TargetSelector> TARGETORS = new HashMap<>();

    static {
        KEYWORDS.put("xp", (param, id, type, value) -> {
            EventType event = EventType.byName(param.toUpperCase());
            if (event == null) return;
            Map<String, Long> award = mapValue(value.getOrDefault("award", ""));
            if (award.isEmpty()) return;
            APIUtils.registerXpAward(type, id, event, award, true);
        });
        KEYWORDS.put("deal_damage", (param, id, type, value) -> {
            Map<String, Long> award = mapValue(value.getOrDefault("award", ""));
            if (award.isEmpty()) return;
            APIUtils.registerXpAward(type, id, EventType.DEAL_DAMAGE, award, true);
        });
        KEYWORDS.put("receive_damage", (param, id, type, value) -> {
            Map<String, Long> award = mapValue(value.getOrDefault("award", ""));
            if (award.isEmpty()) return;
            APIUtils.registerXpAward(type, id, EventType.RECEIVE_DAMAGE, award, true);
        });
        KEYWORDS.put("req", (param, id, type, value) -> {
            ReqType reqType = ReqType.byName(param.toUpperCase());
            if (reqType == null) return;
            Map<String, Long> reqs = mapValue(value.getOrDefault("require", ""));
            if (reqs.isEmpty()) return;
            APIUtils.registerRequirement(type, id, reqType, reqs, true);
        });
        KEYWORDS.put("bonus", (param, id, type, value) -> {
            ModifierDataType bonusType = ModifierDataType.byName(param.toUpperCase());
            if (bonusType == null) return;
            Map<String, Double> bonus = doubleMap(value.getOrDefault("value", ""));
            if (bonus.isEmpty()) return;
            APIUtils.registerBonus(type, id, bonusType, bonus, true);
        });
        KEYWORDS.put("vein_charge", (param, id, type, value) -> {
            DataSource<?> source = Core.get(LogicalSide.SERVER).getLoader().getLoader(type).getData(id);
            if (source instanceof ObjectData data) {
                VeinData current = data.veinData();
                APIUtils.registerVeinData(type, id, current.chargeCap, Optional.of(Double.valueOf(param)), current.consumeAmount, true);
            }
        });
        KEYWORDS.put("vein_capacity", (param, id, type, value) -> {
            DataSource<?> source = Core.get(LogicalSide.SERVER).getLoader().getLoader(type).getData(id);
            if (source instanceof ObjectData data) {
                VeinData current = data.veinData();
                APIUtils.registerVeinData(type, id, Optional.of(Integer.valueOf(param)), current.chargeRate, current.consumeAmount, true);
            }
        });
        KEYWORDS.put("vein_consume", (param, id, type, value) -> {
            DataSource<?> source = Core.get(LogicalSide.SERVER).getLoader().getLoader(type).getData(id);
            if (source instanceof ObjectData data) {
                VeinData current = data.veinData();
                APIUtils.registerVeinData(type, id, current.chargeCap, current.chargeRate, Optional.of(Integer.valueOf(param)), true);
            }
        });
        KEYWORDS.put("mob_scale", (param, id, type, value) -> {
            ResourceLocation entityID = ResourceLocation.parse(param);
            Map<String, Double> modifiers = doubleMap(value.getOrDefault("attribute", ""));
            APIUtils.registerMobModifier(type, id, Map.of(entityID, modifiers), true);
        });
        KEYWORDS.put("positive_effect", (param, id, type, value) -> {
            DataSource<?> data = Core.get(LogicalSide.SERVER).getLoader().getLoader(type).getData(id);
            String[] splitParam = param.split(",");
            if (splitParam.length < 2) return;
            ResourceLocation effectID = ResourceLocation.parse(splitParam[0]);
            int amplifier = Integer.parseInt(splitParam[1]);
            Map<ResourceLocation, Integer> current = new HashMap<>(data.getPositiveEffect());
            current.put(effectID, amplifier);
            APIUtils.registerPositiveEffect(type, id, current, true);
        });
        KEYWORDS.put("negative_effect", (param, id, type, value) -> {
            DataSource<?> data = Core.get(LogicalSide.SERVER).getLoader().getLoader(type).getData(id);
            String[] splitParam = param.split(",");
            if (splitParam.length < 2) return;
            ResourceLocation effectID = ResourceLocation.parse(splitParam[0]);
            int amplifier = Integer.parseInt(splitParam[1]);
            Map<ResourceLocation, Integer> current = new HashMap<>(data.getNegativeEffect());
            current.put(effectID, amplifier);
            APIUtils.registerPositiveEffect(type, id, current, true);
        });
        KEYWORDS.put("salvage", (param, id, type, value) -> {
            APIUtils.SalvageBuilder builder = APIUtils.SalvageBuilder.start();
            if (value.containsKey("chance_level"))
                builder.setChancePerLevel(doubleMap(value.get("chance_level")));
            if (value.containsKey("chance_base"))
                builder.setBaseChance(Double.parseDouble(value.get("chance_base")));
            if (value.containsKey("chance_max"))
                builder.setMaxChance(Double.parseDouble(value.get("chance_max")));
            if (value.containsKey("level_req"))
                builder.setLevelReq(mapValue(value.get("level_req")));
            if (value.containsKey("salvage_award"))
                builder.setXpAward(mapValue(value.get("salvage_award")));
            if (value.containsKey("max_drops"))
                builder.setSalvageMax(Integer.parseInt(value.get("max_drops")));
            ResourceLocation drop = ResourceLocation.parse(param);
            APIUtils.registerSalvage(id, Map.of(drop, builder), true);
        });

        TARGETORS.put("food", param -> {
            //TODO actually read the param
            //Oh shit, yeah i needed a way to use the registry information for this to work.
            return new TargetSelector.Selection(ObjectType.ITEM, List.of());
        });
    }

    private static Map<String, Long> mapValue(String value) {
        Map<String, Long> outMap = new HashMap<>();
        String[] elements = value.replaceAll("\\)", "").split(",");
        for (int i = 0; i <= elements.length-2; i += 2) {
            outMap.put(elements[i], Long.valueOf(elements[i+1]));
        }
        return outMap;
    }

    private static Map<String, Double> doubleMap(String value) {
        Map<String, Double> outMap = new HashMap<>();
        String[] elements = value.replaceAll("\\)", "").split(",");
        for (int i = 0; i <= elements.length-2; i += 2) {
            outMap.put(elements[i], Double.valueOf(elements[i+1]));
        }
        return outMap;
    }
}
