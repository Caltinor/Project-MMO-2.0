package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

public class XpAtLevelCommand
{
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        double maxLevel = Config.getConfig( "maxLevel" );
        EntityPlayer player = (EntityPlayer) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double level = Double.parseDouble( args[3] );

        if( level < 1 )
            level = 1;

        if( level > maxLevel )
            level = maxLevel;

        player.sendStatusMessage( new TextComponentTranslation( "pmmo.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp( XP.xpAtLevelDecimal( level ) ) ), false );

        return 1;
    }
}
