package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.config.writers.PackGenerator;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public class CmdPmmoRoot {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("pmmo")
				.then(CmdNodeAdmin.register(dispatcher))
				.then(CmdNodeParty.register(dispatcher))					
				.then(Commands.literal("genData")
						.requires(ctx -> ctx.hasPermission(2))
						.then(Commands.literal("begin")
								.executes(ctx -> set(ctx, Setting.RESET)))
						.then(Commands.literal("withOverride")
								.executes(ctx -> set(ctx, Setting.OVERRIDE)))
						.then(Commands.literal("disabler")
								.executes(ctx -> set(ctx, Setting.DISABLER)))
						.then(Commands.literal("withDefaults")
								.executes(ctx -> set(ctx, Setting.DEFAULT)))
						.then(Commands.literal("simplified")
								.executes(ctx -> set(ctx, Setting.SIMPLIFY)))
						.then(Commands.literal("forPlayers")
							.then(Commands.argument("players", EntityArgument.players())
									.executes(ctx -> set(ctx, Setting.PLAYER))))
						.then(Commands.literal("create")
							.executes(ctx -> PackGenerator.generatePack(ctx.getSource().getServer())))
						)					
				.then(CmdNodeStore.register(dispatcher))
				.then(Commands.literal("debug"))
				.then(Commands.literal("help")
						.executes(ctx -> help(ctx)))
				.executes(ctx -> credits(ctx)));
	}
	
	public static int credits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		//QOL open credits gui
		context.getSource().sendSuccess(() -> Component.literal("Mod by Harmony and Caltinor"), false);
		return 0;
	}
	
	public static int help(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		//QOL if Patchouli is installed, open the book
		context.getSource().sendSuccess(() -> Component.literal("Help can be found on the wiki or in the discord"), false);
		return 0;
	}
	
	private static enum Setting{RESET, DEFAULT, OVERRIDE, DISABLER, PLAYER, SIMPLIFY}
	public static int set(CommandContext<CommandSourceStack> context, Setting setting) throws CommandSyntaxException {
		context.getSource().sendSuccess(() -> switch (setting) {
		case RESET -> {
			PackGenerator.applyDefaults = false;
			PackGenerator.applyOverride = false;
			PackGenerator.applyDisabler = false;
			PackGenerator.applySimple = false;
			PackGenerator.players.clear();
			yield LangProvider.PACK_BEGIN.asComponent();
		}
		case DEFAULT -> {
			PackGenerator.applyDefaults = true;
			yield LangProvider.PACK_DEFAULTS.asComponent();
		}
		case OVERRIDE -> {
			PackGenerator.applyOverride = true;
			yield LangProvider.PACK_OVERRIDE.asComponent();
		}
		case DISABLER -> {
			PackGenerator.applyDisabler = true;
			yield LangProvider.PACK_DISABLER.asComponent();
		}
		case SIMPLIFY -> {
			PackGenerator.applySimple = true;
			yield LangProvider.PACK_SIMPLE.asComponent();
		}
		case PLAYER -> {
			try {PackGenerator.players.addAll(EntityArgument.getPlayers(context, "players"));}catch(CommandSyntaxException e) {}
			yield LangProvider.PACK_PLAYERS.asComponent();
		}}, true);
		return 0;
	}
}
