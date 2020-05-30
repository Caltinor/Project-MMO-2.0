package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageUpdateNBT;
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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class PmmoCommand
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static String[] suggestSkill = new String[20];
    public static String[] levelOrXp = new String[2];
    public static String[] suggestPref = new String[7];
    public static String[] suggestGui = new String[18];
    public static String[] suggestSearchRegistry = new String[5];

    public static void register( CommandDispatcher<CommandSource> dispatcher )
    {
        suggestSkill[0]  = "Power";
        suggestSkill[1]  = "Mining";
        suggestSkill[2]  = "Building";
        suggestSkill[3]  = "Excavation";
        suggestSkill[4]  = "Woodcutting";
        suggestSkill[5]  = "Farming";
        suggestSkill[6]  = "Agility";
        suggestSkill[7]  = "Endurance";
        suggestSkill[8]  = "Combat";
        suggestSkill[9]  = "Archery";
        suggestSkill[10] = "Smithing";
        suggestSkill[11] = "Flying";
        suggestSkill[12] = "Swimming";
        suggestSkill[13] = "Fishing";
        suggestSkill[14] = "Crafting";
        suggestSkill[15] = "Magic";
        suggestSkill[16] = "Slayer";
        suggestSkill[17] = "Hunter";
        suggestSkill[18] = "Fletching";
        suggestSkill[19] = "Taming";

//        suggestClear[0] = "iagreetothetermsandconditions";

        levelOrXp[0] = "level";
        levelOrXp[1] = "xp";

        suggestPref[0] = "maxReachBoost";
        suggestPref[1] = "maxSpeedBoost";
        suggestPref[2] = "maxSprintJumpBoost";
        suggestPref[3] = "maxCrouchJumpBoost";
        suggestPref[4] = "maxExtraHeartBoost";
        suggestPref[5] = "maxExtraDamageBoost";
        suggestPref[6] = "wipeAllSkillsUponDeathPermanently";

        suggestGui[0] = "barOffsetX";
        suggestGui[1] = "barOffsetY";
        suggestGui[2] = "veinBarOffsetX";
        suggestGui[3] = "veinBarOffsetY";
        suggestGui[4] = "xpDropOffsetX";
        suggestGui[5] = "xpDropOffsetY";
        suggestGui[6] = "xpDropSpawnDistance";
        suggestGui[7] = "xpDropOpacityPerTime";
        suggestGui[8] = "xpDropMaxOpacity";
        suggestGui[9] = "xpDropDecayAge";
        suggestGui[10] = "minXpGrow";
        suggestGui[11] = "showXpDrops";
        suggestGui[12] = "stackXpDrops";
        suggestGui[13] = "xpDropsAttachedToBar";
        suggestGui[14] = "xpBarAlwaysOn";
        suggestGui[15] = "xpLeftDisplayAlwaysOn";
        suggestGui[16] = "lvlUpScreenshot";
        suggestGui[17] = "lvlUpScreenshotShowSkills";

        suggestSearchRegistry[0] = "item";
        suggestSearchRegistry[1] = "biome";
        suggestSearchRegistry[2] = "enchant";
        suggestSearchRegistry[3] = "potionEffect";
        suggestSearchRegistry[4] = "entity";


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
                  .then( Commands.literal( "prefs" )
                  .then( Commands.argument( "option", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestPref, theBuilder ) )
                  .executes( PrefCommand::execute )
                  .then( Commands.argument( "new value", DoubleArgumentType.doubleArg() )
                  .executes( PrefCommand::execute )
                  )))
                  .then( Commands.literal( "gui" )
                  .then( Commands.argument( "option", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestGui, theBuilder ) )
                  .executes( PrefCommand::execute )
                  .then( Commands.argument( "new value", DoubleArgumentType.doubleArg() )
                  .executes( PrefCommand::execute )
                  )))
                  .then( Commands.literal( "checkstat" )
                  .then( Commands.argument( "player name", EntityArgument.player() )
                  .then( Commands.argument( "skill name", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                  .executes( CheckStatCommand::execute )
                  )))
                  .then( Commands.literal( "checkstats" )
                  .then( Commands.argument( "player name", EntityArgument.player() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
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
