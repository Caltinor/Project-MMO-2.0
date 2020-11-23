package harmonised.pmmo.commands;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.server.command.CommandTreeBase;

public class PmmoCommand extends CommandTreeBase
{
    public PmmoCommand()
    {
        super.addSubcommand( new XpAtLevelCommand() );
    }

    @Override
    public String getName()
    {
        return "pmmo";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return null;
    }

    public static void registerCommands( FMLServerStartingEvent event )
    {
        event.registerServerCommand( new PmmoCommand() );
    }
}