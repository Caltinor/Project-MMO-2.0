package harmonised.pmmo.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;

public class PmmoCommand extends CommandTreeBase
{
	public PmmoCommand()
	{
		super.addSubcommand( new CommandXpAtLevel() );
		super.addSubcommand( new CommandLevelAtXp() );
		super.addSubcommand( new CommandSet() );
		super.addSubcommand( new CommandClear() );
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
}
