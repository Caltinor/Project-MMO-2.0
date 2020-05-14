package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class XpFromToCommand
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        double maxLevel = Config.getConfig( "maxLevel" );
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
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

            player.sendStatusMessage( new TranslationTextComponent( "pmmo.xpFromTo", DP.dp(goalXp - xp), ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), ( goalLevel % 1 == 0 ? (int) Math.floor( goalLevel ) : DP.dp(goalLevel) ) ), false );
        }
        else
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp(xp) ), false );

        return 1;
    }
}
