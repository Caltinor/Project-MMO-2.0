package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JsonConfig;

import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReloadConfigCommand extends CommandBase
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName()
    {
        return "reload";
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
        JsonConfig.init();  //Load Up Locally
        if( FConfig.autoGenerateValuesEnabled )
            JsonConfig.setAutoValues(); //Set Auto Values

        server.getPlayerList().getPlayers().forEach( player ->
        {
            XP.syncPlayerDataAndConfig( player );
            XP.updateRecipes( player );
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.jsonConfigReload" ).setStyle( XP.textStyle.get( "green" ) ), false );
        });

        System.out.println( "PMMO Config Reloaded" );

        return;
    }
}
