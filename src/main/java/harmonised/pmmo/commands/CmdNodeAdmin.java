package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.PlayerData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncData;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.storage.Experience;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.fml.LogicalSide;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CmdNodeAdmin {
	private static final String SKILL_ARG = "Skill Name";
	private static final String TARGET_ARG = "Target";
	private static final String TYPE_ARG = "Change Type";
	private static final String VALUE_ARG = "New Value";
	private static final SuggestionProvider<CommandSourceStack> SKILL_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(Config.skills().skills().keySet(), builder);

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("admin")
				.requires(p -> p.hasPermission(2))
				.then(Commands.argument(TARGET_ARG, EntityArgument.players())
						.then(Commands.literal("set")
								.then(Commands.argument(SKILL_ARG, StringArgumentType.word())
										.suggests(SKILL_SUGGESTIONS)
										.then(Commands.argument(TYPE_ARG, StringArgumentType.word())
												.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("level", "xp"), builder))
												.then(Commands.argument(VALUE_ARG, LongArgumentType.longArg())
														.executes(ctx -> adminSetOrAdd(ctx, true))))))
						.then(Commands.literal("add")
								.then(Commands.argument(SKILL_ARG, StringArgumentType.word())
										.suggests(SKILL_SUGGESTIONS)
										.then(Commands.argument(TYPE_ARG, StringArgumentType.word())
												.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("level", "xp"), builder))
												.then(Commands.argument(VALUE_ARG, LongArgumentType.longArg())
														.executes(ctx -> adminSetOrAdd(ctx, false))))))
						.then(Commands.literal("clear")
								.executes(CmdNodeAdmin::adminClear)
								.then(Commands.argument(SKILL_ARG, StringArgumentType.word())
										.suggests(SKILL_SUGGESTIONS)
										.executes(CmdNodeAdmin::adminClearSkill)))
						.then(Commands.literal("attributes")
								.then(Commands.literal("refresh")
										.executes(CmdNodeAdmin::rebuildAttributes))
								.then(Commands.literal("clear")
										.executes(CmdNodeAdmin::clearAttributes)))
						.then(Commands.literal("ignoreReqs")
								.executes(CmdNodeAdmin::exemptAdmin))
						.then(Commands.literal("adminBonus")
								.then(Commands.argument(SKILL_ARG, StringArgumentType.word())
										.suggests(SKILL_SUGGESTIONS)
										.then(Commands.argument(VALUE_ARG, DoubleArgumentType.doubleArg(0.0))
												.executes(CmdNodeAdmin::adminBonuses))))
						.executes(CmdNodeAdmin::displayPlayer));
	}
	
	public static int adminSetOrAdd(CommandContext<CommandSourceStack> ctx, boolean isSet) throws CommandSyntaxException {
		Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, TARGET_ARG);
		String skillName = StringArgumentType.getString(ctx, SKILL_ARG);
		boolean isLevel = StringArgumentType.getString(ctx, TYPE_ARG).equalsIgnoreCase("level");
		long value = LongArgumentType.getLong(ctx, VALUE_ARG);
		IDataStorage data = Core.get(LogicalSide.SERVER).getData();
		
		for (ServerPlayer player : players) {
			Experience exp = data.getXpMap(player.getUUID()).computeIfAbsent(skillName, s -> new Experience());
			if (isSet) {
				if (isLevel) {
					exp.setLevel(value);
					ctx.getSource().sendSuccess(() -> LangProvider.SET_LEVEL.asComponent(skillName, value, player.getName()), true);
				}
				else {
					exp.setXp(value);
					ctx.getSource().sendSuccess(() -> LangProvider.SET_XP.asComponent(skillName, value, player.getName()), true);
				}
			}
			else {
				if (isLevel) {
					exp.addLevel(value);
					ctx.getSource().sendSuccess(() -> LangProvider.ADD_LEVEL.asComponent(skillName, value, player.getName()), true);
				}
				else {
					exp.addXp(value);
					ctx.getSource().sendSuccess(() -> LangProvider.ADD_XP.asComponent(skillName, value, player.getName()), true);
				}
			}
			Networking.sendToClient(new CP_UpdateExperience(skillName, exp, 0), player);
		}
		return 0;
	}
	
	public static int adminClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {	
		IDataStorage data = Core.get(LogicalSide.SERVER).getData();
		for (ServerPlayer player : EntityArgument.getPlayers(ctx, TARGET_ARG)) {
			data.setXpMap(player.getUUID(), new HashMap<>());
			Networking.sendToClient(new CP_SyncData_ClearXp(""), player);
		}
		return 0;
	}
	public static int adminClearSkill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		IDataStorage data = Core.get(LogicalSide.SERVER).getData();
		String specifiedSkill = StringArgumentType.getString(ctx, SKILL_ARG);
		for (ServerPlayer player : EntityArgument.getPlayers(ctx, TARGET_ARG)) {
			data.getXpMap(player.getUUID()).remove(specifiedSkill);
			Networking.sendToClient(new CP_SyncData_ClearXp(specifiedSkill), player);
		}
		return 0;
	}

	public static int rebuildAttributes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		for (ServerPlayer player : EntityArgument.getPlayers(ctx, TARGET_ARG)) {
			Core.get(LogicalSide.SERVER).getPerkRegistry().executePerkFiltered(EventType.SKILL_UP, player, "perk", "pmmo:attribute", new CompoundTag());
		}
		return 0;
	}
	public static int clearAttributes(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		for (ServerPlayer player : EntityArgument.getPlayers(ctx, TARGET_ARG)) {
			player.getAttributes().attributes.values().forEach(instance -> {
				List<ResourceLocation> ids = instance.getModifiers().stream().filter(mod -> mod.id().getPath().startsWith("perk/")).map(AttributeModifier::id).toList();
				ids.forEach(instance::removeModifier);
			});
		}
		return 0;
	}
	
	public static int displayPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		//TODO replace with a gui
		IDataStorage data = Core.get(LogicalSide.SERVER).getData();
		for (ServerPlayer player : EntityArgument.getPlayers(ctx, TARGET_ARG)) {
			ctx.getSource().sendSuccess(player::getName, false);
			for (Map.Entry<String, Experience> skillMap : data.getXpMap(player.getUUID()).entrySet()) {
				long level = skillMap.getValue().getLevel().getLevel();
				ctx.getSource().sendSuccess(() -> Component.literal(skillMap.getKey()+": "+level+" | "+skillMap.getValue()), false);
			}
		}
		return 0;
	}

	public static int exemptAdmin(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Core core = Core.get(ctx.getSource().getLevel());
		ServerPlayer player = EntityArgument.getPlayer(ctx, TARGET_ARG);
		ResourceLocation playerID = Reference.mc(player.getUUID().toString());
		PlayerData existing = core.getLoader().PLAYER_LOADER.getData().get(playerID);
		boolean exists = existing != null;
		PlayerData updated = new PlayerData(true, !exists || !existing.ignoreReq(), exists ? existing.bonuses() : Map.of());
		core.getLoader().PLAYER_LOADER.getData().put(playerID, updated);
		Networking.sendToClient(new CP_SyncData(ObjectType.PLAYER, core.getLoader().PLAYER_LOADER.getData()), player);
		return 0;
	}

	public static int adminBonuses(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		double bonus = DoubleArgumentType.getDouble(ctx, VALUE_ARG);
		String skill = StringArgumentType.getString(ctx, SKILL_ARG);

		Core core = Core.get(ctx.getSource().getLevel());
		ServerPlayer player = EntityArgument.getPlayer(ctx, TARGET_ARG);
		ResourceLocation playerID = Reference.mc(player.getUUID().toString());
		PlayerData existing = core.getLoader().PLAYER_LOADER.getData().get(playerID);
		boolean exists = existing != null;
		Map<String, Double> bonuses = exists ? new HashMap<>(existing.bonuses()) : new HashMap<>();
		bonuses.put(skill, bonus);
		if (skill.equals("clear"))
			bonuses.clear();
		PlayerData updated = new PlayerData(true, !exists || !existing.ignoreReq(), bonuses);
		core.getLoader().PLAYER_LOADER.getData().put(playerID, updated);
		Networking.sendToClient(new CP_SyncData(ObjectType.PLAYER, core.getLoader().PLAYER_LOADER.getData()), player);
		return 0;
	}
}
