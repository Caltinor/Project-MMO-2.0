package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.Requirements;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PmmoCommand
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static String[] suggestSkill = new String[18];
    private static String[] levelOrXp = new String[2];
    private static String[] suggestPref = new String[6];
    private static String[] suggestGui = new String[13];
    private static String[] suggestSearchRegistry = new String[5];

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
        suggestSkill[17] = "Fletching";

//        suggestClear[0] = "iagreetothetermsandconditions";

        levelOrXp[0] = "level";
        levelOrXp[1] = "xp";

        suggestPref[0] = "maxReachBoost";
        suggestPref[1] = "maxSpeedBoost";
        suggestPref[2] = "maxSprintJumpBoost";
        suggestPref[3] = "maxCrouchJumpBoost";
        suggestPref[4] = "maxExtraHeartBoost";
        suggestPref[5] = "maxExtraDamageBoost";

        suggestGui[0] = "barOffsetX";
        suggestGui[1] = "barOffsetY";
        suggestGui[2] = "xpDropOffsetX";
        suggestGui[3] = "xpDropOffsetY";
        suggestGui[4] = "xpDropSpawnDistance";
        suggestGui[5] = "xpDropOpacityPerTime";
        suggestGui[6] = "xpDropMaxOpacity";
        suggestGui[7] = "xpDropDecayAge";
        suggestGui[8] = "showXpDrops";
        suggestGui[9] = "stackXpDrops";
        suggestGui[10] = "xpDropsAttachedToBar";
        suggestGui[11] = "xpBarAlwaysOn";
        suggestGui[12] = "xpLeftDisplayAlwaysOn";

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
                  .then( Commands.literal( "reload" )
                  .requires( player -> { return player.hasPermissionLevel( 2 ); } )
                  .executes( PmmoCommand::commandReloadConfig )
                  )
                  .then( Commands.literal( "resync" )
                  .executes( context -> commandSync( context, EntityArgument.getPlayers( context, "target" ) ) )
                  )
                  .then(Commands.literal( "resync" )
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
                  ))))
                  .then( Commands.literal( "prefs" )
                  .then( Commands.argument( "option", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestPref, theBuilder ) )
                  .executes( PmmoCommand::commandPref )
                  .then( Commands.argument( "new value", DoubleArgumentType.doubleArg() )
                  .executes( PmmoCommand::commandPref )
                  )))
                  .then( Commands.literal( "gui" )
                  .then( Commands.argument( "option", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestGui, theBuilder ) )
                  .executes( PmmoCommand::commandPref )
                  .then( Commands.argument( "new value", DoubleArgumentType.doubleArg() )
                  .executes( PmmoCommand::commandPref )
                  )))
                  .then( Commands.literal( "checkstat" )
                  .then( Commands.argument( "player name", EntityArgument.player() )
                  .then( Commands.argument( "skill name", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                  .executes( PmmoCommand::commandCheckStat )
                  )))
                  .then( Commands.literal( "checkbiome" )
                  .executes( PmmoCommand::commandCheckBiome )
                  )
                  .then( Commands.literal( "debug" )
                  .then( Commands.literal( "searchRegistry" )
                  .then(Commands.argument( "type", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSearchRegistry, theBuilder ) )
                  .then(Commands.argument( "search query", StringArgumentType.word() )
                  .executes( PmmoCommand::commandSearchReg )
                  )))));
    }

    private static int commandSearchReg( CommandContext<CommandSource> context ) throws CommandException
    {
        String query = StringArgumentType.getString( context, "search query" );
        String type = StringArgumentType.getString( context, "type" );
        StringBuilder listOut = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");
        StringBuilder listOutExtra = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");

        switch( type )
        {
            case "item":
                for( Item item : ForgeRegistries.ITEMS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;

            case "biome":
                for( Biome item : ForgeRegistries.BIOMES )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;

            case "enchant":
                for( Enchantment item : ForgeRegistries.ENCHANTMENTS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;

            case "potionEffect":
                for( Effect item : ForgeRegistries.POTIONS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;

            case "entity":
                for( EntityType item : ForgeRegistries.ENTITIES )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;
        }

        System.out.println( listOut.toString() );
        System.out.println( listOutExtra.toString() );

        return 1;
    }

    private static int commandClear( CommandContext<CommandSource> context ) throws CommandException
    {
        String[] args = context.getInput().split( " " );

        try
        {
            Collection<ServerPlayerEntity> players = EntityArgument.getPlayers( context, "target" );

            for( ServerPlayerEntity player : players )
            {
                AttributeHandler.updateAll( player );

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
            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidChoice", skillName ), false );
            return 1;
        }

        if( skill != Skill.INVALID_SKILL )
        {
            try
            {
                Collection<ServerPlayerEntity> players = EntityArgument.getPlayers( context, "target" );

                for( ServerPlayerEntity player : players )
                {
                    double newValue = DoubleArgumentType.getDouble( context, "New Value" );

                    if( type.equals( "level" ) )
                        skill.setLevel( player, newValue );
                    else if( type.equals( "xp" ) )
                        skill.setXp( player, newValue );
                    else
                    {
                        LOGGER.error( "Invalid 6th Element in command (level|xp) " + Arrays.toString( args ) );

                        if( sender != null )
                            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidChoice", args[5] ).setStyle( XP.textStyle.get( "red" ) ), false );
                    }

                    AttributeHandler.updateAll( player );
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
                sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidSkill", skillName ).setStyle( XP.textStyle.get( "red" ) ), false );
        }

        return 1;
    }

    private static int commandAdd(CommandContext<CommandSource> context) throws CommandException
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

        if( skill != Skill.INVALID_SKILL )
        {
            try
            {
                Collection<ServerPlayerEntity> players = EntityArgument.getPlayers( context, "target" );

                for( ServerPlayerEntity player : players )
                {
                    double newValue = DoubleArgumentType.getDouble( context, "Value To Add" );

                    if( type.equals( "level" ) )
                        skill.addLevel( player, newValue );
                    else if( type.equals( "xp" ) )
                        skill.addXp( player, newValue );
                    else
                    {
                        LOGGER.error( "Invalid 6th Element in command (level|xp) " + Arrays.toString( args ) );

                        if( sender != null )
                            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidChoice", args[5] ).setStyle( XP.textStyle.get( "red" ) ), false );
                    }
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
                sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidSkill", skillName ).setStyle( XP.textStyle.get( "red" ) ), false );
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

    private static int commandLevelAtXp(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double xp = Double.parseDouble( args[3] );
        double maxXp = XP.getConfig( "maxXp" );
        double maxLevel = XP.getConfig( "maxLevel" );

        if( xp < 0 )
            xp = 0;

        if( xp >= maxXp )
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelAtXp", DP.dp( xp ), maxLevel ), false );
        else
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.levelAtXp", DP.dp( xp ), XP.levelAtXpDecimal( xp ) ), false );
        return 1;
    }

    private static int commandXpAtLevel(CommandContext<CommandSource> context) throws CommandException
    {
        double maxLevel = XP.getConfig( "maxLevel" );
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        double level = Double.parseDouble( args[3] );

        if( level < 1 )
            level = 1;

        if( level > maxLevel )
            level = maxLevel;

        player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp( XP.xpAtLevelDecimal( level ) ) ), false );

        return 1;
    }

    private static int commandXpFromTo(CommandContext<CommandSource> context) throws CommandException
    {
        double maxLevel = XP.getConfig( "maxLevel" );
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

            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.xpFromTo", DP.dp(goalXp - xp), ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), ( goalLevel % 1 == 0 ? (int) Math.floor( goalLevel ) : DP.dp(goalLevel) ) ), false );
        }
        else
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.xpAtLevel", ( level % 1 == 0 ? (int) Math.floor( level ) : DP.dp(level) ), DP.dp(xp) ), false );

        return 1;
    }

    private static int commandPref(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        CompoundNBT prefsTag = XP.getPreferencesTag( player );
        Double value = null;
        if( args.length > 3 )
        {
            value = Double.parseDouble( args[3] );
            if( value < 0 )
                value = 0D;
        }


        boolean matched = false;
        String match = "ERROR";

        for( String element : suggestPref )
        {
            if( args[2].toLowerCase().equals( element.toLowerCase() ) )
            {
                match = element;
                matched = true;
            }
        }

        for( String element : suggestGui )
        {
            if( args[2].toLowerCase().equals( element.toLowerCase() ) )
            {
                match = element;
                matched = true;
            }
        }

        if( matched )
        {
            if( value != null )
            {
                prefsTag.putDouble( match, value );

                NetworkHandler.sendToPlayer( new MessageUpdateNBT( prefsTag, "prefs" ), (ServerPlayerEntity) player );
                AttributeHandler.updateAll( player );

                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.hasBeenSet", match, args[3] ), false );
            }
            else if( prefsTag.contains( match ) )
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.hasTheValue", "" + match, "" + prefsTag.getDouble( match ) ), false );
            else
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.hasUnsetValue", "" + match ), false );
        }
        else
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidChoice", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );

        return 1;
    }

    private static int commandCheckStat(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity sender = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        String skillName = args[3].toLowerCase();

        if( Skill.getInt( skillName ) != 0 || skillName.toLowerCase().equals( "power" ) )
        {
            try
            {
                double level = 1;

                ServerPlayerEntity target = EntityArgument.getPlayer( context, "player name" );
                if( skillName.toLowerCase().equals( "power" ) )
                    level = XP.getPowerLevel( target );
                else
                    level = XP.levelAtXpDecimal( XP.getSkillsTag( target ).getDouble( skillName ) );

                sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.playerLevelDisplay", target.getDisplayName().getString(), (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), new TranslationTextComponent( "pmmo.text." + skillName ).setStyle( new Style().setColor( XP.skillTextFormat.get( skillName ) ) ) ), false );

                //EXTRA INFO
                switch( skillName )
                {
                    case "fishing":
                        double fishPoolBaseChance = Config.config.fishPoolBaseChance.get();
                        double fishPoolChancePerLevel = Config.config.fishPoolChancePerLevel.get();
                        double fishPoolMaxChance = Config.config.fishPoolMaxChance.get();
                        double fishPoolChance = fishPoolBaseChance + fishPoolChancePerLevel * level;
                        if( fishPoolChance > fishPoolMaxChance )
                            fishPoolChance = fishPoolMaxChance;

                        sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.fishPoolChance", DP.dp( fishPoolChance )  ).setStyle( new Style().setColor( XP.skillTextFormat.get( skillName ) ) ), false );
                        break;
                }
            }
            catch( CommandSyntaxException e )
            {
                sender.sendStatusMessage(  new TranslationTextComponent( "pmmo.text.invalidPlayer", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );
            }
        }
        else
            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidSkill", skillName ).setStyle( XP.textStyle.get( "red" ) ), false );

        return 1;
    }

    private static int commandCheckBiome(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity sender = (PlayerEntity) context.getSource().getEntity();
        String biomeKey = sender.world.getBiome( sender.getPosition() ).getRegistryName().toString();
        String transKey = sender.world.getBiome( sender.getPosition() ).getTranslationKey();
        Map<String, Object> theMap = Requirements.data.get( "biomeMobMultiplier" ).get( biomeKey );

        String damageBonus = "100";
        String hpBonus = "100";
        String speedBonus = "100";

        if( theMap != null )
        {
            if( theMap.containsKey( "damageBonus" ) )
                damageBonus = DP.dp( (double) theMap.get( "damageBonus" ) * 100 );
            if( theMap.containsKey( "hpBonus" ) )
                hpBonus = DP.dp( (double) theMap.get( "hpBonus" ) * 100 );
            if( theMap.containsKey( "damageBonus" ) )
                speedBonus = DP.dp( (double) theMap.get( "damageBonus" ) * 100 );
        }

        sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.mobDamageBoost", new TranslationTextComponent( damageBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TranslationTextComponent( transKey ).setStyle( XP.textStyle.get( "grey" ) ) ), false );
        sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.mobHpBoost", new TranslationTextComponent( hpBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TranslationTextComponent( transKey ).setStyle( XP.textStyle.get( "grey" ) ) ), false );
        sender.sendStatusMessage( new TranslationTextComponent( "pmmo.text.mobSpeedBoost", new TranslationTextComponent( speedBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TranslationTextComponent( transKey ).setStyle( XP.textStyle.get( "grey" ) ) ), false );

        return 1;
    }

    private static int commandReloadConfig(CommandContext<CommandSource> context) throws CommandException
    {
        Requirements.init();    //load up locally

        context.getSource().getServer().getPlayerList().getPlayers().forEach( player ->
        {
            XP.syncPlayerConfig( player );
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.jsonConfigReload" ).setStyle( XP.textStyle.get( "green" ) ), false );
        });

        return 1;
    }
}
