package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.DP;

import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CheckStatsCommand extends CommandBase
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName()
    {
        return "checkStats";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return new ArrayList<>();
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
        catch( PlayerNotFoundException e )
        {
            LOGGER.info( "CheckStats command fired not from player " + args, e );
            return;
        }

        if( args.length > 0 )
        {
            try
            {
                EntityPlayerMP target = getPlayer( server, sender, args[0] );

                NBTTagCompound packetxpMap = NBTHelper.mapSkillToNbt( FConfig.getXpMap( target ) );

                packetxpMap.setString( "UUID", target.getUniqueID().toString() );
                packetxpMap.setString( "name", target.getDisplayName().getFormattedText() );

                NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( packetxpMap, 3 ), (EntityPlayerMP) sender );
            }
            catch( PlayerNotFoundException e )
            {
                LOGGER.info( "Error: Invalid Player requested at CheckStats Command \"" + args[2] + "\"", e );

                player.sendStatusMessage(  new TextComponentTranslation( "pmmo.invalidPlayer", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );
                return;
            }
        }

        return;
    }
}