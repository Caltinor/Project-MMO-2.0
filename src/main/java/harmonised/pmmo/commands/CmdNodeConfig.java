package harmonised.pmmo.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.JsonOps;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.config.codecs.EnhancementsData;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.MobModifier;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.config.writers.PackGenerator;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.LogicalSide;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CmdNodeConfig {
    @FunctionalInterface
    interface CommandBuilder {
        ArgumentBuilder<CommandSourceStack, ?> build(ObjectType type) throws CommandSyntaxException;
    }
    private static final Supplier<ArgumentBuilder<CommandSourceStack, ?>> SKILL = () -> Commands.argument("skill", StringArgumentType.word());
    private static final Supplier<ArgumentBuilder<CommandSourceStack, ?>> LONG = () -> Commands.argument("value", LongArgumentType.longArg());
    private static final Supplier<ArgumentBuilder<CommandSourceStack, ?>> DOUBLE = () -> Commands.argument("value", DoubleArgumentType.doubleArg());

    private static ArgumentBuilder<CommandSourceStack, ?> CMD_SKILL_NUM(
            String command,
            ObjectType objType,
            Supplier<ArgumentBuilder<CommandSourceStack, ?>> numtype,
            BiFunction<ObjectType, CommandContext<CommandSourceStack>, Integer> execution) {
        return Commands.literal(command).then(CmdNodeConfig.SKILL.get().then(numtype.get().executes(ctx -> execution.apply(objType, ctx))));
    }
    private static ArgumentBuilder<CommandSourceStack, ?> CMD_SKILL_LONG(String cmd, ObjectType type, BiFunction<ObjectType, CommandContext<CommandSourceStack>, Integer> execution) {
        return CMD_SKILL_NUM(cmd, type, LONG, execution);
    }
    private static ArgumentBuilder<CommandSourceStack, ?> CMD_SKILL_DOUBLE(String cmd, ObjectType type, BiFunction<ObjectType, CommandContext<CommandSourceStack>, Integer> execution) {
        return CMD_SKILL_NUM(cmd, type, DOUBLE, execution);
    }

    public static ArgumentBuilder<CommandSourceStack, ?> CONFIG() throws CommandSyntaxException {
        return Commands.literal("config")
                .requires(p -> p.hasPermission(2))
                .then(object("item", Registries.ITEM, Options.ITEM, ObjectType.ITEM))
                .then(object("block", Registries.BLOCK, Options.BLOCK, ObjectType.BLOCK))
                .then(object("entity", Registries.ENTITY_TYPE, Options.ENTITY, ObjectType.ENTITY))
                .then(object("biome", Registries.BIOME, Options.LOCATION, ObjectType.BIOME))
                .then(object("dimension", Registries.DIMENSION, Options.LOCATION, ObjectType.DIMENSION))
                .then(object("enchants", Registries.ENCHANTMENT, Options.ENHANCES, ObjectType.ENCHANTMENT))
                .then(object("effects", Registries.MOB_EFFECT, Options.ENHANCES, ObjectType.EFFECT));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> object(String literal, ResourceKey<? extends Registry<?>> registry, Options[] opts, ObjectType objType) throws CommandSyntaxException {
        var regKey = ResourceKey.createRegistryKey(objType.key.location());
        var IdArg = Commands.argument("id", ResourceOrTagKeyArgument.resourceOrTagKey(regKey))
                .suggests((c,b) -> {
                    var reg = c.getSource().registryAccess().lookupOrThrow(registry);
                    List<String> values = new ArrayList<>(reg.listElementIds().map(key -> key.location().toString()).toList());
                    values.addAll(reg.listTagIds().map(key -> "#" + key.location()).toList());
                    return SharedSuggestionProvider.suggest(values, b);
                });
        for (Options opt : opts) {
            IdArg.then(opt.command.build(objType));
        }
        return Commands.literal(literal).then(IdArg);
    }

    private enum Options {
        //region REQS
        REQS((objectType) -> Commands.literal("requirement")
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((c,b) -> {
                            ReqType[] types = switch(objectType) {
                                case ITEM -> ReqType.ITEM_APPLICABLE_EVENTS;
                                case BLOCK -> ReqType.BLOCK_APPLICABLE_EVENTS;
                                case ENTITY -> ReqType.ENTITY_APPLICABLE_EVENTS;
                                case BIOME, DIMENSION -> new ReqType[]{ReqType.TRAVEL};
                                default -> new ReqType[]{};
                            };
                            return enumSuggestion(types, b);
                        })
                        .then(CMD_SKILL_LONG("set", objectType, (objType, ctx) -> {
                            var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                            ReqType type = ReqType.byName(StringArgumentType.getString(ctx, "type"));
                            String skill = StringArgumentType.getString(ctx, "skill");
                            int value = IntegerArgumentType.getInteger(ctx, "value");
                            List<ResourceLocation> ids = new ArrayList<>();
                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                            for (ResourceLocation id : ids) {
                                DataSource<?> src = loader.getData(id);
                                src.setReqs(type, Map.of(skill, value));
                                commit(objType, id, src, ctx);
                            }
                            return 0;
                        }))
                        .then(CMD_SKILL_LONG("add", objectType, (objType, ctx) -> {
                            var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                            ReqType type = ReqType.byName(StringArgumentType.getString(ctx, "type"));
                            String skill = StringArgumentType.getString(ctx, "skill");
                            int value = IntegerArgumentType.getInteger(ctx, "value");
                            List<ResourceLocation> ids = new ArrayList<>();
                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                            for (ResourceLocation id : ids) {
                                DataSource<?> src = loader.getData(id);
                                Map<String, Integer> map = new HashMap<>(src.getReqs(type, new CompoundTag()));
                                map.put(skill, value);
                                src.setReqs(type, map);
                                commit(objType, id, src, ctx);
                            }
                            return 0;
                        }))
                        .then(Commands.literal("clear").executes(ctx -> {
                            var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objectType);
                            ReqType type = ReqType.byName(StringArgumentType.getString(ctx, "type"));
                            List<ResourceLocation> ids = new ArrayList<>();
                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                            for (ResourceLocation id : ids) {
                                DataSource<?> src = loader.getData(id);
                                src.setReqs(type, Map.of());
                                commit(objectType, id, src, ctx);
                            }
                            return 0;
                        })))
        ),
        //endregion
        //region XP
        XP((objectType) -> Commands.literal("xp")
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((c,b) -> {
                            EventType[] types = switch (objectType) {
                                case ITEM -> EventType.ITEM_APPLICABLE_EVENTS;
                                case BLOCK -> EventType.BLOCK_APPLICABLE_EVENTS;
                                case ENTITY -> EventType.ENTITY_APPLICABLE_EVENTS;
                                default -> new EventType[]{};
                            };
                            return enumSuggestion(types, b);
                        })
                        .then(CMD_SKILL_LONG("set", objectType, (objType, ctx) -> {
                                var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                                EventType type = EventType.byName(StringArgumentType.getString(ctx, "type"));
                                String skill = StringArgumentType.getString(ctx, "skill");
                                long value = LongArgumentType.getLong(ctx, "value");
                                List<ResourceLocation> ids = new ArrayList<>();
                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                for (ResourceLocation id : ids) {
                                    DataSource<?> src = loader.getData(id);
                                    src.setXpValues(type, Map.of(skill, value));
                                    commit(objType, id, src, ctx);
                                }
                                return 0;
                            }))
                        .then(CMD_SKILL_LONG("add", objectType, (objType, ctx) -> {
                                var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                                EventType type = EventType.byName(StringArgumentType.getString(ctx, "type"));
                                String skill = StringArgumentType.getString(ctx, "skill");
                                long value = LongArgumentType.getLong(ctx, "value");
                                List<ResourceLocation> ids = new ArrayList<>();
                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                for (ResourceLocation id : ids) {
                                    DataSource<?> src = loader.getData(id);
                                    Map<String, Long> map = new HashMap<>(src.getXpValues(type, new CompoundTag()));
                                    map.put(skill, value);
                                    src.setXpValues(type, map);
                                    commit(objType, id, src, ctx);
                                }
                                return 0;
                            }))
                        .then(Commands.literal("clear").executes(ctx -> {
                                var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objectType);
                                List<ResourceLocation> ids = new ArrayList<>();
                                EventType type = EventType.byName(StringArgumentType.getString(ctx, "type"));
                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                for (ResourceLocation id : ids) {
                                    DataSource<?> src = loader.getData(id);
                                    src.setXpValues(type, Map.of());
                                    commit(objectType, id, src, ctx);
                                }
                                return 0;
                            })
                        )
                )
        ),
        //endregion
        //region BONUS
        BONUS((objectType) -> Commands.literal("bonus")
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((c,b) -> {
                            ModifierDataType[] types = switch (objectType) {
                                case ITEM -> new ModifierDataType[]{ModifierDataType.HELD, ModifierDataType.WORN};
                                case BIOME -> new ModifierDataType[]{ModifierDataType.BIOME};
                                case DIMENSION -> new ModifierDataType[]{ModifierDataType.DIMENSION};
                                default -> new ModifierDataType[]{};
                            };
                            return enumSuggestion(types, b);
                        })
                        .then(CMD_SKILL_DOUBLE("set", objectType, (objType, ctx) -> {
                            var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                            ModifierDataType type = ModifierDataType.byName(StringArgumentType.getString(ctx, "type"));
                            String skill = StringArgumentType.getString(ctx, "skill");
                            double value = DoubleArgumentType.getDouble(ctx, "value");
                            List<ResourceLocation> ids = new ArrayList<>();
                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                            for (ResourceLocation id : ids) {
                                DataSource<?> src = loader.getData(id);
                                src.setBonuses(type, Map.of(skill, value));
                                commit(objType, id, src, ctx);
                            }
                            return 0;
                        }))
                        .then(CMD_SKILL_DOUBLE("add", objectType, (objType, ctx) -> {
                            var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                            ModifierDataType type = ModifierDataType.byName(StringArgumentType.getString(ctx, "type"));
                            String skill = StringArgumentType.getString(ctx, "skill");
                            double value = DoubleArgumentType.getDouble(ctx, "value");
                            List<ResourceLocation> ids = new ArrayList<>();
                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                            for (ResourceLocation id : ids) {
                                DataSource<?> src = loader.getData(id);
                                Map<String, Double> map = new HashMap<>(src.getBonuses(type, new CompoundTag()));
                                map.put(skill, value);
                                src.setBonuses(type, map);
                                commit(objType, id, src, ctx);
                            }
                            return 0;
                        }))
                        .then(Commands.literal("clear").executes(ctx -> {
                            var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objectType);
                            ModifierDataType type = ModifierDataType.byName(StringArgumentType.getString(ctx, "type"));
                            List<ResourceLocation> ids = new ArrayList<>();
                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                            for (ResourceLocation id : ids) {
                                DataSource<?> src = loader.getData(id);
                                src.setBonuses(type, Map.of());
                                commit(objectType, id, src, ctx);
                            }
                            return 0;
                        }))
                )
        ),
        //endregion
        //region POS_EFFECT
        POS_EFFECT((objectType) -> Commands.literal("pos_effect")
                .then(Commands.literal("set")
                    .then(Commands.argument("effect", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.MOB_EFFECT))
                        .suggests((c,b) -> SharedSuggestionProvider.suggest(c.getSource().registryAccess()
                                .lookupOrThrow(Registries.MOB_EFFECT)
                                .listElementIds().map(key -> key.location().toString()).toList(), b))
                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objectType);
                                    ResourceLocation effect = ResourceLocationArgument.getId(ctx, "effect");
                                    int value = IntegerArgumentType.getInteger(ctx, "level");
                                    List<ResourceLocation> ids = new ArrayList<>();
                                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                    for (ResourceLocation id : ids) {
                                        DataSource<?> src = loader.getData(id);
                                        var currentEffects = new HashMap<>(src.getPositiveEffect());
                                        currentEffects.put(effect, value);
                                        src.setPositiveEffects(currentEffects);
                                        commit(objectType, id, src, ctx);
                                    }
                                    return 0;
                                })
                        )
                    )
                )
                .then(Commands.literal("clear").executes(ctx -> {
                    var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objectType);
                    List<ResourceLocation> ids = new ArrayList<>();
                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                    for (ResourceLocation id : ids) {
                        DataSource<?> src = loader.getData(id);
                        src.setPositiveEffects(Map.of());
                        commit(objectType, id, src, ctx);
                    }
                    return 0;
                }))
        ),
        //endregion
        //region NEG_EFFECT
        NEG_EFFECT((objectType) -> Commands.literal("neg_effect")
                .then(Commands.literal("set")
                        .then(Commands.argument("effect", ResourceLocationArgument.id())
                                .suggests((c,b) -> SharedSuggestionProvider.suggest(c.getSource().registryAccess()
                                        .lookupOrThrow(Registries.MOB_EFFECT)
                                        .listElementIds().map(key -> key.location().toString()).toList(), b))
                                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objectType);
                                            ResourceLocation effect = ResourceLocationArgument.getId(ctx, "effect");
                                            int value = IntegerArgumentType.getInteger(ctx, "level");
                                            List<ResourceLocation> ids = new ArrayList<>();
                                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                            for (ResourceLocation id : ids) {
                                                DataSource<?> src = loader.getData(id);
                                                var currentEffects = new HashMap<>(src.getNegativeEffect());
                                                currentEffects.put(effect, value);
                                                src.setNegativeEffects(currentEffects);
                                                commit(objectType, id, src, ctx);
                                            }
                                            return 0;
                                        })
                                )
                        )
                )
                .then(Commands.literal("clear").executes(ctx -> {
                    var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objectType);
                    List<ResourceLocation> ids = new ArrayList<>();
                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                    for (ResourceLocation id : ids) {
                        DataSource<?> src = loader.getData(id);
                        src.setNegativeEffects(Map.of());
                        commit(objectType, id, src, ctx);
                    }
                    return 0;
                }))
        ),
        //endregion
        //region DAMAGE_XP
        DAMAGE_XP((objectType) -> Commands.literal("damage_xp")
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((c,b) -> enumSuggestion(new EventType[]{EventType.DEAL_DAMAGE, EventType.RECEIVE_DAMAGE}, b))
                        .then(Commands.argument("damage_type", ResourceLocationArgument.id())
                                .suggests((c,b) -> SharedSuggestionProvider.suggest(c.getSource().registryAccess()
                                            .lookupOrThrow(Registries.DAMAGE_TYPE)
                                            .listElementIds().map(key -> key.location().toString()).toList(), b)
                                )
                                .then(CMD_SKILL_LONG("set", objectType, (objType, ctx) -> {
                                    var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                                    String dmgType = ResourceLocationArgument.getId(ctx, "damage_type").toString();
                                    EventType type = EventType.byName(StringArgumentType.getString(ctx, "type"));
                                    String skill = StringArgumentType.getString(ctx, "skill");
                                    long value = LongArgumentType.getLong(ctx, "value");
                                    List<ResourceLocation> ids = new ArrayList<>();
                                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                    for (ResourceLocation id : ids) {
                                        ObjectData src = (ObjectData) loader.getData(id);
                                        Map<String, Map<String, Long>> map = new HashMap<>(src.damageXpValues().getOrDefault(type, new HashMap<>()));
                                        map.put(dmgType, Map.of(skill, value));
                                        src.damageXpValues().put(type, map);
                                        commit(objType, id, src, ctx);
                                    }
                                    return 0;
                                }))
                                .then(CMD_SKILL_LONG("add", objectType, (objType, ctx) -> {
                                    var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                                    String dmgType = ResourceLocationArgument.getId(ctx, "damage_type").toString();
                                    EventType type = EventType.byName(StringArgumentType.getString(ctx, "type"));
                                    String skill = StringArgumentType.getString(ctx, "skill");
                                    long value = LongArgumentType.getLong(ctx, "value");
                                    List<ResourceLocation> ids = new ArrayList<>();
                                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                    for (ResourceLocation id : ids) {
                                        ObjectData src = (ObjectData) loader.getData(id);
                                        Map<String, Map<String, Long>> map = new HashMap<>(src.damageXpValues().getOrDefault(type, new HashMap<>()));
                                        Map<String, Long> inner = new HashMap<>(map.getOrDefault(dmgType, new HashMap<>()));
                                        inner.put(skill, value);
                                        map.put(dmgType, inner);
                                        src.damageXpValues().put(type, map);
                                        commit(objType, id, src, ctx);
                                    }
                                    return 0;
                                }))
                                .then(Commands.literal("clear").executes(ctx -> {
                                    var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objectType);
                                    String dmgType = ResourceLocationArgument.getId(ctx, "damage_type").toString();
                                    EventType type = EventType.byName(StringArgumentType.getString(ctx, "type"));
                                    List<ResourceLocation> ids = new ArrayList<>();
                                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                    for (ResourceLocation id : ids) {
                                        ObjectData src = (ObjectData) loader.getData(id);
                                        Map<String, Map<String, Long>> map = new HashMap<>(src.damageXpValues().getOrDefault(type, new HashMap<>()));
                                        map.remove(dmgType);
                                        src.damageXpValues().put(type, map);
                                        commit(objectType, id, src, ctx);
                                    }
                                    return 0;
                                }))
                        )
                )
        ),
        //endregion
        //region SALVAGE
        SALVAGE((objectType) -> Commands.literal("salvage")
                .then(Commands.literal("set")
                        .then(Commands.argument("drop", ResourceLocationArgument.id())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(ctx.getSource().registryAccess()
                                        .lookupOrThrow(Registries.ITEM).listElementIds().map(key -> key.location().toString()).toList(), b)
                                )
                                .then(Commands.literal("chance_per_level")
                                        .then(CMD_SKILL_DOUBLE("set", objectType, (objType, ctx) -> {
                                                var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                String skill = StringArgumentType.getString(ctx, "skill");
                                                double value = DoubleArgumentType.getDouble(ctx, "value");
                                                List<ResourceLocation> ids = new ArrayList<>();
                                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                for (ResourceLocation id : ids) {
                                                    ObjectData src = loader.getData(id);
                                                    CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(src.salvage().get(drop))
                                                            .setChancePerLevel(Map.of(skill, value)).build();
                                                    src.salvageRaw().put(drop, data);
                                                    commit(objType, id, src, ctx);
                                                }
                                                return 0;
                                            }))
                                        .then(CMD_SKILL_DOUBLE("add", objectType, (objType, ctx) -> {
                                                var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                String skill = StringArgumentType.getString(ctx, "skill");
                                                double value = DoubleArgumentType.getDouble(ctx, "value");
                                                List<ResourceLocation> ids = new ArrayList<>();
                                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                for (ResourceLocation id : ids) {
                                                    ObjectData src = loader.getData(id);
                                                    CodecTypes.SalvageData data = src.salvage().getOrDefault(drop, APIUtils.SalvageBuilder.start().build());
                                                    data.chancePerLevel().put(skill, value);
                                                    src.salvageRaw().put(drop, data);
                                                    commit(objType, id, src, ctx);
                                                }
                                                return 0;
                                            }))
                                        .then(Commands.literal("clear").executes(ctx -> {
                                            var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                            ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                            List<ResourceLocation> ids = new ArrayList<>();
                                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                            for (ResourceLocation id : ids) {
                                                ObjectData src = loader.getData(id);
                                                CodecTypes.SalvageData original = src.salvage().getOrDefault(drop, APIUtils.SalvageBuilder.start().build());
                                                CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(original)
                                                        .setChancePerLevel(Map.of()).build();
                                                src.salvageRaw().put(drop, data);
                                                commit(objectType, id, src, ctx);
                                            }
                                            return 0;
                                        }))
                                )
                                .then(Commands.literal("requirement")
                                        .then(CMD_SKILL_LONG("set", objectType, (objType, ctx) -> {
                                                var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                String skill = StringArgumentType.getString(ctx, "skill");
                                                int value = IntegerArgumentType.getInteger(ctx, "value");
                                                List<ResourceLocation> ids = new ArrayList<>();
                                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                for (ResourceLocation id : ids) {
                                                    ObjectData src = loader.getData(id);
                                                    CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(src.salvage().get(drop))
                                                            .setLevelReq(Map.of(skill, value)).build();
                                                    src.salvageRaw().put(drop, data);
                                                    commit(objType, id, src, ctx);
                                                }
                                                return 0;
                                            }))
                                        .then(CMD_SKILL_LONG("add", objectType, (objType, ctx) -> {
                                                var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                String skill = StringArgumentType.getString(ctx, "skill");
                                                int value = IntegerArgumentType.getInteger(ctx, "value");
                                                List<ResourceLocation> ids = new ArrayList<>();
                                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                for (ResourceLocation id : ids) {
                                                    ObjectData src = loader.getData(id);
                                                    CodecTypes.SalvageData data = src.salvage().getOrDefault(drop, APIUtils.SalvageBuilder.start().build());
                                                    data.levelReq().put(skill, value);
                                                    src.salvageRaw().put(drop, data);
                                                    commit(objType, id, src, ctx);
                                                }
                                                return 0;
                                            }))
                                        .then(Commands.literal("clear").executes(ctx -> {
                                            var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                            ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                            List<ResourceLocation> ids = new ArrayList<>();
                                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                            for (ResourceLocation id : ids) {
                                                ObjectData src = loader.getData(id);
                                                CodecTypes.SalvageData original = src.salvage().getOrDefault(drop, APIUtils.SalvageBuilder.start().build());
                                                CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(original)
                                                        .setLevelReq(Map.of()).build();
                                                src.salvageRaw().put(drop, data);
                                                commit(objectType, id, src, ctx);
                                            }
                                            return 0;
                                        }))
                                )
                                .then(Commands.literal("xp")
                                        .then(CMD_SKILL_LONG("set", objectType, (objType, ctx) -> {
                                                var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                String skill = StringArgumentType.getString(ctx, "skill");
                                                long value = LongArgumentType.getLong(ctx, "value");
                                                List<ResourceLocation> ids = new ArrayList<>();
                                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                for (ResourceLocation id : ids) {
                                                    ObjectData src = loader.getData(id);
                                                    CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(src.salvage().get(drop))
                                                            .setXpAward(Map.of(skill, value)).build();
                                                    src.salvageRaw().put(drop, data);
                                                    commit(objType, id, src, ctx);
                                                }
                                                return 0;
                                            }))
                                        .then(CMD_SKILL_LONG("add", objectType, (objType, ctx) -> {
                                                var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                String skill = StringArgumentType.getString(ctx, "skill");
                                                long value = LongArgumentType.getLong(ctx, "value");
                                                List<ResourceLocation> ids = new ArrayList<>();
                                                try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                for (ResourceLocation id : ids) {
                                                    ObjectData src = loader.getData(id);
                                                    CodecTypes.SalvageData data = src.salvage().getOrDefault(drop, APIUtils.SalvageBuilder.start().build());
                                                    data.xpAward().put(skill, value);
                                                    src.salvageRaw().put(drop, data);
                                                    commit(objType, id, src, ctx);
                                                }
                                                return 0;
                                            }))
                                        .then(Commands.literal("clear").executes(ctx -> {
                                            var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                            ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                            List<ResourceLocation> ids = new ArrayList<>();
                                            try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                            for (ResourceLocation id : ids) {
                                                ObjectData src = loader.getData(id);
                                                CodecTypes.SalvageData original = src.salvage().getOrDefault(drop, APIUtils.SalvageBuilder.start().build());
                                                CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(original)
                                                        .setXpAward(Map.of()).build();
                                                src.salvageRaw().put(drop, data);
                                                commit(objectType, id, src, ctx);
                                            }
                                            return 0;
                                        }))
                                )
                                .then(Commands.literal("max_drops")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                    ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                    int value = IntegerArgumentType.getInteger(ctx, "value");
                                                    List<ResourceLocation> ids = new ArrayList<>();
                                                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                    for (ResourceLocation id : ids) {
                                                        ObjectData src = loader.getData(id);
                                                        CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(src.salvage().get(drop))
                                                                .setSalvageMax(value).build();
                                                        src.salvageRaw().put(drop, data);
                                                        commit(objectType, id, src, ctx);
                                                    }
                                                    return 0;
                                                })
                                        )
                                )
                                .then(Commands.literal("base_chance")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                                .executes(ctx -> {
                                                    var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                    ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                    double value = DoubleArgumentType.getDouble(ctx, "value");
                                                    List<ResourceLocation> ids = new ArrayList<>();
                                                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                    for (ResourceLocation id : ids) {
                                                        ObjectData src = loader.getData(id);
                                                        CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(src.salvage().get(drop))
                                                                .setBaseChance(value).build();
                                                        src.salvageRaw().put(drop, data);
                                                        commit(objectType, id, src, ctx);
                                                    }
                                                    return 0;
                                                })
                                        )
                                )
                                .then(Commands.literal("max_chance")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0, 1.0))
                                                .executes(ctx -> {
                                                    var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                                    ResourceLocation drop = ResourceLocationArgument.getId(ctx, "drop");
                                                    double value = DoubleArgumentType.getDouble(ctx, "value");
                                                    List<ResourceLocation> ids = new ArrayList<>();
                                                    try {ids = readIDs(ctx, objectType);} catch(CommandSyntaxException ignored) {}
                                                    for (ResourceLocation id : ids) {
                                                        ObjectData src = loader.getData(id);
                                                        CodecTypes.SalvageData data = APIUtils.SalvageBuilder.from(src.salvage().get(drop))
                                                                .setMaxChance(value).build();
                                                        src.salvageRaw().put(drop, data);
                                                        commit(objectType, id, src, ctx);
                                                    }
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("clear"))
        ),
        //endregion
        //region VEIN
        VEIN((objType) -> {
            var root = Commands.literal("vein");
            switch (objType) {
                case ITEM -> {
                    root
                    .then(Commands.literal("charge_rate")
                            .then(Commands.argument("rate", DoubleArgumentType.doubleArg(0.0))
                                    .executes(ctx -> {
                                        var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                        double rate = DoubleArgumentType.getDouble(ctx, "rate");
                                        List<ResourceLocation> ids = new ArrayList<>();
                                        try {ids = readIDs(ctx, objType);} catch(CommandSyntaxException ignored) {}
                                        for (ResourceLocation id : ids) {
                                            ObjectData src = loader.getData(id);
                                            src.veinData().replaceWith(new VeinData(src.veinData().chargeCap, Optional.of(rate), Optional.empty()));
                                            commit(objType, id, src, ctx);
                                        }
                                        return 0;
                                    })
                            )
                    )
                    .then(Commands.literal("capacity")
                            .then(Commands.argument("cap", IntegerArgumentType.integer(0))
                                    .executes(ctx -> {
                                        var loader = Core.get(LogicalSide.SERVER).getLoader().ITEM_LOADER;
                                        int cap = IntegerArgumentType.getInteger(ctx, "cap");
                                        List<ResourceLocation> ids = new ArrayList<>();
                                        try {ids = readIDs(ctx, objType);} catch(CommandSyntaxException ignored) {}
                                        for (ResourceLocation id : ids) {
                                            ObjectData src = loader.getData(id);
                                            src.veinData().replaceWith(new VeinData(Optional.of(cap), src.veinData().chargeRate, Optional.empty()));
                                            commit(objType, id, src, ctx);
                                        }
                                        return 0;
                                    })
                            )
                    );
                }
                case BLOCK -> {
                    root.then(Commands.argument("consume_amount", IntegerArgumentType.integer(1)).executes(ctx -> {
                        var loader = Core.get(LogicalSide.SERVER).getLoader().BLOCK_LOADER;
                        int consumeAmount = IntegerArgumentType.getInteger(ctx, "consume_amount");
                        List<ResourceLocation> ids = new ArrayList<>();
                        try {ids = readIDs(ctx, objType);} catch(CommandSyntaxException ignored) {}
                        for (ResourceLocation id : ids) {
                            ObjectData src = loader.getData(id);
                            src.veinData().replaceWith(new VeinData(Optional.empty(), Optional.empty(), Optional.of(consumeAmount)));
                            commit(objType, id, src, ctx);
                        }
                        return 0;
                    }));
                }
                default -> {}
            }
            return root;
        }),
        //endregion
        //region MOBS
        MOBS((objType) -> Commands.literal("mob_scaling")
                .then(Commands.literal("clear").executes(ctx -> {
                    var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                    List<ResourceLocation> ids = new ArrayList<>();
                    try {ids = readIDs(ctx, objType);} catch(CommandSyntaxException ignored) {}
                    for (ResourceLocation id : ids) {
                        LocationData src = (LocationData) loader.getData(id);
                        src.mobModifiers().clear();
                        commit(objType, id, src, ctx);
                    }
                    return 0;
                    })
                )
                .then(Commands.literal("add")
                    .then(Commands.argument("entity", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.ENTITY_TYPE))
                            .suggests((c,b) -> {
                                var reg = c.getSource().registryAccess().lookupOrThrow(Registries.ENTITY_TYPE);
                                List<String> values = new ArrayList<>(reg.listElementIds().map(key -> key.location().toString()).toList());
                                values.addAll(reg.listTagIds().map(key -> "#" + key.location()).toList());
                                return SharedSuggestionProvider.suggest(values, b);
                            })
                            .then(Commands.literal("add")
                                .then(Commands.argument("attribute", ResourceLocationArgument.id())
                                        .suggests((c,b) -> SharedSuggestionProvider.suggest(c.getSource().registryAccess().lookupOrThrow(Registries.ATTRIBUTE).listElementIds().map(key -> key.location().toString()).toList(), b))
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("operation", StringArgumentType.word())
                                                        .suggests((c,b) -> SharedSuggestionProvider.suggest(Arrays.stream(AttributeModifier.Operation.values()).map(o -> o.name().toLowerCase()).toList(), b))
                                                        .executes(ctx -> {
                                                            var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                                                            ResourceLocation attribute = ResourceLocationArgument.getId(ctx, "attribute");
                                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                                            AttributeModifier.Operation operation = switch (StringArgumentType.getString(ctx, "operation")) {
                                                                case "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
                                                                case "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
                                                                default -> AttributeModifier.Operation.ADDITION;
                                                            };
                                                            List<ResourceLocation> ids = new ArrayList<>();
                                                            List<ResourceLocation> mobs = new ArrayList<>();
                                                            try {
                                                                ids = readIDs(ctx, objType);
                                                                mobs = readWithTags("entity", ctx, ObjectType.ENTITY);
                                                            } catch(CommandSyntaxException ignored) {}
                                                            for (ResourceLocation id : ids) {
                                                                LocationData src = (LocationData) loader.getData(id);
                                                                for (ResourceLocation entity : mobs) {
                                                                    List<MobModifier> currentMods = new ArrayList<>(src.mobModifiers().getOrDefault(entity, new ArrayList<>()));
                                                                    currentMods.add(new MobModifier(attribute, amount, operation));
                                                                    src.mobModifiers().put(entity, currentMods);
                                                                    commit(objType, id, src, ctx);
                                                                }
                                                            }
                                                            return 0;
                                                        })
                                                )
                                        )
                                )
                            )
                            .then(Commands.literal("clear").executes(ctx -> {
                                var loader = Core.get(LogicalSide.SERVER).getLoader().getLoader(objType);
                                List<ResourceLocation> ids = new ArrayList<>();
                                List<ResourceLocation> mobs = new ArrayList<>();
                                try {
                                    ids = readIDs(ctx, objType);
                                    mobs = readWithTags("entity", ctx, ObjectType.ENTITY);
                                } catch(CommandSyntaxException ignored) {}
                                for (ResourceLocation id : ids) {
                                    LocationData src = (LocationData) loader.getData(id);
                                    for (ResourceLocation entity : mobs) {
                                        src.mobModifiers().getOrDefault(entity, new ArrayList<>()).clear();
                                        commit(objType, id, src, ctx);
                                    }
                                }
                                return 0;
                                })
                            )
                    )
                )

        );
        //endregion
        public final CommandBuilder command;
        Options(CommandBuilder command) {this.command = command;}
        public static final Options[] ITEM = new Options[]{REQS, XP, BONUS, NEG_EFFECT, DAMAGE_XP, SALVAGE, VEIN};
        public static final Options[] BLOCK = new Options[]{REQS, XP, VEIN};
        public static final Options[] ENTITY = new Options[]{REQS, XP, DAMAGE_XP};
        public static final Options[] LOCATION = new Options[]{REQS, BONUS, POS_EFFECT, NEG_EFFECT, MOBS};
        public static final Options[] ENHANCES = new Options[]{XP};
    }

    private static void commit(ObjectType objType, ResourceLocation id, DataSource<?> src, CommandContext<CommandSourceStack> ctx) {
        Networking.sendToClient(new CP_SyncData(objType, Map.of(id, src)), ctx.getSource().getPlayer());
        writeFile(ctx.getSource().getServer(), src, id, objType);
    }

    private static CompletableFuture<Suggestions> enumSuggestion(Enum<?>[] src, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Arrays.stream(src).map(Enum::toString), builder);
    }

    private static List<ResourceLocation> readIDs(CommandContext<CommandSourceStack> ctx, ObjectType objectType) throws CommandSyntaxException {
        return readWithTags("id", ctx, objectType);
    }
    private static List<ResourceLocation> readWithTags(String id, CommandContext<CommandSourceStack> ctx, ObjectType objectType) throws CommandSyntaxException {
        List<ResourceLocation> ids = new ArrayList<>();
        var error = new DynamicCommandExceptionType(err -> Component.literal("Invalid argument id/tag in command: ").append(err.toString()));
        String rawID = switch (objectType) {
            case ITEM -> ResourceOrTagKeyArgument.getResourceOrTagKey(ctx, id, Registries.ITEM, error).asPrintable();
            case BLOCK -> ResourceOrTagKeyArgument.getResourceOrTagKey(ctx, id, Registries.BLOCK, error).asPrintable();
            case ENTITY, PLAYER -> ResourceOrTagKeyArgument.getResourceOrTagKey(ctx, id, Registries.ENTITY_TYPE, error).asPrintable();
            case BIOME -> ResourceOrTagKeyArgument.getResourceOrTagKey(ctx, id, Registries.BIOME, error).asPrintable();
            case DIMENSION -> ResourceOrTagKeyArgument.getResourceOrTagKey(ctx, id, Registries.DIMENSION, error).asPrintable();
            case ENCHANTMENT -> ResourceOrTagKeyArgument.getResourceOrTagKey(ctx, id, Registries.ENCHANTMENT, error).asPrintable();
            case EFFECT -> ResourceOrTagKeyArgument.getResourceOrTagKey(ctx, id, Registries.MOB_EFFECT, error).asPrintable();
        };
        if (rawID.startsWith("#")) {
            String key = rawID.substring(1);
            TagKey<?> tagKey = TagKey.create(ResourceKey.createRegistryKey(objectType.key.location()), ResourceLocation.parse(key));
            ctx.getSource().registryAccess().lookupOrThrow(objectType.key)
                    .get((TagKey<Object>) tagKey).get()
                    .forEach(holder -> ids.add(holder.unwrapKey().get().location()));
        }
        else
            ids.add(ResourceLocation.parse(rawID));
        return ids;
    }

    private static void writeFile(MinecraftServer server, DataSource<?> src, ResourceLocation id, ObjectType type) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //Create base pack if not exists
        Path packPath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("command_pack").resolve("pack.mcmeta");
        if (!Files.exists(packPath)) generatePack(server, gson);
        //3. write files with path
        PackGenerator.Category category = type.category;
        int index = id.getPath().lastIndexOf('/');
        String pathRoute = id.getPath().substring(0, Math.max(index, 0));
        Path finalPath = server.getWorldPath(LevelResource.DATAPACK_DIR)
                .resolve("command_pack")
                .resolve("data/"+id.getNamespace()+"/"+category.route+"/"+pathRoute);
        finalPath.toFile().mkdirs();
        try {
            Files.writeString(
                    finalPath.resolve(id.getPath().substring(id.getPath().lastIndexOf('/')+1)+".json"),
                    srcToString(src, category, gson),
                    Charset.defaultCharset(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {System.out.println("Error While Generating Pack File For: "+id.toString()+" ("+e.toString()+")");}
    }

    private static void generatePack(MinecraftServer server, Gson gson) {
        Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("command_pack");
        filepath.toFile().mkdirs();
        Path packPath = filepath.resolve("pack.mcmeta");
        try {
            Files.writeString(
                    packPath,
                    gson.toJson(PackGenerator.getPackObject(false)),
                    Charset.defaultCharset(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {System.out.println("Error While Generating pack.mcmeta for Command Data: "+e.toString());}
    }

    private static String srcToString(DataSource<?> data, PackGenerator.Category type, Gson gson) {
        JsonObject raw = switch (type) {
            case ITEMS, BLOCKS, ENTITIES -> {
                ObjectData overridden = new ObjectData(true).combine((ObjectData) data);
                yield ObjectData.CODEC.encodeStart(JsonOps.INSTANCE, overridden).result().get().getAsJsonObject();
            }
            case BIOMES, DIMENSIONS -> {
                LocationData overridden = new LocationData(true).combine((LocationData) data);
                yield LocationData.CODEC.encodeStart(JsonOps.INSTANCE, overridden).result().get().getAsJsonObject();
            }
            case ENCHANTMENTS, EFFECTS -> {
                EnhancementsData overridden = new EnhancementsData(true, ((EnhancementsData)data).skillArray());
                yield EnhancementsData.CODEC.encodeStart(JsonOps.INSTANCE, overridden).result().get().getAsJsonObject();
            }
            default -> new JsonObject();
        };
        return gson.toJson(raw);
    }
}
