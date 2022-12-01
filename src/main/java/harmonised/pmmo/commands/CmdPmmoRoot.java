package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.config.writers.PackGenerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class CmdPmmoRoot {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("pmmo")
				.then(CmdNodeAdmin.register(dispatcher))
				.then(CmdNodeParty.register(dispatcher))					
				.then(Commands.literal("genData")
						.requires(ctx -> ctx.hasPermission(2))
						.executes(ctx -> {
							PackGenerator.generateEmptyPack(ctx.getSource().getServer(), false);
							return 0;
						})
						.then(Commands.literal("withOverride")
								.executes(ctx -> {
									PackGenerator.generateEmptyPack(ctx.getSource().getServer(), true);
									return 0;
								}))
						.then(Commands.literal("disabler")
								.executes(ctx -> {
									PackGenerator.generateDisablingPack(ctx.getSource().getServer());
									return 0;
								})))
				.then(CmdNodeStore.register(dispatcher))
				.then(Commands.literal("debug"))
				.then(Commands.literal("help")
						.executes(ctx -> help(ctx)))
				.executes(ctx -> credits(ctx)));
	}
	
	public static int credits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		//QOL open credits gui
		context.getSource().sendSuccess(new TextComponent("Mod by Harmony and Caltinor"), false);
		return 0;
	}
	
	public static int help(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		//QOL if Patchouli is installed, open the book
		context.getSource().sendSuccess(new TextComponent("Help can be found on the wiki or in the discord"), false);
		return 0;
	}
}
