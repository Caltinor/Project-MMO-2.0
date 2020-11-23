package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;

public class AddCommand
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        String[] args = context.getInput().split( " " );
        String skillName = StringArgumentType.getString( context, "Skill" ).toLowerCase();
        String type = StringArgumentType.getString( context, "Level|Xp" ).toLowerCase();
        boolean ignoreBonuses = true;
        Skill skill = Skill.getSkill( skillName );
        EntityPlayer sender = null;

        try
        {
            ignoreBonuses = BoolArgumentType.getBool( context, "Ignore Bonuses" );
        }
        catch( IllegalArgumentException e )
        {
            //no Ignore Bonuses specified, it's fine
        }

        try
        {
            sender = context.getSource().asPlayer();
        }
        catch( CommandSyntaxException e )
        {
            //not player, it's fine
        }

        if( skill != Skill.INVALID_SKILL )
        {
            try
            {
                Collection<EntityPlayerMP> players = EntityArgument.getPlayers( context, "target" );

                for( EntityPlayerMP player : players )
                {
                    String playerName = player.getDisplayName().getUnformattedText();
                    double newValue = DoubleArgumentType.getDouble( context, "Value To Add" );

                    if( type.equals( "level" ) )
                        skill.addLevel( player, newValue, "add level Command", false, ignoreBonuses );
                    else if( type.equals( "xp" ) )
                        skill.addXp( player, newValue, "add xp Command", false, ignoreBonuses );
                    else
                    {
                        LOGGER.info( "PMMO Command Add: Invalid 6th Element in command (level|xp) " + Arrays.toString( args ) );

                        if( sender != null )
                            sender.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidChoice", args[5] ).setStyle( XP.textStyle.get( "red" ) ), false );
                    }

                    LOGGER.info( "PMMO Command Add: " + playerName + " " + args[4] + " has had " + args[6] + " " + args[5] + " added" );
                }
            }
            catch( CommandSyntaxException e )
            {
                LOGGER.info( "PMMO Command Add: Add Command Failed to get Players [" + Arrays.toString(args) + "]", e );
            }
        }
        else
        {
            LOGGER.info( "PMMO Command Add: Invalid 5th Element in command (skill name) " + Arrays.toString( args ) );

            if( sender != null )
                sender.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidSkill", skillName ).setStyle( XP.textStyle.get( "red" ) ), false );
        }

        return 1;
    }
}
