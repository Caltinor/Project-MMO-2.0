package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class LevelAtXpCommand
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double xp = Double.parseDouble(args[3]);
        double maxXp = Config.getConfig("maxXp");
        double maxLevel = XP.getMaxLevel();

        if(xp < 0)
            xp = 0;

        if(xp >= maxXp)
            player.sendStatusMessage(new TranslationTextComponent("pmmo.levelAtXp", DP.dp(xp), maxLevel), false);
        else
            player.sendStatusMessage(new TranslationTextComponent("pmmo.levelAtXp", DP.dp(xp), XP.levelAtXpDecimal(xp)), false);
        return 1;
    }
}
