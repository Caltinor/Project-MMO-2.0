package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.writers.PackGenerator;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.fml.ModList;

public class CmdPmmoRoot {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("pmmo")
				.then(CmdNodeAdmin.register(dispatcher))
				.then(CmdNodeParty.register(dispatcher))					
				.then(Commands.literal("genData")
						.requires(ctx -> ctx.hasPermission(2))
						.then(Commands.literal("begin")
								.executes(ctx -> set(ctx, Setting.RESET)))
						.then(Commands.literal("withoutObjects")
								.executes(ctx -> set(ctx, Setting.OBJECTS)))
						.then(Commands.literal("withOverride")
								.executes(ctx -> set(ctx, Setting.OVERRIDE)))
						.then(Commands.literal("disabler")
								.executes(ctx -> set(ctx, Setting.DISABLER)))
						.then(Commands.literal("withDefaults")
								.executes(ctx -> set(ctx, Setting.DEFAULT)))
						.then(Commands.literal("withConfigs")
								.executes(ctx -> set(ctx, Setting.CONFIG)))
						.then(Commands.literal("simplified")
								.executes(ctx -> set(ctx, Setting.SIMPLIFY)))
						.then(Commands.literal("modFilter")
							.then(Commands.argument("namespace", StringArgumentType.word())
									.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ModList.get().getMods()
											.stream().map(modInfo -> modInfo.getNamespace())
											//special exclusions 
											.filter(modid -> !modid.equals("pmmo") && !modid.equals("forge")), builder))
									.executes(ctx -> set(ctx, Setting.FILTER))))
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
		MutableComponent wiki = Component.literal("the wiki").withStyle(Style.EMPTY
				.withUnderlined(true)
				.withColor(ChatFormatting.BLUE)
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Caltinor/Project-MMO-2.0/blob/main/wiki/home.md")));
		MutableComponent discord = Component.literal("the discord").withStyle(Style.EMPTY
				.withUnderlined(true)
				.withColor(ChatFormatting.BLUE)
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/5NVNkNB")));
		context.getSource().sendSuccess(() -> Component.literal("Help can be found on ").append(wiki).append(" or in ").append(discord), false);
		return 0;
	}
	
	private static enum Setting{RESET, DEFAULT, OVERRIDE, DISABLER, PLAYER, SIMPLIFY, FILTER, CONFIG, OBJECTS}
	public static int set(CommandContext<CommandSourceStack> context, Setting setting) throws CommandSyntaxException {
		context.getSource().sendSuccess(() -> switch (setting) {
		case RESET -> {
			PackGenerator.applyDefaults = false;
			PackGenerator.applyOverride = false;
			PackGenerator.applyDisabler = false;
			PackGenerator.applySimple = false;
			PackGenerator.applyObjects = true;
			PackGenerator.applyConfigs = false;
			PackGenerator.players.clear();
			PackGenerator.namespaceFilter.clear();
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
		}
		case FILTER -> {
			PackGenerator.namespaceFilter.add(StringArgumentType.getString(context, "namespace"));
			yield LangProvider.PACK_FILTER.asComponent();
		}
		case OBJECTS -> {
			PackGenerator.applyObjects = false;
			yield LangProvider.PACK_OBJECTS.asComponent();
		}
		case CONFIG -> {
			PackGenerator.applyConfigs = true;
			yield LangProvider.PACK_CONFIGS.asComponent();
		}}, true);
		
		return 0;
	}
}
