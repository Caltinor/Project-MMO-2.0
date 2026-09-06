package harmonised.pmmo.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.GlobalsConfig;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.ServerData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.config.writers.PackGenerator;
import harmonised.pmmo.features.anticheese.AntiCheeseConfig;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CmdNodeServerConfigs {
    public static ArgumentBuilder<CommandSourceStack, ?> SERVERCONFIGS() {
        return Commands.literal("mod_settings")
//                .then(perks())
                .then(skills())
                .then(antiCheese())
                .then(autoValues())
                //Everything below here is server config specifics
                //region GENERAL
                .then(setServerValueDouble(ServerData.General.CREATIVE_REACH))
                .then(setServerValueID(ServerData.General.SALVAGE_BLOCK, Registries.BLOCK))
                .then(setServerValueBool(ServerData.General.TREASURE))
                .then(setServerValueBool(ServerData.General.BREWING))
                //endregion
                //region LEVELS
                .then(setServerValueLong(ServerData.Levels.MAX_LEVEL))
                .then(setServerValueDouble(ServerData.Levels.LOSS_DEATH))
                .then(setServerValueBool(ServerData.Levels.LOSE_EXCESS))
                .then(setServerValueDouble(ServerData.Levels.GLOBAL_MODIFIER))
                .then(setServerValueLong(ServerData.Levels.XP_MIN))
                .then(setServerValueDouble(ServerData.Levels.XP_BASE))
                .then(setServerValueDouble(ServerData.Levels.PER_LEVEL))
                .then(Commands.literal(ServerData.Levels.SKILL_MODIFIER)
                        .then(Commands.literal("clear").executes(ctx -> setServerValue(ctx, ServerData.Levels.SKILL_MODIFIER, "default")))
                        .then(Commands.literal("set")
                                .then(Commands.argument("skill", StringArgumentType.word())
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                .executes(ctx -> {
                                                    String skill = StringArgumentType.getString(ctx, "skill");
                                                    double value = DoubleArgumentType.getDouble(ctx, "value");
                                                    String mapAsString = String.join(",", skill, String.valueOf(value));
                                                    String type = "custom:" + mapAsString;
                                                    return setServerValue(ctx, ServerData.Levels.SKILL_MODIFIER, type);
                                                })
                                        )
                                )
                        )
                )
                //endregion
                //region REQS
                .then(Commands.literal(ServerData.Requirements.DISABLE)
                        .then(Commands.argument("value", StringArgumentType.word())
                                .suggests((c,b) -> CmdNodeConfig.enumSuggestion(ReqType.values(), b))
                                .executes(ctx -> setServerValue(ctx, ServerData.Requirements.DISABLE, "string"))
                        )
                )
                //endregion
                //region XP
                .then(setServerValueDouble(ServerData.XpGains.REUSE))
                .then(setServerValueBool(ServerData.XpGains.PERKSPLUS))
                .then(Commands.literal(ServerData.XpGains.PLAYERACTIONS)
                        .then(Commands.argument("event", StringArgumentType.word())
                                .suggests((c,b) -> CmdNodeConfig.enumSuggestion(new EventType[]{
                                    EventType.JUMP, EventType.SPRINT_JUMP, EventType.CROUCH_JUMP,
                                    EventType.BREATH_CHANGE, EventType.HEALTH_INCREASE, EventType.HEALTH_DECREASE,
                                    EventType.SPRINTING, EventType.SUBMERGED, EventType.SWIMMING,
                                    EventType.DIVING, EventType.SURFACING, EventType.SWIM_SPRINTING
                                }, b))
                                .then(Commands.argument("skill", StringArgumentType.word())
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                .executes(ctx -> {
                                                    ServerData src = Config.server();
                                                    Map<String, String> map = new HashMap<>();
                                                    map.put(ServerData.XpGains.EVENT, StringArgumentType.getString(ctx, "event").toUpperCase());
                                                    String skill = StringArgumentType.getString(ctx, "skill");
                                                    String value = skill + "," + DoubleArgumentType.getDouble(ctx, "value");
                                                    map.put("value", value);
                                                    ServerData data = src.getFromScripting(ServerData.XpGains.PLAYERACTIONS, map);
                                                    commit(ctx, data);
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("generic_damage_xp")
                        .then(Commands.argument("event", StringArgumentType.word())
                                .suggests((c,b) -> CmdNodeConfig.enumSuggestion(new EventType[]{
                                    EventType.DEAL_DAMAGE, EventType.RECEIVE_DAMAGE, EventType.MITIGATE_DAMAGE
                                }, b))
                                .then(Commands.argument("damage_type", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.DAMAGE_TYPE))
                                        .then(Commands.argument("skill", StringArgumentType.word())
                                                .then(Commands.argument("value", LongArgumentType.longArg())
                                                        .executes(ctx -> {
                                                            ServerData src = Config.server();
                                                            String eventType = switch(StringArgumentType.getString(ctx, "event")) {
                                                                case "DEAL_DAMAGE" -> ServerData.XpGains.DAMAGE_DEALT;
                                                                case "RECEIVE_DAMAGE" -> ServerData.XpGains.DAMAGE_RECEIVED;
                                                                case "MITIGATE_DAMAGE" -> ServerData.XpGains.DAMAGE_MITIGATED;
                                                                default -> "";
                                                            };
                                                            Map<String, String> map = new HashMap<>();
                                                            map.put(ServerData.XpGains.DAMAGE_TYPE, StringArgumentType.getString(ctx, "damage_type").toUpperCase());
                                                            String skill = StringArgumentType.getString(ctx, "skill");
                                                            String value = skill + "," + DoubleArgumentType.getDouble(ctx, "value");
                                                            map.put("value", value);
                                                            ServerData data = src.getFromScripting(eventType, map);
                                                            commit(ctx, data);
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                //endregion
                //region PARTY
                .then(setServerValueInt(ServerData.Party.PARTY_RANGE))
                .then(Commands.literal(ServerData.Party.PARTY_BONUS)
                    .then(Commands.literal("clear").executes(ctx -> setServerValue(ctx, ServerData.Party.PARTY_BONUS, "default")))
                    .then(Commands.literal("set")
                        .then(Commands.argument("skill", StringArgumentType.word())
                            .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                .executes(ctx -> {
                                    ServerData src = Config.server();
                                    String existing = src.party().bonus().entrySet().stream()
                                            .map(entry -> entry.getKey() + "," + entry.getValue())
                                            .collect(Collectors.joining(","));
                                    Map<String, String> map = new HashMap<>();
                                    String skill = StringArgumentType.getString(ctx, "skill");
                                    String value = existing + "," + skill + "," + DoubleArgumentType.getDouble(ctx, "value");
                                    map.put("value", value);
                                    ServerData data = src.getFromScripting(ServerData.Party.PARTY_BONUS, map);
                                    commit(ctx, data);
                                    return 0;
                                })
                            )
                        )
                    )
                )
                //endregion
                //region MOB_SCALING
                .then(setServerValueBool(ServerData.MobScaling.ENABLED))
                .then(setServerValueInt(ServerData.MobScaling.AOE))
                .then(setServerValueLong(ServerData.MobScaling.BASE_LVL))
                .then(setServerValueDouble(ServerData.MobScaling.BOSS_SCALE))
                .then(setServerValueBool(ServerData.MobScaling.USE_EXPONENT))
                .then(setServerValueDouble(ServerData.MobScaling.PER_LEVEL))
                .then(setServerValueDouble(ServerData.MobScaling.POWER_BASE))
                .then(Commands.literal(ServerData.MobScaling.RATIOS)
                    .then(Commands.argument("attribute", IdentifierArgument.id())
                        .suggests((c,b) -> SharedSuggestionProvider.suggest(c.getSource().registryAccess().lookupOrThrow(Registries.ATTRIBUTE).keySet().stream().map(Identifier::toString), b))
                        .then(Commands.literal("remove").executes(ctx -> {
                                ServerData data = Config.server();
                                String attribute = IdentifierArgument.getId(ctx, "attribute").toString();
                                Map<String, String> map = new HashMap<>();
                                map.put(ServerData.MobScaling.ATTRIBUTE_ID, attribute);
                                commit(ctx, data.getFromScripting(ServerData.MobScaling.RATIOS, map));
                                return 0;
                            })
                        )
                        .then(Commands.literal("add")
                            .then(Commands.argument("skill", StringArgumentType.word())
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                    .executes(ctx -> {
                                        ServerData data = Config.server();
                                        String attribute = IdentifierArgument.getId(ctx, "attribute").toString();
                                        String skill = StringArgumentType.getString(ctx, "skill");
                                        String ratio = String.valueOf(DoubleArgumentType.getDouble(ctx, "value"));
                                        String value = String.join(",", skill, ratio);
                                        Map<String, String> map = new HashMap<>();
                                        map.put(ServerData.MobScaling.ATTRIBUTE_ID, attribute);
                                        map.put("value", value);
                                        commit(ctx, data.getFromScripting(ServerData.MobScaling.RATIOS, map));
                                        return 0;
                                    })
                                )
                            )
                        )
                    )
                )
                //endregion
                //region VEIN
                .then(setServerValueBool(ServerData.VeinMiner.ENABLED))
                .then(setServerValueBool(ServerData.VeinMiner.REQUIRE))
                .then(setServerValueInt(ServerData.VeinMiner.DEFAULT_CONSUME))
                .then(setServerValueDouble(ServerData.VeinMiner.CHARGE_MODIFIER))
                .then(Commands.literal("vein_blacklist_tools")
                    .then(Commands.literal("clear").executes(ctx -> setServerValue(ctx, ServerData.VeinMiner.BLACKLIST, "default")))
                    .then(Commands.literal("add")
                        .then(Commands.argument("id", IdentifierArgument.id())
                            .suggests((c,b) -> SharedSuggestionProvider.suggest(c.getSource().registryAccess().lookupOrThrow(Registries.ITEM).keySet().stream().map(Identifier::toString) , b))
                            .executes(ctx -> {
                                ServerData src = Config.server();
                                String existing = src.veinMiner().blacklist().stream().map(Identifier::toString).collect(Collectors.joining(","));
                                Map<String, String> map = new HashMap<>();
                                String skill = IdentifierArgument.getId(ctx, "id").toString();
                                String value = String.join(",", existing, skill);
                                map.put("value", value);
                                ServerData data = src.getFromScripting(ServerData.VeinMiner.BLACKLIST, map);
                                commit(ctx, data);
                                return 0;
                            })

                        )
                    )
                )
                //endregion
                ;
    }



    private static ArgumentBuilder<CommandSourceStack, ?> setServerValueBool(String param) {
        return Commands.literal(param)
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> setServerValue(ctx, param, "long")));
    }
    private static ArgumentBuilder<CommandSourceStack, ?> setServerValueID(String param, ResourceKey<? extends Registry<?>> key) {
        return Commands.literal(param)
                .then(Commands.argument("value", IdentifierArgument.id())
                    .suggests((c,b) -> SharedSuggestionProvider.suggest(c.getSource().registryAccess().lookupOrThrow(key).keySet().stream().map(Identifier::toString), b))
                    .executes(ctx -> setServerValue(ctx, param, "identifier")));
    }
    private static ArgumentBuilder<CommandSourceStack, ?> setServerValueInt(String param) {
        return Commands.literal(param)
                .then(Commands.argument("value", LongArgumentType.longArg())
                        .executes(ctx -> setServerValue(ctx, param, "int")));
    }
    private static ArgumentBuilder<CommandSourceStack, ?> setServerValueLong(String param) {
        return Commands.literal(param)
                .then(Commands.argument("value", LongArgumentType.longArg())
                        .executes(ctx -> setServerValue(ctx, param, "long")));
    }
    private static ArgumentBuilder<CommandSourceStack, ?> setServerValueDouble(String param) {
        return Commands.literal(param)
                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                        .executes(ctx -> setServerValue(ctx, param, "double")));
    }

    private static int setServerValue(CommandContext<CommandSourceStack> ctx, String param, String type) {
        ServerData src = Config.server();
        String[] typeVal = type.split(":");
        String value = switch (typeVal[0]) {
            case "string" -> StringArgumentType.getString(ctx, "value");
            case "bool" -> String.valueOf(BoolArgumentType.getBool(ctx, "value"));
            case "double" -> String.valueOf(DoubleArgumentType.getDouble(ctx, "value"));
            case "long" -> String.valueOf(LongArgumentType.getLong(ctx, "value"));
            case "int" -> String.valueOf(IntegerArgumentType.getInteger(ctx, "value"));
            case "identifier" -> IdentifierArgument.getId(ctx, "value").toString();
            case "custom" -> typeVal.length > 1 ? typeVal[1] : "";
            default -> "";
        };
        ServerData data = src.getFromScripting(param, Map.of("value", value));
        commit(ctx, data);
        return 0;
    }
//TODO decide if this is something worth building
//    public static ArgumentBuilder<CommandSourceStack, ?> perks() {
//        return Commands.literal("perks");
//    }

    public static ArgumentBuilder<CommandSourceStack, ?> autoValues() {
        return Commands.literal("auto_values");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> antiCheese() {
        return Commands.literal("anti_cheese");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> skills() {
        return Commands.literal("skills");
    }

    private static void commit(CommandContext<CommandSourceStack> ctx, ConfigData<?> data) {
        Config.CONFIG.set(data.getType(), data);
        Networking.sendToClient(new CP_SyncConfig(ConfigListener.ServerConfigs.SERVER, data), ctx.getSource().getPlayer());
        writeFile(ctx.getSource().getServer(), data);
    }

    private static void writeFile(MinecraftServer server, ConfigData<?> src) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //Create base pack if not exists
        Path packPath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("command_pack").resolve("pack.mcmeta");
        if (!Files.exists(packPath)) CmdNodeConfig.generatePack(server, gson);
        //3. write files with path
        Path finalPath = server.getWorldPath(LevelResource.DATAPACK_DIR)
                .resolve("command_pack")
                .resolve("data/pmmo/config");
        finalPath.toFile().mkdirs();
        try {
            Files.writeString(
                    finalPath.resolve(src.getType().filename+".json"),
                    srcToString(src, gson),
                    Charset.defaultCharset(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {System.out.println("Error While Generating Server Config File For: "+src.getType().filename+" ("+e+")");}
    }

    private static String srcToString(ConfigData<?> data, Gson gson) {
        JsonObject raw = switch (data.getType().filename) {
            case "server" -> ServerData.CODEC.codec().encodeStart(JsonOps.INSTANCE, (ServerData) data).result().get().getAsJsonObject();
            case "autovalues" -> AutoValueConfig.CODEC.codec().encodeStart(JsonOps.INSTANCE, (AutoValueConfig) data).result().get().getAsJsonObject();
            case "anticheese" -> AntiCheeseConfig.CODEC.codec().encodeStart(JsonOps.INSTANCE, (AntiCheeseConfig) data).result().get().getAsJsonObject();
            case "skills" -> SkillsConfig.CODEC.codec().encodeStart(JsonOps.INSTANCE, (SkillsConfig) data).result().get().getAsJsonObject();
            case "perks" -> PerksConfig.CODEC.codec().encodeStart(JsonOps.INSTANCE, (PerksConfig) data).result().get().getAsJsonObject();
            case "globals" -> GlobalsConfig.CODEC.codec().encodeStart(JsonOps.INSTANCE, (GlobalsConfig) data).result().get().getAsJsonObject();
            default -> new JsonObject();
        };
        return gson.toJson(raw);
    }
}
