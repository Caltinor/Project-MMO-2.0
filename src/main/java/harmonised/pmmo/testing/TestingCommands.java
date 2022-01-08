package harmonised.pmmo.testing;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.setup.Reference;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class TestingCommands {
	
	@SubscribeEvent
	public static void testCommands(RegisterCommandsEvent event) {
		PrintStatsCommand.regsister(event.getDispatcher());
	}
	
	public static class PrintStatsCommand implements Command<CommandSourceStack> {
		private static final PrintStatsCommand CMD = new PrintStatsCommand();

		public static void regsister(CommandDispatcher<CommandSourceStack> dispatcher) {
			dispatcher.register(Commands.literal("mystats").executes(CMD));
		}
		
		@Override
		public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			ServerPlayer player = context.getSource().getPlayerOrException();
			Map<String, Long> xpMap = PmmoSavedData.get().getXpMap(player.getUUID());
			player.sendMessage(new TextComponent("My PMMO Skills"), player.getUUID());
			for (Map.Entry<String, Long> skill : xpMap.entrySet()) {
				player.sendMessage(new TextComponent(skill.getKey()+": "+String.valueOf(XpUtils.getLevelFromXP(skill.getValue()))), player.getUUID());
			}
			return 0;
		}
		
	}
}
