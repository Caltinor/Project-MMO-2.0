package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SyncCommand extends CommandBase
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
        try
        {
            EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer( sender );
            XP.syncPlayer( player );
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.skillsResynced" ), false );
        }
        catch( PlayerNotFoundException e )
        {
            LOGGER.info( "Sync command fired not from player " + args, e );
        }
        return;
    }
}
