package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.TranslatableComponent;

public class XpFromToCommand
{
    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        double maxLevel = XP.getMaxLevel();
        Player player = (Player) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

        double level = Double.parseDouble( args[3] );
        if( level < 1 )
            level = 1;
        if( level > maxLevel )
            level = maxLevel;
        double xp = XP.xpAtLevelDecimal( level );
        if( xp < 0 )
            xp = 0;

        if( args.length > 4 )
        {
            double goalLevel = Double.parseDouble( args[4] );
            if( goalLevel < 1 )
                goalLevel = 1;
            if( goalLevel > maxLevel )
                goalLevel = maxLevel;

            if( goalLevel < level )
            {
                double temp = goalLevel;
                goalLevel = level;
                level = temp;

                xp = XP.xpAtLevelDecimal( level );
            }

            double goalXp = XP.xpAtLevelDecimal( goalLevel );
            if( goalXp < 0 )
                goalXp = 0;

            player.displayClientMessage( new TranslatableComponent( "pmmo.xpFromTo", DP.dp(goalXp - xp), ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), ( goalLevel % 1 == 0 ? (int) Math.floor( goalLevel ) : DP.dp(goalLevel) ) ), false );
        }
        else
            player.displayClientMessage( new TranslatableComponent( "pmmo.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp(xp) ), false );

        return 1;
    }
}
