package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;

import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdminCommand extends CommandBase
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName()
    {
        return "admin";
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

        if( args.length == 1 )
            return getListOfStringsMatchingLastWord( args, server.getOnlinePlayerNames() );
        else if( args.length == 2 )
        {
            completions.add( "add" );
            completions.add( "set" );
            completions.add( "clear" );
        }
        else if( !args[1].equals( "clear" ) )
        {
            if( args.length == 3 )
                return PmmoCommand.skillCompletions;
            else if( args.length == 4 )
            {
                completions.add( "level" );
                completions.add( "xp" );
            }
            else if( args.length == 5 )
                completions.add( "1" );
            else if( args.length == 6 && args[ 1 ].toLowerCase().equals( "add" ) )
            {
                completions.add( "true" );
                completions.add( "false" );
            }
        }

        return completions;
    }

    @Override
    public List<String> getAliases()
    {
        List<String> aliases = new ArrayList<String>();
        return aliases;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return null;
    }

    @Override
    public void execute( MinecraftServer server, ICommandSender commandSender, String[] args ) throws CommandException
    {
        boolean ignoreBonuses = true;
        EntityPlayerMP sender = null;
        int permissionLevel = 0;
        try
        {
            sender = CommandBase.getCommandSenderAsPlayer( commandSender );
        }
        catch ( Exception e )
        {
            //Not player, it's fine
        }

        if( args.length > 0 )
        {
            EntityPlayerMP targetPlayer = getPlayer( server, commandSender, args[0] );
            String targetPlayerName = targetPlayer.getDisplayName().getUnformattedText();

            if( targetPlayer != null )
            {
                if( args.length > 1 )
                {
                    switch( args[1].toLowerCase() )
                    {
                        case "add":
                        case "set":
                        {
                            if( args.length > 2 )
                            {
                                String skillName = args[2];
                                Skill skill = Skill.getSkill( skillName );

                                if( !skill.equals( Skill.INVALID_SKILL ) )
                                {
                                    if( args.length > 3 )
                                    {
                                        String type = args[3].toLowerCase();

                                        if( type.equals( "level" ) || type.equals( "xp" ) )
                                        {
                                            if( args.length > 4 )
                                            {
                                                double newValue = parseDouble( args[4] );

                                                if( args.length > 5 && args[5].toLowerCase().equals( "false" ) )
                                                        ignoreBonuses = false;

                                                if( !Double.isNaN( newValue ) )
                                                {
                                                    switch( args[1].toLowerCase() )
                                                    {
                                                        case "add":
                                                        {
                                                            if( type.equals( "level" ) )
                                                                skill.addLevel( targetPlayer, newValue, "add level Command", false, ignoreBonuses );
                                                            else if( type.equals( "xp" ) )
                                                                skill.addXp( targetPlayer, newValue, "add xp Command", false, ignoreBonuses );

                                                            LOGGER.info( "PMMO Command Add: " + targetPlayerName + " " + skillName + " has had " + newValue + " " + type + " added" );
                                                        }
                                                            return;

                                                        case "set":
                                                        {
                                                            if( type.equals( "level" ) )
                                                                skill.setLevel( targetPlayer, newValue );
                                                            else if( type.equals( "xp" ) )
                                                                skill.setXp( targetPlayer, newValue );

                                                            LOGGER.info( "PMMO Command Set: " + targetPlayerName + " " + skillName + " has been set to " + newValue + " " + type );
                                                        }
                                                            return;
                                                    }
                                                }
                                                else
                                                    PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.notANumber", args[4] ).setStyle( XP.skillStyle.get( "red" ) ) );
                                            }
                                            else
                                                PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( XP.skillStyle.get( "red" ) ) );
                                        }
                                    }
                                    else
                                        PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( XP.skillStyle.get( "red" ) ) );
                                }
                                else
                                    PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.invalidSkill", args[1] ).setStyle( XP.skillStyle.get( "red" ) ) );
                            }
                            else
                                PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( XP.skillStyle.get( "red" ) ) );
                        }
                        return;

                        case "clear":
                        {
                            Set<Skill> skills = PmmoSavedData.get().getXpMap( targetPlayer.getUniqueID() ).keySet();
                            for( Skill theSkill : skills )
                            {
                                theSkill.setXp( targetPlayer, 0D );
                            }
                            NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0, true ), targetPlayer );
                            targetPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.skillsCleared" ), false );

                            LOGGER.info( "PMMO Command Clear: " + targetPlayerName + " has had their stats wiped!" );
                        }
                            return;

                        default:
                        {
                            PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.invalidChoice", args[1] ).setStyle( XP.textStyle.get( "red" ) ) );
                        }
                            return;
                    }
                }
                else
                    PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( XP.skillStyle.get( "red" ) ) );
            }
            else
                PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.invalidPlayer", args[0] ).setStyle( XP.skillStyle.get( "red" ) ) );
        }
        else
            PmmoCommand.reply( sender, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( XP.textStyle.get( "red" ) ) );
    }
}
