package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.TranslatableComponent;

public class XpAtLevelCommand
{
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        double maxLevel = XP.getMaxLevel();
        Player player = (Player) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double level = Double.parseDouble(args[3]);

        if(level < 1)
            level = 1;

        if(level > maxLevel)
            level = maxLevel;

        player.displayClientMessage(new TranslatableComponent("pmmo.xpAtLevel", (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), DP.dp(XP.xpAtLevelDecimal(level))), false);

        return 1;
    }
}
