package harmonised.pmmo.config.scripting;

import com.mojang.datafixers.util.Pair;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.MobModifier;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.fml.LogicalSide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

public class Functions {
    //Map of keywords and their "registered" consumers
    public static final Map<String, NodeConsumer> KEYWORDS = new HashMap<>();
    public static final Map<String, TargetSelector> TARGETORS = new HashMap<>();
    public static final Pattern operationRegex = Pattern.compile("(>=|<=|>|<|=)\\s*([-+]?\\d*\\.?\\d+(?:[eE][-+]?\\d+)?)");

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
            Map<String, Integer> reqs = intMap(value.getOrDefault("require", ""));
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
            ResourceLocation entityID = new ResourceLocation(param);
            List<MobModifier> modifiers = modifiers(value.getOrDefault("attribute", ""));
            APIUtils.registerMobModifier(type, id, Map.of(entityID, modifiers), true);
        });
        KEYWORDS.put("positive_effect", (param, id, type, value) -> {
            DataSource<?> data = Core.get(LogicalSide.SERVER).getLoader().getLoader(type).getData(id);
            String[] splitParam = param.split(",");
            if (splitParam.length < 2) return;
            ResourceLocation effectID = new ResourceLocation(splitParam[0]);
            int amplifier = Integer.parseInt(splitParam[1]);
            Map<ResourceLocation, Integer> current = new HashMap<>(data.getPositiveEffect());
            current.put(effectID, amplifier);
            APIUtils.registerPositiveEffect(type, id, current, true);
        });
        KEYWORDS.put("negative_effect", (param, id, type, value) -> {
            DataSource<?> data = Core.get(LogicalSide.SERVER).getLoader().getLoader(type).getData(id);
            String[] splitParam = param.split(",");
            if (splitParam.length < 2) return;
            ResourceLocation effectID = new ResourceLocation(splitParam[0]);
            int amplifier = Integer.parseInt(splitParam[1]);
            Map<ResourceLocation, Integer> current = new HashMap<>(data.getNegativeEffect());
            current.put(effectID, amplifier);
            APIUtils.registerNegativeEffect(type, id, current, true);
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
                builder.setLevelReq(intMap(value.get("level_req")));
            if (value.containsKey("salvage_award"))
                builder.setXpAward(mapValue(value.get("salvage_award")));
            if (value.containsKey("max_drops"))
                builder.setSalvageMax(Integer.parseInt(value.get("max_drops")));
            ResourceLocation drop = new ResourceLocation(param);
            APIUtils.registerSalvage(id, Map.of(drop, builder), true);
        });

        TARGETORS.put("food", (param, access) -> {
            String[] exprStr = param.split(",");
            //left equals nutrition, right equals saturation
            float nutVal = 0f;
            float satVal = 0f;
            Operator nutOp = Operator.GTE;
            Operator satOp = Operator.GTE;
            for (String str : exprStr) {
                var match = operationRegex.matcher(str);                ;
                if (!match.find()) continue;
                if (str.startsWith("nutrition")) {
                    nutOp = Operator.fromString(match.group(1));
                    nutVal = Float.valueOf(match.group(2));
                }
                else if (str.startsWith("saturation")) {
                    satOp = Operator.fromString(match.group(1));
                    satVal = Float.valueOf(match.group(2));
                }
            }
            final Pair<Float, Float> values = Pair.of(nutVal, satVal);
            final Pair<Operator, Operator> ops = Pair.of(nutOp, satOp);
            List<ResourceLocation> food = access.registryOrThrow(Registries.ITEM).entrySet().stream()
                .filter(entry -> {
                        FoodProperties props = entry.getValue().getFoodProperties(entry.getValue().getDefaultInstance(), null);
                        return props != null
                        && ops.getFirst().evaluation.test(Integer.valueOf(props.getNutrition()).floatValue(), values.getFirst())
                        && ops.getSecond().evaluation.test(props.getSaturationModifier(), values.getSecond());})
                .map(entry -> entry.getKey().location())
                .toList();
            return new TargetSelector.Selection(ObjectType.ITEM, food);
        });
        TARGETORS.put("tool", (param, access) -> {
            List<ResourceLocation>  tools = new ArrayList<>();
            if (param.isEmpty())
                tools.addAll(access.registryOrThrow(Registries.ITEM).entrySet().stream()
                    .filter(entry -> entry.getValue() instanceof TieredItem)
                    .map(entry -> entry.getKey().location())
                    .toList());
            else {
                ResourceLocation tag = new ResourceLocation(param);
                tools.addAll(access.registryOrThrow(Registries.ITEM).getTag(TagKey.create(Registries.ITEM, tag))
                        .map(named -> named.stream()
                                .filter(holder -> holder.value() instanceof TieredItem)
                                .map(holder -> holder.unwrapKey().get().location()).toList())
                        .orElse(List.of()));
            }
            return new TargetSelector.Selection(ObjectType.ITEM, tools);
        });
        TARGETORS.put("armor", (param, access) -> {
            List<ResourceLocation>  tools = new ArrayList<>();
            if (param.isEmpty())
                tools.addAll(access.registryOrThrow(Registries.ITEM).entrySet().stream()
                        .filter(entry -> entry.getValue() instanceof ArmorItem)
                        .map(entry -> entry.getKey().location())
                        .toList());
            else {
                ResourceLocation tag = new ResourceLocation(param);
                tools.addAll(access.registryOrThrow(Registries.ITEM).getTag(TagKey.create(Registries.ITEM, tag))
                        .map(named -> named.stream()
                                .filter(holder -> holder.value() instanceof ArmorItem)
                                .map(holder -> holder.unwrapKey().get().location()).toList())
                        .orElse(List.of()));
            }
            return new TargetSelector.Selection(ObjectType.ITEM, tools);
        });
        TARGETORS.put("weapon", (param, access) -> {
            List<ResourceLocation>  tools = new ArrayList<>();
            if (param.isEmpty())
                tools.addAll(access.registryOrThrow(Registries.ITEM).entrySet().stream()
                        .filter(entry -> entry.getValue().getAttributeModifiers(EquipmentSlot.MAINHAND, entry.getValue().getDefaultInstance()).containsKey(Attributes.ATTACK_DAMAGE))
                        .map(entry -> entry.getKey().location())
                        .toList());
            else {
                ResourceLocation tag = new ResourceLocation(param);
                tools.addAll(access.registryOrThrow(Registries.ITEM).getTag(TagKey.create(Registries.ITEM, tag))
                        .map(named -> named.stream()
                                .filter(holder -> holder.value().getAttributeModifiers(EquipmentSlot.MAINHAND, holder.value().getDefaultInstance()).containsKey(Attributes.ATTACK_DAMAGE))
                                .map(holder -> holder.unwrapKey().get().location()).toList())
                        .orElse(List.of()));
            }
            return new TargetSelector.Selection(ObjectType.ITEM, tools);
        });
    }
    private enum Operator {
        GT((one, two) -> one > two),
        LT((one, two) -> one < two),
        EQ(Float::equals),
        GTE((one, two) -> one >= two),
        LTE((one, two) -> one <= two);

        public final BiPredicate<Float, Float> evaluation;
        Operator(BiPredicate<Float, Float> evaluation) {
            this.evaluation = evaluation;
        }

        public static Operator fromString(String str) {
            return switch (str) {
                case ">" -> GT;
                case "<" -> LT;
                case "=" -> EQ;
                case ">=" -> GTE;
                case "<=" -> LTE;
                default -> null;
            };
        }
    }

    private static Map<String, Long> mapValue(String value) {
        Map<String, Long> outMap = new HashMap<>();
        String[] elements = value.replaceAll("\\)", "").split(",");
        for (int i = 0; i <= elements.length-2; i += 2) {
            outMap.put(elements[i], Long.valueOf(elements[i+1]));
        }
        return outMap;
    }

    private static Map<String, Integer> intMap(String value) {
        Map<String, Integer> outMap = new HashMap<>();
        String[] elements = value.replaceAll("\\)", "").split(",");
        for (int i = 0; i <= elements.length-2; i += 2) {
            outMap.put(elements[i], Integer.valueOf(elements[i+1]));
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

    private static List<MobModifier> modifiers(String value) {
        List<MobModifier> output = new ArrayList<>();
        String[] elements = value.replaceAll("\\)", "").split(",");
        if (elements.length % 3 != 0) return output;
        for (int i = 0; i <= elements.length-3; i += 3) {
            output.add(new MobModifier(
                    new ResourceLocation(elements[i]),
                    Double.valueOf(elements[i+1]),
                    AttributeModifier.Operation.valueOf(elements[i+2])));
        }
        return output;
    }
}
