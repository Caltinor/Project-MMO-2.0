package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;

import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class ToolsCommand extends CommandBase
{
    @Override
    public String getName()
    {
        return "tools";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        List<String> completions = new ArrayList<>();
        EntityPlayerMP player = null;
        try
        {
            player = CommandBase.getCommandSenderAsPlayer( sender );
        }
        catch( PlayerNotFoundException e )
        {

        }

        if( args.length == 1 )
        {
            completions.add( "xpAtLevel" );
            completions.add( "levelAtXp" );
            completions.add( "xpTo" );
        }
        else if( player != null )
        {
            Map<Skill, Double> skillMap = PmmoSavedData.get().getXpMap( player.getUniqueID() );
            String command = args[0].toLowerCase();

            if( args.length == 2 )
            {
                for( Double value : skillMap.values() )
                {
                    if( command.equals( "levelatxp" ) )
                        completions.add( DP.dpSoft( value ) );
                    else
                        completions.add( DP.dpSoft( XP.levelAtXpDecimal( value ) ) );
                }
            }
            else if( args.length == 3 && command.equals( "xpto" ) )
            {
                for( Double value : skillMap.values() )
                {
                    completions.add( DP.dpSoft( XP.levelAtXpDecimal( value ) ) );
                }
            }
        }

        return completions;
    }

    @Override
    public List<String> getAliases()
    {
        List<String> aliases = new ArrayList<>();
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
        EntityPlayerMP player = null;
        try
        {
            player = CommandBase.getCommandSenderAsPlayer( sender );
        }
        catch (PlayerNotFoundException e)
        {
            //Not player, bad
            return;
        }

        if( args.length > 0 )
        {
            String command = args[0].toLowerCase();
            double maxLevel = FConfig.getConfig( "maxLevel" );
            double maxXp = FConfig.getConfig( "maxXp" );

            switch( command )
            {
                case "xpatlevel":
                {
                    if( args.length > 1 )
                    {
                        double level = Double.parseDouble( args[1] );

                        if( Double.isNaN( level ) )
                        {
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.notANumber", args[0] ).setStyle( XP.textStyle.get( "red" ) ), false );
                            return;
                        }

                        if( level < 1 )
                            level = 1;

                        if( level > maxLevel )
                            level = maxLevel;

                        ITextComponent message = new TextComponentTranslation( "pmmo.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dpSoft(level) ), DP.dpSoft( XP.xpAtLevelDecimal( level ) ) );

                        PmmoCommand.reply( player, message );
                    }
                }
                    return;

                case "levelatxp":
                {
                    if( args.length > 1 )
                    {
                        double xp = Double.parseDouble( args[1] );

                        if( Double.isNaN( xp ) )
                        {
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.notANumber", args[0] ).setStyle( XP.textStyle.get( "red" ) ), false );
                            return;
                        }

                        if( xp < 0 )
                            xp = 0;

                        if( xp >= maxXp )
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.levelAtXp", DP.dpSoft( xp ), maxLevel ), false );
                        else
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.levelAtXp", DP.dpSoft( xp ), XP.levelAtXpDecimal( xp ) ), false );
                    }
                }
                    return;

                case "xpto":
                {
                    if( args.length > 1 )
                    {
                        double level = Double.parseDouble( args[1] );
                        if( Double.isNaN( level ) )
                        {
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidNumber", args[1] ).setStyle( XP.skillStyle.get( "red" ) ), false );
                            return;
                        }
                        if( level < 1 )
                            level = 1;
                        if( level > maxLevel )
                            level = maxLevel;
                        double xp = XP.xpAtLevelDecimal( level );
                        if( xp < 0 )
                            xp = 0;

                        if( args.length > 2 )
                        {
                            double goalLevel = Double.parseDouble( args[2] );

                            if( Double.isNaN( goalLevel ) )
                            {
                                player.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidNumber", args[2] ).setStyle( XP.skillStyle.get( "red" ) ), false );
                                return;
                            }

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

                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.xpFromTo", DP.dpSoft(goalXp - xp), ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dpSoft(level) ), ( goalLevel % 1 == 0 ? (int) Math.floor( goalLevel ) : DP.dpSoft(goalLevel) ) ), false );
                        }
                        else
                            player.sendStatusMessage( new TextComponentTranslation( "pmmo.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dpSoft(level) ), DP.dpSoft(xp) ), false );
                    }
                    else
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( XP.skillStyle.get( "red" ) ), false );

                }
                    return;

                default:
                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidChoice", args[0] ).setStyle( XP.textStyle.get( "red" ) ), false );
                    break;
            }
        }
        else
        {
            player.sendStatusMessage( new TextComponentTranslation("pmmo.missingNextArgument" ).setStyle( XP.textStyle.get( "red" ) ), false );
            return;
        }
    }
}