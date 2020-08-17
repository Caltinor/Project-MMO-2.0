package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import harmonised.pmmo.skills.Skill;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class PmmoCommand
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static String[] suggestSkill;
    public static String[] levelOrXp = { "level", "xp" };
    public static String[] suggestPref = { "maxExtraReachBoost",
                                           "maxSpeedBoost",
                                           "maxSprintJumpBoost",
                                           "maxCrouchJumpBoost",
                                           "maxExtraHeartBoost",
                                           "maxExtraDamageBoost",
                                           "wipeAllSkillsUponDeathPermanently" };
    public static String[] suggestSearchRegistry = { "item",
                                                     "biome",
                                                     "enchant",
                                                     "potionEffect",
                                                     "entity" };
    public static void init()
    {
        suggestSkill = new String[ Skill.skillMap.size() ];
        int i = 0;
        for( Skill skill : Skill.skillMap.keySet() )
        {
            suggestSkill[ i++ ] = skill.toString().toLowerCase();
        }
    }

    public static void register( CommandDispatcher<CommandSource> dispatcher )
    {
//        int i = 0;
//
//        for( Biome biome : ForgeRegistries.BIOMES.getValues() )
//        {
//            suggestBiome[i++] = biome.getRegistryName().toString();
//        }

        dispatcher.register( Commands.literal( "pmmo" )
                  .then( Commands.literal( "admin" )
                  .requires( player -> { return player.hasPermissionLevel( 2 ); })
                  .then( Commands.argument( "target", EntityArgument.players() )
                  .then( Commands.literal( "set" )
                  .then( Commands.argument( "Skill", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                  .then( Commands.argument( "Level|Xp", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( levelOrXp, theBuilder ) )
                  .then( Commands.argument( "New Value", DoubleArgumentType.doubleArg() )
                  .executes( SetCommand::execute )
                  ))))
                  .then( Commands.literal( "add" )
                  .then( Commands.argument( "Skill", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                  .then( Commands.argument( "Level|Xp", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( levelOrXp, theBuilder ) )
                  .then( Commands.argument( "Value To Add", DoubleArgumentType.doubleArg() )
                  .executes( AddCommand::execute )
                  .then( Commands.argument( "Ignore Bonuses", BoolArgumentType.bool() )
                  .executes( AddCommand::execute ) )
                  ))))
                  .then( Commands.literal( "clear" )
                  .executes( ClearCommand::execute ) )))
                  .then( Commands.literal( "reload" )
                  .requires( player -> { return player.hasPermissionLevel( 2 ); } )
                  .executes( ReloadConfigCommand::execute )
                  )
                  .then( Commands.literal( "resync" )
                  .executes( context -> SyncCommand.execute( context, EntityArgument.getPlayers( context, "target" ) ) )
                  )
                  .then(Commands.literal( "resync" )
                  .executes( context -> SyncCommand.execute( context, null )))
                  .then( Commands.literal( "tools" )
                  .then( Commands.literal( "levelatxp" )
                  .then( Commands.argument( "xp", DoubleArgumentType.doubleArg() )
                  .executes( LevelAtXpCommand::execute )
                  ))
                  .then( Commands.literal( "xpatlevel" )
                  .then(  Commands.argument( "level", DoubleArgumentType.doubleArg() )
                  .executes( XpAtLevelCommand::execute )
                  ))
                  .then( Commands.literal( "xpto" )
                  .then(  Commands.argument( "level", DoubleArgumentType.doubleArg() )
                  .executes( XpFromToCommand::execute )
                  .then(  Commands.argument( "goal level", DoubleArgumentType.doubleArg() )
                  .executes( XpFromToCommand::execute )
                  ))))
//                  .then( Commands.literal( "prefs" )
//                  .then( Commands.argument( "option", StringArgumentType.word() )
//                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestPref, theBuilder ) )
//                  .executes( PrefCommand::execute )
//                  .then( Commands.argument( "new value", DoubleArgumentType.doubleArg() )
//                  .executes( PrefCommand::execute )
//                  )))
//                  .then( Commands.literal( "gui" )
//                  .then( Commands.argument( "option", StringArgumentType.word() )
//                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestGui, theBuilder ) )
//                  .executes( PrefCommand::execute )
//                  .then( Commands.argument( "new value", DoubleArgumentType.doubleArg() )
//                  .executes( PrefCommand::execute )
//                  )))
//                  .then( Commands.literal( "checkstat" )
//                  .then( Commands.argument( "player name", EntityArgument.player() )
//                  .then( Commands.argument( "skill name", StringArgumentType.word() )
//                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
//                  .executes( CheckStatCommand::execute )
//                  )))
                  .then( Commands.literal( "checkstats" )
                  .then( Commands.argument( "player name", EntityArgument.player() )
                  .executes( CheckStatsCommand::execute )
                  ))
                  .then( Commands.literal( "checkbiome" )
                  .executes( CheckBiomeCommand::execute )
                  )
                  .then( Commands.literal( "debug" )
                  .then( Commands.literal( "searchRegistry" )
                  .then(Commands.argument( "type", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSearchRegistry, theBuilder ) )
                  .then(Commands.argument( "search query", StringArgumentType.word() )
                  .executes( SearchRegCommand::execute )
                  )))
                  .then( Commands.literal( "nearbyPowerLevel" )
                  .executes( NearbyPowerLevelCommand::execute )
                  .then( Commands.argument( "target", EntityArgument.player() )
                  .executes( NearbyPowerLevelCommand::execute )
                  ))));
    }
}
