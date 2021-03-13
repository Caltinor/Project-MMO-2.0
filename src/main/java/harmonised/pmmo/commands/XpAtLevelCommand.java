package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class XpAtLevelCommand
{
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        double maxLevel = XP.getMaxLevel();
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double level = Double.parseDouble( args[3] );

        if( level < 1 )
            level = 1;

        if( level > maxLevel )
            level = maxLevel;

        player.sendStatusMessage( new TranslationTextComponent( "pmmo.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp( XP.xpAtLevelDecimal( level ) ) ), false );

        return 1;
    }
}
