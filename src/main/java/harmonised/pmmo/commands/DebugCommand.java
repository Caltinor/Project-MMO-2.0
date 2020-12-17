package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugCommand extends CommandBase
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName()
    {
        return "debug";
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
        {
            completions.add( "nearbyPowerLevel" );
            completions.add( "searchRegistry" );
        }
        else if( args[0].toLowerCase().equals( "searchregistry" ) )
        {
            if( args.length == 2 )
            {
                completions.add( "biome" );
                completions.add( "enchant" );
                completions.add( "entity" );
                completions.add( "item" );
                completions.add( "potioneffect" );
            }
            else if( args.length == 3 )
                completions.add( "minecraft" );
        }
        else if( args[0].toLowerCase().equals( "nearbypowerlevel" ) )
        {
            if( args.length == 2 )
                return getListOfStringsMatchingLastWord( args, server.getOnlinePlayerNames() );
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

    public static void append( String input, StringBuilder listOut, StringBuilder listOutExtra, StringBuilder listOutForBuilder )
    {
        listOut.append(input).append("\n");
        listOutExtra.append("\"").append(input).append("\": { \"info\": value },\n");
        listOutForBuilder.append("addData( \"dataType\", \"").append(input).append("\", { \"info\": value } );\n");
    }

    @Override
    public void execute( MinecraftServer server, ICommandSender sender, String[] args ) throws CommandException
    {
        EntityPlayerMP player = null;
        int permissionLevel = 0;
        try
        {
            player = CommandBase.getCommandSenderAsPlayer( sender );
        }
        catch ( Exception e )
        {
            //Not player, it's fine
        }

        if( args.length > 0 )
        {
            String command = args[0].toLowerCase();

            switch( command )
            {
                case "searchregistry":
                {
                    if( args.length > 1 )
                    {
                        String type = args[1].toLowerCase();
                        if( args.length > 2 )
                        {
                            String query = args[2].toLowerCase();
                            StringBuilder listOut = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");
                            StringBuilder listOutExtra = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");
                            StringBuilder listOutForBuilder = new StringBuilder();

                            switch( type.toLowerCase() )
                            {
                                case "item":
                                    for( Item item : ForgeRegistries.ITEMS )
                                    {
                                        String regName = item.getRegistryName().toString();
                                        if( regName.contains( query ) )
                                        {
                                            append( regName, listOut, listOutExtra, listOutForBuilder );
                                        }
                                    }
                                    break;

                                case "biome":
                                    for( Biome item : ForgeRegistries.BIOMES )
                                    {
                                        String regName = item.getRegistryName().toString();
                                        if( regName.contains( query ) )
                                        {
                                            append( regName, listOut, listOutExtra, listOutForBuilder );
                                        }
                                    }
                                    break;

                                case "enchant":
                                    for( Enchantment item : ForgeRegistries.ENCHANTMENTS )
                                    {
                                        String regName = item.getRegistryName().toString();
                                        if( regName.contains( query ) )
                                        {
                                            append( regName, listOut, listOutExtra, listOutForBuilder );
                                        }
                                    }
                                    break;

                                case "potioneffect":
                                    for( Potion item : ForgeRegistries.POTIONS )
                                    {
                                        String regName = item.getRegistryName().toString();
                                        if( regName.contains( query ) )
                                        {
                                            append( regName, listOut, listOutExtra, listOutForBuilder );
                                        }
                                    }
                                    break;

                                case "entity":
                                    for( EntityEntry item : ForgeRegistries.ENTITIES )
                                    {
                                        String regName = item.getRegistryName().toString();
                                        if( regName.contains( query ) )
                                        {
                                            append( regName, listOut, listOutExtra, listOutForBuilder );
                                        }
                                    }
                                    break;
                            }

                            LOGGER.info( listOut.toString() );
                            LOGGER.info( listOutExtra.toString() );
                            LOGGER.info( listOutForBuilder.toString() );

                            return;
                        }
                        else
                            PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( Skill.getSkillStyle( "red" ) ) );
                    }
                    else
                        PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( Skill.getSkillStyle( "red" ) ) );
                }
                    return;

                case "nearbypowerlevel":
                {
                    EntityPlayerMP target = null;
                    if( args.length > 1 )
                    {
                        try
                        {
                            target = getPlayer( server, sender, args[1] );
                        }
                        catch( PlayerNotFoundException e )
                        {
                            //no target, target will be sender
                        }
                    }

                    if( player == null && target == null )
                        LOGGER.info( "PMMO NearbyPowerLevel Command: Sender not player, and target is invalid!" );
                    else
                    {
                        if( target == null )
                            target = player;

                        double totalPowerLevel = 0;

                        for( EntityPlayer nearbyPlayer : XP.getNearbyPlayers( target ) )
                        {
                            totalPowerLevel += XP.getPowerLevel( nearbyPlayer.getUniqueID() );
                        }

                        LOGGER.info( "PMMO NearbyPowerLevel Command Output: " + totalPowerLevel );

                        PmmoCommand.reply( player, new TextComponentString( totalPowerLevel + " " + new TextComponentTranslation( "pmmo.power" ).getUnformattedText() ) );
                    }
                }
                    return;
            }
        }
        else
            PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( Skill.getSkillStyle( "red" ) ) );
    }
}