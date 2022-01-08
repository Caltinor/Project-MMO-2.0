package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CmdPmmoRoot {
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("pmmo")
				.then(Commands.literal("admin"))
				.then(Commands.literal("party"))
				.then(Commands.literal("resync"))
				.then(Commands.literal("tools"))
				.then(Commands.literal("checkbiome"))
				.then(Commands.literal("debug"))
				.then(Commands.literal("help")));
	}
}
