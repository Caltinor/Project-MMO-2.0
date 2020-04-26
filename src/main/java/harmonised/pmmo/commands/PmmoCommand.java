package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class PmmoCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register( CommandDispatcher<CommandSource> dispatcher )
    {
        String[] suggestSkill = new String[15];
        suggestSkill[0] = "Mining";
        suggestSkill[1] = "Building";
        suggestSkill[2] = "Excavation";
        suggestSkill[3] = "Woodcutting";
        suggestSkill[4] = "Farming";
        suggestSkill[5] = "Agility";
        suggestSkill[6] = "Endurance";
        suggestSkill[7] = "Combat";
        suggestSkill[8] = "Archery";
        suggestSkill[9] = "Smithing";
        suggestSkill[10] = "Flying";
        suggestSkill[11] = "Swimming";
        suggestSkill[12] = "Fishing";
        suggestSkill[13] = "Crafting";
        suggestSkill[14] = "Magic";

        String[] suggestClear = new String[1];
        suggestClear[0] = "iagreetothetermsandconditions";

        String[] levelOrXp = new String[2];
        levelOrXp[0] = "level";
        levelOrXp[1] = "xp";

        dispatcher.register( Commands.literal( "pmmo" ).requires( player -> { return player.hasPermissionLevel( 4 ); })
                  .then( Commands.literal( "admin" )
                  .then( Commands.argument( "target", EntityArgument.players() )
                  .then( Commands.literal( "set" )
                  .then( Commands.argument( "Skill", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                  .then( Commands.argument( "Level|Xp", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( levelOrXp, theBuilder ) )
                  .then( Commands.argument( "New Value", DoubleArgumentType.doubleArg() )
                  .executes( PmmoCommand::commandSet )
                  ))))
                  .then( Commands.literal( "add" )
                  .then( Commands.argument( "Skill", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                  .then( Commands.argument( "Level|Xp", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( levelOrXp, theBuilder ) )
                  .then( Commands.argument( "Value To Add", DoubleArgumentType.doubleArg() )
                  .executes( PmmoCommand::commandAdd )
                  ))))
                  .then( Commands.literal( "clear" )
                  .executes( PmmoCommand::commandClear ) )))
                  .then( Commands.literal( "sync" )
                  .executes( context -> commandSync( context, EntityArgument.getPlayers( context, "target" ) ) )
                  )
                  .then(Commands.literal( "sync" )
                  .executes( context -> commandSync( context, null )))
                  .then( Commands.literal( "tools" )
                  .then( Commands.literal( "levelatxp" )
                  .then( Commands.argument( "xp", DoubleArgumentType.doubleArg() )
                  .executes( PmmoCommand::commandLevelAtXp )
                  ))
                  .then( Commands.literal( "xpatlevel" )
                  .then(  Commands.argument( "level", DoubleArgumentType.doubleArg() )
                  .executes( PmmoCommand::commandXpAtLevel )
                  ))
                  .then( Commands.literal( "xpto" )
                  .then(  Commands.argument( "level", DoubleArgumentType.doubleArg() )
                  .executes( PmmoCommand::commandXpFromTo )
                  .then(  Commands.argument( "goal level", DoubleArgumentType.doubleArg() )
                  .executes( PmmoCommand::commandXpFromTo )
                  )))));
    }

    private static int commandClear( CommandContext<CommandSource> context ) throws CommandException
    {
        String[] args = context.getInput().split( " " );

        try
        {
            Collection<ServerPlayerEntity> players = EntityArgument.getPlayers( context, "target" );

            for( ServerPlayerEntity player : players )
            {
                AttributeHandler.updateDamage( player );
                AttributeHandler.updateHP( player );
                AttributeHandler.updateReach( player );

                NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0, true ), player );
                player.getPersistentData().getCompound( "pmmo" ).put( "skills", new CompoundNBT() );

                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.skillsCleared" ), false );
            }
        }
        catch( CommandSyntaxException e )
        {
            LOGGER.error( "Clear Command Failed to get Players [" + Arrays.toString(args) + "]", e );
        }

        return 1;
    }

    private static int commandSet(CommandContext<CommandSource> context) throws CommandException
    {
        String[] args = context.getInput().split( " " );
        String skillName = args[4].toLowerCase();
        int skillInt = Skill.getInt( skillName );
        PlayerEntity sender = null;

        try
        {
            sender = context.getSource().asPlayer();
        }
        catch( CommandSyntaxException e )
        {
            //not player, it's fine
        }

        if( skillInt != 0 )
        {
            try
            {
                Collection<ServerPlayerEntity> players = EntityArgument.getPlayers( context, "target" );

                for( ServerPlayerEntity player : players )
                {
                    double newValue = Double.parseDouble( args[6] );

                    if( newValue > XP.maxXp )
                        newValue = XP.maxXp;

                    if( newValue < 0 )
                        newValue = 0;

                    if( args[5].toLowerCase().equals( "level" ) )
                    {
                        double newLevelXp = XP.xpAtLevel( newValue );

                        NetworkHandler.sendToPlayer( new MessageXp( newLevelXp, skillInt, 0, true ), player );
                        player.getPersistentData().getCompound( "pmmo" ).getCompound( "skills" ).putDouble( skillName, newLevelXp );

                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.setLevel", skillName, newValue ), false );
                    }
                    else if( args[5].toLowerCase().equals( "xp" ) )
                    {
                        NetworkHandler.sendToPlayer( new MessageXp( newValue, skillInt, 0, true ), player );
                        player.getPersistentData().getCompound( "pmmo" ).getCompound( "skills" ).putDouble( skillName, newValue );

                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.setXp", skillName, newValue ), false );
                    }
                    else
                    {
                        LOGGER.error( "Invalid 6th Element in command (level|xp) " + Arrays.toString( args ) );

                        if( sender != null )
                            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidChoice", args[5] ), false );
                    }

                    AttributeHandler.updateDamage( player );
                    AttributeHandler.updateHP( player );
                    AttributeHandler.updateReach( player );
                }
            }
            catch( CommandSyntaxException e )
            {
                LOGGER.error( "Set Command Failed to get Players [" + Arrays.toString(args) + "]", e );
            }
        }
        else
        {
            LOGGER.error( "Invalid 5th Element in command (skill name) " + Arrays.toString( args ) );

            if( sender != null )
                sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidSkillWarning", skillName ).setStyle( new Style().setColor( TextFormatting.RED ) ), false );
        }

        return 1;
    }

    private static int commandAdd(CommandContext<CommandSource> context) throws CommandException
    {
        String[] args = context.getInput().split( " " );
        String skillName = args[4].toLowerCase();
        int skillInt = Skill.getInt( skillName );
        PlayerEntity sender = null;

        try
        {
            sender = context.getSource().asPlayer();
        }
        catch( CommandSyntaxException e )
        {
            //not player, it's fine
        }

        if( skillInt != 0 )
        {
            try
            {
                Collection<ServerPlayerEntity> players = EntityArgument.getPlayers( context, "target" );

                for( ServerPlayerEntity player : players )
                {
                    double newValue = Double.parseDouble( args[6] );
                    double playerXp = XP.getSkillsTag( player ).getDouble( skillName );
                    double newLevelXp;

                    if( args[5].toLowerCase().equals( "level" ) )
                    {
                        newLevelXp = XP.xpAtLevel( XP.levelAtXp( playerXp ) + newValue );

                        if( newLevelXp < 0 )
                            newLevelXp = 0;

                        NetworkHandler.sendToPlayer( new MessageXp( newLevelXp, skillInt, 0, true ), player );
                        player.getPersistentData().getCompound( "pmmo" ).getCompound( "skills" ).putDouble( skillName, newLevelXp );

                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.addLevel", skillName, newValue ), false );
                    }
                    else if( args[5].toLowerCase().equals( "xp" ) )
                    {
                        newLevelXp = newValue + playerXp;

                        if( newLevelXp > XP.maxXp )
                            newLevelXp = XP.maxXp;

                        if( newLevelXp < 0 )
                            newLevelXp = 0;

                        NetworkHandler.sendToPlayer( new MessageXp( newValue, skillInt, 0, true ), player );
                        player.getPersistentData().getCompound( "pmmo" ).getCompound( "skills" ).putDouble( skillName, newLevelXp );

                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.addXp", skillName, newValue ), false );
                    }
                    else
                    {
                        LOGGER.error( "Invalid 6th Element in command (level|xp) " + Arrays.toString( args ) );

                        if( sender != null )
                            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidChoice", args[5] ), false );
                    }

                    AttributeHandler.updateDamage( player );
                    AttributeHandler.updateHP( player );
                    AttributeHandler.updateReach( player );
                }
            }
            catch( CommandSyntaxException e )
            {
                LOGGER.error( "Add Command Failed to get Players [" + Arrays.toString(args) + "]", e );
            }
        }
        else
        {
            LOGGER.error( "Invalid 5th Element in command (skill name) " + Arrays.toString( args ) );

            if( sender != null )
                sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidSkillWarning", skillName ).setStyle( new Style().setColor( TextFormatting.RED ) ), false );
        }

        return 1;
    }

    private static int commandSync( CommandContext<CommandSource> context, @Nullable  Collection<ServerPlayerEntity> players ) throws CommandException
    {
        if( players != null )
        {
            for( ServerPlayerEntity player : players )
            {
                XP.syncPlayer( player );
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.skillsResynced" ), false );
            }
        }
        else
        {
            try
            {
                PlayerEntity player = context.getSource().asPlayer();
                XP.syncPlayer( player );
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.skillsResynced" ), false );
            }
            catch( CommandSyntaxException e )
            {
                LOGGER.error( "Sync command fired not from player " + context.getInput(), e );
            }
        }

        return 1;
    }

    public static int commandLevelAtXp(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double xp = Double.parseDouble( args[3] );

        if( xp < 0 )
            xp = 0;

        if( xp >= XP.maxXp )
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelAtXp", DP.dp( xp ), XP.maxLevel ), false );
        else
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelAtXp", DP.dp( xp ), XP.levelAtXpDecimal( xp ) ), false );
        return 1;
    }

    public static int commandXpAtLevel(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double level = Double.parseDouble( args[3] );

        if( level < 1 )
            level = 1;

        if( level > XP.maxLevel )
            level = XP.maxLevel;

        player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp( XP.xpAtLevelDecimal( level ) ) ), false );

        return 1;
    }

    public static int commandXpFromTo(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

        double level = Double.parseDouble( args[3] );
        if( level < 1 )
            level = 1;
        if( level > XP.maxLevel )
            level = XP.maxLevel;
        double xp = XP.xpAtLevelDecimal( level );
        if( xp < 0 )
            xp = 0;

        if( args.length > 4 )
        {
            double goalLevel = Double.parseDouble( args[4] );
            if( goalLevel < 1 )
                goalLevel = 1;
            if( goalLevel > XP.maxLevel )
                goalLevel = XP.maxLevel;

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

            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.xpFromTo", DP.dp(goalXp - xp), ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), ( goalLevel % 1 == 0 ? (int) Math.floor( goalLevel ) : DP.dp(goalLevel) ) ), false );
        }
        else
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp(xp) ), false );

        return 1;
    }
}
