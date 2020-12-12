package harmonised.pmmo.commands;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyMemberInfo;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PrefCommand extends CommandBase
{
    @Override
    public String getName()
    {
        return "party";
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

        if( args.length == 0 )
        {
            completions.add( "create" );
            completions.add( "invite" );
            completions.add( "accept" );
            completions.add( "decline" );
            completions.add( "leave" );
        }
        else if( args.length == 1 && args[0].toLowerCase().equals( "invite" ) )
            return getListOfStringsMatchingLastWord( args, server.getOnlinePlayerNames() );
        return completions;
    }

    @Override
    public List<String> getAliases()
    {
        return new ArrayList<>();
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
            System.out.println( "ERROR: Pref Command called not by a player!" );
            return;
        }
        Map<String, Double> prefsMap = FConfig.getPreferencesMap( player );
        Double value = null;
        if( args.length > 3 )
        {
            value = Double.parseDouble( args[3] );
            if( value < 0 )
                value = 0D;
        }


        boolean matched = false;
        String match = "ERROR";

        for( String element : PmmoCommand.suggestPref )
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
                prefsMap.put( match, value );

                NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( NBTHelper.mapStringToNbt( prefsMap ), 0 ), (EntityPlayerMP) player );
                AttributeHandler.updateAll( player );

                player.sendStatusMessage( new TextComponentTranslation( "pmmo.hasBeenSet", match, args[3] ), false );
            }
            else if( prefsMap.containsKey( match ) )
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.hasTheValue", "" + match, "" + prefsMap.get( match ) ), false );
            else
                player.sendStatusMessage( new TextComponentTranslation( "pmmo.hasUnsetValue", "" + match ), false );
        }
        else
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidChoice", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );

        return;
    }
}