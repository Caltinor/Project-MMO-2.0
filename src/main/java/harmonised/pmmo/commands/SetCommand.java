package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;

public class SetCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        String[] args = context.getInput().split( " " );
        String skillName = StringArgumentType.getString( context, "Skill" ).toLowerCase();
        String type = StringArgumentType.getString( context, "Level|Xp" ).toLowerCase();
        Skill skill = Skill.getSkill( skillName );
        PlayerEntity sender = null;

        try
        {
            sender = context.getSource().asPlayer();
        }
        catch( CommandSyntaxException e )
        {
            //not player, it's fine
        }

        if( skillName.equals( "power" ) )
        {
            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.invalidChoice", skillName ), false );
            return 1;
        }

        if( skill != Skill.INVALID_SKILL )
        {
            try
            {
                Collection<ServerPlayerEntity> players = EntityArgument.getPlayers( context, "target" );

                for( ServerPlayerEntity player : players )
                {
                    String playerName = player.getDisplayName().getString();
                    double newValue = DoubleArgumentType.getDouble( context, "New Value" );

                    if( type.equals( "level" ) )
                        skill.setLevel( player, newValue );
                    else if( type.equals( "xp" ) )
                        skill.setXp( player, newValue );
                    else
                    {
                        LogHandler.LOGGER.error( "PMMO Command Set: Invalid 6th Element in command (level|xp) " + Arrays.toString( args ) );

                        if( sender != null )
                            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.invalidChoice", args[5] ).setStyle( XP.textStyle.get( "red" ) ), false );
                    }
                    LogHandler.LOGGER.info( "PMMO Command Set: " + playerName + " " + args[4] + " has been set to " + args[5] + " " + args[6] );
                }
            }
            catch( CommandSyntaxException e )
            {
                LogHandler.LOGGER.error( "PMMO Command Set: Failed to get Players [" + Arrays.toString(args) + "]", e );
            }
        }
        else
        {
            LogHandler.LOGGER.error( "PMMO Command Set: Invalid 5th Element in command (skill name) " + Arrays.toString( args ) );

            if( sender != null )
                sender.sendStatusMessage( new TranslationTextComponent( "pmmo.invalidSkill", skillName ).setStyle( XP.textStyle.get( "red" ) ), false );
        }

        return 1;
    }
}
