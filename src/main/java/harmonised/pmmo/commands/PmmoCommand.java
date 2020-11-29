package harmonised.pmmo.commands;

import harmonised.pmmo.skills.Skill;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.server.command.CommandTreeBase;

import java.util.ArrayList;
import java.util.List;

public class PmmoCommand extends CommandTreeBase
{
    public static List<String> skillCompletions = new ArrayList( Skill.stringMap.keySet() );

    public PmmoCommand()
    {
        super.addSubcommand( new ToolsCommand() );
        super.addSubcommand( new PartyCommand() );
        super.addSubcommand( new AdminCommand() );
        super.addSubcommand( new DebugCommand() );
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

    public static void reply(EntityPlayerMP sender, ITextComponent message )
    {
        if( sender == null )
            System.out.println( message.getFormattedText() );
        else
            sender.sendStatusMessage( message, false );
    }
}