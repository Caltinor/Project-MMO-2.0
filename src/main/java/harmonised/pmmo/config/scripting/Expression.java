package harmonised.pmmo.config.scripting;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Expression(
        ObjectType targetType,
        Identifier targetID,
        Map<String, String> value,
        List<Node> features) {
    public record Node(String param, NodeConsumer consumer) {@Override public String toString() {return param;}}

    public static List<Expression> create(RegistryAccess access, String str) {
        MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.DATA, "Raw Script Line: {}", str);
        List<Expression> expressions = new ArrayList<>();
        String[] nodes = str.replace(";", "").split("\\)\\.");
        List<Node> features = new ArrayList<>();
        Map<String, String> values = new HashMap<>();
        List<Identifier> targetIDs = new ArrayList<>();
        ObjectType targetType = null;
        for (String node : nodes) {
            MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.DATA, "NODE: {}", node);
            String keyword = node.substring(0, node.indexOf('('));
            String param = node.substring(node.indexOf('(')+1).replaceAll("[ )]", "");

            if (targetType == null && ObjectType.byName(keyword.toUpperCase()) != null) {
                targetType = ObjectType.byName(keyword.toUpperCase());
                targetIDs = parseIDs(param, targetType, access);
            }
            else if (targetType == null && Functions.TARGETORS.containsKey(keyword)) {
                TargetSelector.Selection selection = Functions.TARGETORS.get(keyword).read(param, access);
                targetType = selection.type();
                targetIDs = selection.IDs();
            }
            else if (Functions.KEYWORDS.containsKey(keyword))
                features.add(new Node(param, Functions.KEYWORDS.get(keyword)));
            else values.put(keyword, param);
        }

        for (Identifier id : targetIDs) {expressions.add(new Expression(targetType, id, values, features));}
        expressions.forEach(expr -> MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.DATA, "Expressions: {}", expr));
        return expressions;
    }

    public boolean isValid() {return targetType != null && targetID != null;}

    public void commit() {
        features.forEach(c -> c.consumer().consume(c.param(), targetID, targetType, value));
    }

    public static List<Identifier> parseIDs(String raw, ObjectType type, RegistryAccess access) {
        List<Identifier> ids = new ArrayList<>();
        String[] rawSplit = raw.split(",");
        for (String str : rawSplit) {
            if (str.startsWith("#")) {
                Identifier tagID = Reference.of(str.substring(1));
                ids.addAll(getMembers(true, tagID, access, type));
            }
            else if (str.endsWith(":*")) {
                Identifier namespace = Reference.of(str.replace("*", "wildcard"));
                ids.addAll(getMembers(false, namespace, access, type));
            }
            else ids.add(Reference.of(str));
        }
        return ids;
    }

    private static List<Identifier> getMembers(boolean isTag, Identifier tagID, RegistryAccess access, ObjectType type) {
        return switch (type) {
            case ITEM -> readRegistry(isTag, access, Registries.ITEM, tagID);
            case BLOCK -> readRegistry(isTag, access, Registries.BLOCK, tagID);
            case ENTITY -> readRegistry(isTag, access, Registries.ENTITY_TYPE, tagID);
            case BIOME -> readRegistry(isTag, access, Registries.BIOME, tagID);
            case ENCHANTMENT -> readRegistry(isTag, access, Registries.ENCHANTMENT, tagID);
            default -> List.of();
        };
    }

    private static <T> List<Identifier> readRegistry(boolean forTags, RegistryAccess access, ResourceKey<Registry<T>> registry, Identifier tagID) {
        var reg = access.lookupOrThrow(registry);
        return forTags
                ? reg.get(TagKey.create(registry, tagID))
                .map(named -> named.stream().map(holder -> holder.unwrapKey().get().identifier()).toList())
                .orElse(List.of())
                : reg.keySet().stream().filter(id -> id.getNamespace().equals(tagID.getNamespace())).toList();
    }

}
