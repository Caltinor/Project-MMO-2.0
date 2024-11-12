package harmonised.pmmo.config.scripting;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Expression(
        ObjectType targetType,
        ResourceLocation targetID,
        Map<String, String> value,
        List<Node> features) {
    public record Node(String param, NodeConsumer consumer) {@Override public String toString() {return param;}}

    public static List<Expression> create(String str) {
        MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.DATA, "Raw Script Line: {}", str);
        List<Expression> expressions = new ArrayList<>();
        String[] nodes = str.replace(";", "").split("\\)\\.");
        List<Node> features = new ArrayList<>();
        Map<String, String> values = new HashMap<>();
        String[] targetIDs = new String[]{};
        ObjectType targetType = null;
        for (String node : nodes) {
            MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.DATA, "NODE: {}", node);
            String keyword = node.substring(0, node.indexOf('('));
            String param = node.substring(node.indexOf('(')+1).replaceAll("[ )]", "");

            if (ObjectType.byName(keyword.toUpperCase()) != null) {
                targetType = ObjectType.byName(keyword.toUpperCase());
                targetIDs = param.split(",");
            }
            /*TODO find a way to hook script reads into a loading stage with registry access so that we
             *can use dynamic keywords such as food, weapons, etc to make scripts better.
            else if (Functions.TARGETORS.containsKey(keyword)) {
                TargetSelector.Selection selection = Functions.TARGETORS.get(keyword).read(param);
                targetType = selection.type();
                targetIDs = (String[]) selection.IDs().stream().map(ResourceLocation::toString).toArray();
            }*/
            else if (Functions.KEYWORDS.containsKey(keyword))
                features.add(new Node(param, Functions.KEYWORDS.get(keyword)));
            else values.put(keyword, param);
        }

        for (String id : targetIDs) {expressions.add(new Expression(targetType, ResourceLocation.parse(id), values, features));}
        expressions.forEach(expr -> MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.DATA, "Expressions: {}", expr));
        return expressions;
    }

    public boolean isValid() {return targetType != null && targetID != null;}

    public void commit() {
        features.forEach(c -> c.consumer().consume(c.param(), targetID, targetType, value));
    }
}
