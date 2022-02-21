package harmonised.pmmo.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncData_ClearXp;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;

public class CmdNodeAdmin {
	private static final String SKILL_ARG = "Skill Name";
	private static final String TARGET_ARG = "Target";
	private static final String TYPE_ARG = "Change Type";
	private static final String VALUE_ARG = "New Value";

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("admin")
				.requires(p -> p.hasPermission(2))
				.then(Commands.argument(TARGET_ARG, EntityArgument.players())
						.then(Commands.literal("set")
								.then(Commands.argument(SKILL_ARG, StringArgumentType.word())
										.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Core.get(LogicalSide.SERVER).getDataConfig().getSkillData().keySet(), builder))
										.then(Commands.argument(TYPE_ARG, StringArgumentType.word())
												.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("level", "xp"), builder))
												.then(Commands.argument(VALUE_ARG, LongArgumentType.longArg())
														.executes(ctx -> adminSetOrAdd(ctx, true))))))
						.then(Commands.literal("add")
								.then(Commands.argument(SKILL_ARG, StringArgumentType.word())
										.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Core.get(LogicalSide.SERVER).getDataConfig().getSkillData().keySet(), builder))
										.then(Commands.argument(TYPE_ARG, StringArgumentType.word())
												.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("level", "xp"), builder))
												.then(Commands.argument(VALUE_ARG, LongArgumentType.longArg())
														.executes(ctx -> adminSetOrAdd(ctx, false))))))
						.then(Commands.literal("clear")
								.executes(ctx -> adminClear(ctx)))
						.executes(ctx -> displayPlayer(ctx)));
	}
	
	public static int adminSetOrAdd(CommandContext<CommandSourceStack> ctx, boolean isSet) throws CommandSyntaxException {
		Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, TARGET_ARG);
		String skillName = StringArgumentType.getString(ctx, SKILL_ARG);
		boolean isLevel = StringArgumentType.getString(ctx, TYPE_ARG).equalsIgnoreCase("level");
		Long value = LongArgumentType.getLong(ctx, VALUE_ARG);
		IDataStorage data = Core.get(LogicalSide.SERVER).getData();
		
		for (ServerPlayer player : players) {
			if (isSet) {
				if (isLevel) {
					data.setPlayerSkillLevel(skillName, player.getUUID(), value.intValue());
					ctx.getSource().sendSuccess(new TranslatableComponent("pmmo.setLevel", skillName, value, player.getName()), true);
				}
				else {
					data.setXpRaw(player.getUUID(), skillName, value);
					ctx.getSource().sendSuccess(new TranslatableComponent("pmmo.setXp", skillName, value, player.getName()), true);
				}
			}
			else {
				if (isLevel) {
					data.changePlayerSkillLevel(skillName, player.getUUID(), value.intValue());
					ctx.getSource().sendSuccess(new TranslatableComponent("pmmo.addLevel", skillName, value, player.getName()), true);
				}
				else {
					data.setXpDiff(player.getUUID(), skillName, value);
					ctx.getSource().sendSuccess(new TranslatableComponent("pmmo.addXp", skillName, value, player.getName()), true);
				}
			}
		}
		return 0;
	}
	
	public static int adminClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {	
		IDataStorage data = Core.get(LogicalSide.SERVER).getData();
		for (ServerPlayer player : EntityArgument.getPlayers(ctx, TARGET_ARG)) {
			data.setXpMap(player.getUUID(), new HashMap<>());
			Networking.sendToClient(new CP_SyncData_ClearXp(), player);
		}
		return 0;
	}
	
	public static int displayPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		//TODO replace with a gui
		IDataStorage data = Core.get(LogicalSide.SERVER).getData();
		for (ServerPlayer player : EntityArgument.getPlayers(ctx, TARGET_ARG)) {
			ctx.getSource().sendSuccess(player.getName(), false);
			for (Map.Entry<String, Long> skillMap : data.getXpMap(player.getUUID()).entrySet()) {
				int level = data.getLevelFromXP(skillMap.getValue());
				ctx.getSource().sendSuccess(new TextComponent(skillMap.getKey()+": "+level+" | "+skillMap.getValue()), false);
			}
		}
		return 0;
	}
}
