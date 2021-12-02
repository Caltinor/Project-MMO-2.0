package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.TranslatableComponent;

public class LevelAtXpCommand
{
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        Player player = (Player) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double xp = Double.parseDouble(args[3]);
        double maxXp = Config.getConfig("maxXp");
        double maxLevel = XP.getMaxLevel();

        if(xp < 0)
            xp = 0;

        if(xp >= maxXp)
            player.displayClientMessage(new TranslatableComponent("pmmo.levelAtXp", DP.dp(xp), maxLevel), false);
        else
            player.displayClientMessage(new TranslatableComponent("pmmo.levelAtXp", DP.dp(xp), XP.levelAtXpDecimal(xp)), false);
        return 1;
    }
}
