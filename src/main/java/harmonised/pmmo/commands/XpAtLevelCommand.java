package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;

import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class XpAtLevelCommand extends CommandBase
{
    @Override
    public String getName()
    {
        return "levelatxp";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        EntityPlayer player;
        try
        {
            player = CommandBase.getCommandSenderAsPlayer( sender );
        }
        catch (PlayerNotFoundException e)
        {
            return PmmoCommand.skillCompletions;
        }

        Map<Skill, Double> skillsMap = FConfig.getXpMap( player );

        List<String> completions = new ArrayList<>();

        for( Map.Entry<Skill, Double> entry : skillsMap.entrySet() )
        {
            completions.add( Double.toString( entry.getValue() ) );
        }

        return completions;
    }

    @Override
    public List<String> getAliases()
    {
        List<String> aliases = new ArrayList<String>();
        aliases.add( "level" );
        aliases.add( "levelat" );
        return aliases;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return null;
    }

    @Override
    public void execute( MinecraftServer server, ICommandSender sender, String[] args ) throws CommandException
    {
        EntityPlayer player = null;
        try
        {
            player = CommandBase.getCommandSenderAsPlayer( sender );
        }
        catch (PlayerNotFoundException e)
        {
            //Not player, still okay
        }

        if( args.length > 0 )
        {
            double maxLevel = FConfig.getConfig( "maxLevel" );

            double level;

            try
            {
                level = Double.parseDouble( args[0] );
            }
            catch( Exception e )
            {
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.notANumber", args[0] ).setStyle( XP.textStyle.get( "red" ) ), false );
                return;
            }
            if( level < 1 )
                level = 1;

            if( level > maxLevel )
                level = maxLevel;

            ITextComponent message = new TextComponentTranslation( "pmmo.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp( XP.xpAtLevelDecimal( level ) ) );

            if( player == null )
                System.out.println( message.getFormattedText() );
            else
                player.sendStatusMessage( message, false );
        }
        else
        {
            if( player == null )
                System.out.println( "Missing Level" );
            else
                player.sendStatusMessage( new TextComponentTranslation("pmmo.invalidArguments" ).setStyle( XP.textStyle.get( "red" ) ), false );
        }
    }
}